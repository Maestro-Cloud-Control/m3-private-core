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

package io.maestro3.agent.api.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import io.maestro3.agent.backup.SimpleBareosClient;
import io.maestro3.agent.dao.IRegionRepository;
import io.maestro3.agent.dao.ITenantRepository;
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.chef.client.context.IChefContext;
import io.maestro3.chef.client.context.IChefContextFactory;
import io.maestro3.chef.model.ChefInstance;
import io.maestro3.chef.model.ChefRoleInfo;
import io.maestro3.chef.model.UserData;
import io.maestro3.chef.model.role.ChefRole;
import io.maestro3.chef.service.IChefDataBagService;
import io.maestro3.chef.service.IChefInfoService;
import io.maestro3.chef.service.IChefInstanceService;
import io.maestro3.chef.service.IResourceIdGenerator;
import io.maestro3.chef.util.ChefUtils;
import io.maestro3.chef.util.RandomStringUtils;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.JsonUtils;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.backup.BackupResponse;
import io.maestro3.sdk.v3.model.backup.BackupState;
import io.maestro3.sdk.v3.model.instance.SdkInstance;
import io.maestro3.sdk.v3.model.instance.SdkOpenStackInstance;
import io.maestro3.sdk.v3.request.backup.BackupRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BackupServiceHandler<TR extends ITenantRepository, RR extends IRegionRepository> extends AbstractM3ApiHandler<BackupRequest, List<BackupResponse>> {
    private static final String BACKUP_JOB_PATTERN = "%s-%s-backup";
    private Map<String, SimpleBareosClient> bareosClientMap = new HashMap<>();
    private TR tenantService;
    private RR regionService;
    private IChefContextFactory chefContextFactory;
    private IChefInstanceService chefInstanceService;
    private IChefDataBagService dataBagService;
    private IChefInfoService chefInfoService;
    private IResourceIdGenerator resourceIdGenerator;
    private SdkCloud cloudType;


    public BackupServiceHandler(TR tenantService, RR regionService, IChefContextFactory chefContextFactory,
                                IChefInstanceService chefInstanceService, IChefDataBagService dataBagService,
                                IChefInfoService chefInfoService, IResourceIdGenerator resourceIdGenerator,
                                SdkCloud cloudType) {
        super(BackupRequest.class, ActionType.BACKUP_PLATFORM_SERVICE);
        this.chefInfoService = chefInfoService;
        this.tenantService = tenantService;
        this.regionService = regionService;
        this.chefContextFactory = chefContextFactory;
        this.chefInstanceService = chefInstanceService;
        this.dataBagService = dataBagService;
        this.resourceIdGenerator = resourceIdGenerator;
        this.cloudType = cloudType;
    }

    @Override
    protected List<BackupResponse> handlePayload(ActionType actionType, BackupRequest request) throws Exception {
        List<String> instances = request.getInstances();
        String serviceId = request.getServiceId();
        xor(CollectionUtils.isEmpty(instances), StringUtils.isEmpty(serviceId), "instances", "serviceId");
        IRegion region = regionService.findByRegionAlias(request.getRegion());
        ITenant tenant = tenantService.findByTenantAliasAndRegionId(request.getTenantName(), region.getId());
        String backupServerRole = chefInfoService.backupServerRole();
        String backupClientRole = chefInfoService.backupClientRole();
        IChefContext context = chefContextFactory.getInstance(tenant.getName(), region.getRegionAlias());
        validateBackupRole(backupServerRole, backupClientRole, context.getRoles());
        List<ChefInstance> instancesByRole = chefInstanceService.findInstancesByRole(tenant.getName(), region.getRegionAlias(), backupServerRole);
        ChefInstance backupServer = instancesByRole.stream()
                .filter(i -> i.getInstanceId().equalsIgnoreCase(request.getBackupServer()))
                .findFirst()
                .orElseThrow(() -> new ReadableAgentException(
                        String.format("Backup server with id %s is not found", request.getBackupServer())));
        if (CollectionUtils.isNotEmpty(instances)) {
            return processInstancesBackup(context, backupClientRole, backupServer, tenant, region, instances);
        } else {
            throw new ReadableAgentException("Service backup currently unsupported");
        }
    }


    private List<BackupResponse> processInstancesBackup(IChefContext context, String backupClientRole, ChefInstance backupServer,
                                                        ITenant tenant, IRegion region, List<String> instances) {
        LOG.info("Processing backup for {} instances", instances.size());
        List<BackupResponse> responses = new ArrayList<>();
        for (String instanceId : instances) {
            try {
                SdkOpenStackInstance instance = new SdkOpenStackInstance();
                instance.setInstanceId(instanceId);
                String resourceId = resourceIdGenerator.generate(tenant.getName(), region.getRegionAlias(), instance);
                ChefInstance target = chefInstanceService.getInstanceByResourceId(resourceId);
                BackupResponse response = new BackupResponse();
                response.setInstanceId(instanceId);
                response.setRegion(region.getRegionAlias());
                response.setTenant(tenant.getName());

                if (target == null) {
                    String err = String.format("Failed to find chef instance by id %s", instanceId);
                    fillResponse(response, BackupState.FAILED, err, responses);
                    continue;
                }
                if (target.getRoles().contains(backupClientRole)) {
                    processBackup(backupClientRole, backupServer, response, responses, target);
                } else {
                    LOG.info("Configuring backup role for instance({})", target.getInstanceId());
                    //create databag item on backup server and push fqdn and password for connection
                    String token = new String(RandomStringUtils.getAlphanumericSourceSymbols()) + "-!.";
                    token = RandomStringUtils.random(20, token.toCharArray());
                    Map<String, String> clientData = Map.of(
                            UserData.CLIENT_FQDN, getAddress(target),
                            UserData.PASSWORD, token,
                            UserData.ROLE, getInstanceServiceRole(target, backupClientRole));
                    Map<String, String> clients = dataBagService.getDataBagItem(context, backupServer.getInstanceId(), "clients");
                    if (CollectionUtils.isEmpty(clients)) {
                        clients = new HashMap<>();
                    }
                    clients.put(target.getInstanceId(), JsonUtils.convertObjectToJson(clientData));
                    dataBagService.updateOrCreateItem(context, backupServer.getInstanceId(), "clients", clients);

                    // add backup role
                    Set<String> targetRoles = new LinkedHashSet<>();
                    targetRoles.add(ChefRole.BASE.getName());
                    targetRoles.add(backupClientRole);
                    targetRoles.addAll(target.getRoles());
                    dataBagService.pushChefRole(context, target.getInstanceId(),
                            targetRoles.toArray(new String[]{}));

                    //push token
                    Map<String, String> backupData = Map.of(UserData.SERVER_FQDN, getAddress(backupServer), UserData.PASSWORD, token);
                    dataBagService.updateOrCreateItem(context, target.getInstanceId(), backupClientRole, backupData);
                    chefInstanceService.setRoles(target, targetRoles);
                    fillResponse(response, BackupState.SUCCESS, "Instance was successfully queued to register on backup server", responses);
                }
            } catch (Exception ex) {
                BackupResponse response = new BackupResponse();
                response.setInstanceId(instanceId);
                response.setRegion(region.getRegionAlias());
                response.setTenant(tenant.getName());
                String err = String.format("Failed to process instance %s backup", response.getInstanceId());
                LOG.error(err, ex);
                fillResponse(response, BackupState.FAILED, err, responses);
            }
        }
        return responses;
    }

    private boolean processBackup(String backupClientRole, ChefInstance backupServer, BackupResponse response, List<BackupResponse> responses, ChefInstance target) throws Exception {
        SimpleBareosClient client = bareosClientMap.computeIfAbsent(backupServer.getInstanceId(), (i) -> getBareosClient(backupServer));
        if (client == null) {
            fillResponse(response, BackupState.SKIPPED, "Failed to build bareos client for " + backupServer.getInstanceId(), responses);
            return true;
        }
        String baseRole = target.getRoles().stream()
                .filter(r -> !r.equals(backupClientRole) && !r.equals("base"))
                .findFirst().orElse(null);
        if (StringUtils.isEmpty(baseRole)) {
            fillResponse(response, BackupState.SKIPPED, "Failed to extract base vm role", responses);
            return true;
        }
        String jobName = String.format(BACKUP_JOB_PATTERN, baseRole, target.getInstanceId());
        try {
            client.getJob(jobName);
        } catch (Exception ex) {
            fillResponse(response, BackupState.SKIPPED, "Backup job is not available yet", responses);
            return true;
        }
        boolean success = client.runBackupJob(jobName);
        if (success) {
            fillResponse(response, BackupState.SUCCESS, "Backup job executed.", responses);
        } else {
            fillResponse(response, BackupState.FAILED, "Failed to execute backup job", responses);
        }
        return false;
    }

    private SimpleBareosClient getBareosClient(ChefInstance backupServer) {
        String additionalData = backupServer.getAdditionalData();
        Map<String, String> additionalParams = ChefUtils.decodeJson(additionalData, new TypeReference<>() {
        });
        String fqdn = backupServer.getFqdn();
        String url = "http://" + fqdn + ":8000";
        String apiPassword = additionalParams.get("bconsole_restapi_password");
        String user = additionalParams.getOrDefault("bconsole_restapi_user", "api_admin");
        if (StringUtils.isEmpty(fqdn) || StringUtils.isEmpty(apiPassword) || StringUtils.isEmpty(user)) {
            LOG.error("Failed to build bareos client for {}", backupServer.getInstanceId());
            return null;
        }
        return new SimpleBareosClient(url, user, apiPassword);
    }

    private static void fillResponse(BackupResponse response, BackupState failed, String err, List<BackupResponse> responses) {
        response.setState(failed);
        response.setDescription(err);
        responses.add(response);
    }

    private String getInstanceServiceRole(ChefInstance target, String backupClient) {
        Set<String> roles = new HashSet<>(target.getRoles());
        roles.remove(ChefRole.BASE.getName());
        roles.remove(backupClient);
        return roles.stream().findFirst().orElseThrow(
                () -> new ReadableAgentException(String.format("An instance %s cannot be queued for backup " +
                        "because it does not contain a service", target.getInstanceId())));
    }

    private static SdkInstance buildInstance(String regionName, String tenantName, ITenant tenant, String instance) {
        SdkInstance sdkInstance = new SdkInstance() {
        };
        sdkInstance.setInstanceId(instance);
        sdkInstance.setCloud(SdkCloud.OPEN_STACK);
        sdkInstance.setTenant(tenantName);
        sdkInstance.setRegion(regionName);
        return sdkInstance;
    }

    private static String getAddress(ChefInstance target) {
        return StringUtils.isNotBlank(target.getFqdn())
                ? target.getFqdn()
                : StringUtils.isNotBlank(target.getPublicIp())
                ? target.getPublicIp()
                : target.getPrivateIp();
    }

    private void validateBackupRole(String backupRoleName, String backupClientRole, List<ChefRoleInfo> roles) {
        if (roles.stream().filter(r -> r.getRoleName().equals(backupRoleName)).findFirst().isEmpty()) {
            throw new ReadableAgentException("Incorrect configuration of backup server. Backups server role is not available");
        }
        if (roles.stream().filter(r -> r.getRoleName().equals(backupClientRole)).findFirst().isEmpty()) {
            throw new ReadableAgentException("Incorrect configuration of backup server. Backups client role is not available");
        }
    }

    private static void xor(boolean statement1, boolean statement2, String param1, String param2) {
        if (statement1 && statement2) {
            throw new IllegalArgumentException(String.format("Parameters %s and %s cant be specified simultaneously", param1, param2));
        }
        if (!statement1 && !statement2) {
            throw new IllegalArgumentException(String.format("Parameter %s or %s must be specified", param1, param2));
        }
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return cloudType;
    }
}
