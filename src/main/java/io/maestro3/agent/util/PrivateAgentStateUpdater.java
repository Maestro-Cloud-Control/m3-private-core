/*
 * Copyright 2023 Maestro Cloud Control LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.maestro3.agent.util;

import io.maestro3.agent.cadf.ICadfAuditEventSender;
import io.maestro3.agent.dao.IRegionRepository;
import io.maestro3.agent.dao.ITenantRepository;
import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.util.conversion.tenant.BaseTenantInfoConverter;
import io.maestro3.agent.util.conversion.tenant.TenantInfoConverter;
import io.maestro3.cadf.ICadfAction;
import io.maestro3.cadf.model.CadfActions;
import io.maestro3.cadf.model.CadfAttachment;
import io.maestro3.cadf.model.CadfAuditEvent;
import io.maestro3.cadf.model.CadfEventType;
import io.maestro3.cadf.model.CadfOutcomes;
import io.maestro3.cadf.model.CadfResource;
import io.maestro3.cadf.model.CadfResourceTypes;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.DateUtils;
import io.maestro3.sdk.internal.util.JsonUtils;
import io.maestro3.sdk.v3.model.audit.AuditEventGroupType;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class PrivateAgentStateUpdater implements IPrivateAgentStateUpdater {

    private static final String ID_NAMESPACE = "maestro2:";
    private static final String ACTIVATION_DATE = "activationDate";
    private static final String STATUS = "status";
    private static final String REGION_NAME = "regionName";
    private static final String RELATED_TENANTS = "relatedTenants";

    private static final CadfResource SYSTEM = CadfResource.builder()
            .ofType(CadfResourceTypes.system())
            .withId(ID_NAMESPACE + "SYSTEM")
            .withName("System")
            .build();

    private static final CadfResource UNKNOWN = CadfResource.builder()
            .ofType(CadfResourceTypes.unknown())
            .withId(ID_NAMESPACE + "UNKNOWN")
            .withName("Unknown")
            .build();

    private static final TenantInfoConverter<ITenant> DEFAULT_TENANT_INFO_CONVERTER = new BaseTenantInfoConverter();

    private String exchangeName;
    private String syncQueue;
    private String asyncQueue;
    private String responseQueue;
    private String keyName;
    private String privateAgentName;
    private ICadfAuditEventSender auditEventSender;
    private IRegionRepository regionRepository;
    private ITenantRepository tenantRepository;
    private Map<PrivateCloudType, TenantInfoConverter<?>> tenantInfoConverters;

    @Autowired
    public PrivateAgentStateUpdater(
            @Value("${private.agent.rabbit.m3api.exchange}") String exchangeName,
            @Value("${private.agent.rabbit.m3api.sync.queue}") String syncQueue,
            @Value("${private.agent.rabbit.m3api.async.queue}") String asyncQueue,
            @Value("${server.m3api.response.queue.name}") String responseQueue,
            @Value("${server.m3api.access.key}") String keyName,
            @Value("${private.agent.name}") String privateAgentName,
            ICadfAuditEventSender auditEventSender,
            IRegionRepository regionRepository,
            ITenantRepository tenantRepository,
            List<TenantInfoConverter<?>> tenantInfoConverters) {
        this.exchangeName = exchangeName;
        this.regionRepository = regionRepository;
        this.tenantRepository = tenantRepository;
        this.privateAgentName = privateAgentName;
        this.syncQueue = syncQueue;
        this.asyncQueue = asyncQueue;
        this.responseQueue = responseQueue;
        this.keyName = keyName;
        this.auditEventSender = auditEventSender;
        this.tenantInfoConverters = tenantInfoConverters.stream()
                .collect(Collectors.toMap(TenantInfoConverter::getPrivateCloudType, Function.identity()));
    }

    @PostConstruct
    @Scheduled(cron = "0 */5 * * * ?") // send heartbeat event
    public void init() {
        pushStateEvent(false);
    }

    @Override
    public void pushStateEvent(boolean forceUpdate) {
        Date date = new Date(System.currentTimeMillis());

        String actionId = new ObjectId().toHexString();
        CadfResource target = createTarget(privateAgentName);

        List<CadfAttachment> attachments = getAgentAttachments(forceUpdate, getState());

        CadfAuditEvent cadfAuditEvent = generateCadfAuditEvent(CadfActions.start(), date, actionId, target, attachments);

        auditEventSender.sendCadfAuditEvent(cadfAuditEvent, Collections.singletonList(AuditEventGroupType.PRIVATE_AGENT));
    }

    @Override
    public Map<PrivateCloudType, List<Map<String, Object>>> getState() {
        List<IRegion> regions = regionRepository.findAll();
        Map<String, List<ITenant>> tenants = ((List<ITenant>) tenantRepository.findAll()).stream()
                .collect(Collectors.groupingBy(ITenant::getRegionId));
        Map<PrivateCloudType, List<Map<String, Object>>> result = new HashMap<>();
        for (IRegion region : regions) {
            List<Map<String, Object>> regionInfos = result.computeIfAbsent(region.getCloud(), (cloudType) -> new ArrayList<>());
            Map<String, Object> info = buildRegionInfo(region, tenants);
            regionInfos.add(info);
        }
        return result;
    }

    private Map<String, Object> buildRegionInfo(IRegion region, Map<String, List<ITenant>> tenants) {
        Map<String, Object> regionInfo = new HashMap<>();
        regionInfo.put(ACTIVATION_DATE, convertToTimestampFrom(region.getId()));
        regionInfo.put(REGION_NAME, region.getRegionAlias());
        regionInfo.put(STATUS, region.isManagementAvailable() ? "AVAILABLE" : "READ_ONLY");
        List<Map<String, Object>> relatedTenantsInfo = new ArrayList<>();
        List<ITenant> regionTenants = tenants.get(region.getId());
        if (CollectionUtils.isNotEmpty(regionTenants)) {
            for (ITenant regionTenant : regionTenants) {
                TenantInfoConverter<?> converter = tenantInfoConverters.getOrDefault(regionTenant.getCloud(),
                        DEFAULT_TENANT_INFO_CONVERTER);
                Map<String, Object> data = converter.getTenantInfo(regionTenant);
                relatedTenantsInfo.add(data);
            }
        }
        regionInfo.put(RELATED_TENANTS, relatedTenantsInfo);
        return regionInfo;
    }

    public static long convertToTimestampFrom(String objectId) {
        return Long.parseLong(objectId.substring(0, 8), 16) * 1000;
    }

    private CadfAuditEvent generateCadfAuditEvent(ICadfAction action,
                                                  Date date,
                                                  String actionId,
                                                  CadfResource target,
                                                  List<CadfAttachment> attachments) {
        return CadfAuditEvent.builder()
                .withId(ID_NAMESPACE + actionId)
                .withAction(action)
                .withEventTime(DateUtils.formatDate(date, DateUtils.CADF_FORMAT_TIME))
                .withEventType(CadfEventType.ACTIVITY)
                .withInitiator(UNKNOWN)
                .withObserver(SYSTEM)
                .withOutcome(CadfOutcomes.success())
                .withAttachments(attachments)
                .withMeasurements(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withTarget(target)
                .build();
    }

    private CadfResource createTarget(String privateAgentName) {
        return CadfResource.builder()
                .ofType(CadfResourceTypes.privateAgent())
                .withId(ID_NAMESPACE + privateAgentName)
                .withName(privateAgentName)
                .build();
    }

    private List<CadfAttachment> getAgentAttachments(boolean forceUpdate, Map<PrivateCloudType, List<Map<String, Object>>> tenantsAndRegionsInfo) {
        List<CadfAttachment> result = new ArrayList<>();

        CadfAttachment<Object> cadfAttachment = new CadfAttachment<>("string", "exchange");
        cadfAttachment.setContent(exchangeName);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "syncQueue");
        cadfAttachment.setContent(syncQueue);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "asyncQueue");
        cadfAttachment.setContent(asyncQueue);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "responseQueue");
        cadfAttachment.setContent(responseQueue);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "sdkKey");
        cadfAttachment.setContent(keyName);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "agentName");
        cadfAttachment.setContent(privateAgentName);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "agentInfo");
        cadfAttachment.setContent(JsonUtils.convertObjectToJson(tenantsAndRegionsInfo));
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("boolean", "force");
        cadfAttachment.setContent(forceUpdate);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "description");
        cadfAttachment.setContent("Private agent heartbeat event");
        result.add(cadfAttachment);

        return result;
    }
}
