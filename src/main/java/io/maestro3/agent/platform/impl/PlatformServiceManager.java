package io.maestro3.agent.platform.impl;

import io.maestro3.agent.dao.IRegionRepository;
import io.maestro3.agent.dao.ITenantRepository;
import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.platform.IPlatformServiceManager;
import io.maestro3.agent.platform.deployment.IPlatformServiceDeploymentManager;
import io.maestro3.agent.platform.deployment.provider.IPlatformServiceDeploymentManagerProvider;
import io.maestro3.agent.platform.model.PlatformServiceDefinition;
import io.maestro3.agent.platform.model.PlatformServiceDefinitionInfo;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentInfo;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentType;
import io.maestro3.agent.platform.model.PlatformServiceEntry;
import io.maestro3.agent.platform.model.PlatformServiceEntryInfo;
import io.maestro3.agent.platform.model.params.ActivatePlatformServiceParams;
import io.maestro3.agent.platform.model.params.DeactivatePlatformServiceParams;
import io.maestro3.agent.platform.model.params.ListPlatformServiceDefinitionsParams;
import io.maestro3.agent.platform.model.params.ListPlatformServiceEntriesParams;
import io.maestro3.agent.platform.model.params.RegisterPlatformServiceParams;
import io.maestro3.agent.terraform.model.PlatformServiceVariable;
import io.maestro3.agent.terraform.sevice.IPlatformServiceDefinitionService;
import io.maestro3.agent.terraform.sevice.IPlatformServiceEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;
import team.syndicate.terraform.engine.utils.CollectionUtils;
import team.syndicate.terraform.engine.utils.StringUtils;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PlatformServiceManager implements IPlatformServiceManager {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformServiceManager.class);
    private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile("[\\w_ \\-.]{3,50}");
    private static final String SERVICE_NAME_FORMAT_ERROR = "Service name must contain only characters, symbols: _-. and have size between 3 and 50\n";
    private static final String SERVICE_ALREADY_EXIST_ERROR = "Service '%s' already exists";

    private final HttpClient client = HttpClient.newHttpClient();
    private final IPlatformServiceDefinitionService definitionService;
    private final IPlatformServiceEntryService entryService;
    private final IPlatformServiceDeploymentManagerProvider deploymentManagerProvider;
    private final ITenantRepository tenantService;
    private final IRegionRepository regionService;

    @Autowired
    public PlatformServiceManager(IPlatformServiceDefinitionService definitionService,
                                  IPlatformServiceEntryService entryService,
                                  IPlatformServiceDeploymentManagerProvider deploymentManagerProvider,
                                  ITenantRepository tenantService,
                                  IRegionRepository regionService) {
        this.definitionService = definitionService;
        this.entryService = entryService;
        this.deploymentManagerProvider = deploymentManagerProvider;
        this.tenantService = tenantService;
        this.regionService = regionService;
    }

    @Override
    public String registerPlatformService(final RegisterPlatformServiceParams params) {
        final String serviceName = params.getName();
        validateServiceName(serviceName);
        if (definitionService.isServiceExist(serviceName)) {
            throw new RuntimeException(String.format(SERVICE_ALREADY_EXIST_ERROR, serviceName));
        }

        final IPlatformServiceDeploymentManager deploymentManager = deploymentManagerProvider.provideDeploymentManager(params.getDeploymentType());
        final PlatformServiceDeploymentInfo deploymentInfo = deploymentManager.handleServiceRegistration(serviceName, params.getDeploymentParams());

        final PlatformServiceDefinition platformServiceDefinition = new PlatformServiceDefinition()
                .withName(serviceName)
                .withTitle(params.getTitle())
                .withSupportedClouds(params.getSupportedClouds())
                .withTenantDisplayName(params.isAllTenants() ? null : params.getTenantDisplayName())
                .withDescription(params.getDescription())
                .withDiscoverableUrl(params.getDiscoverableUrl())
                .withServiceDeploymentInfo(deploymentInfo)
                .withProductVersion(params.getProductVersion())
                .withCategories(params.getCategories())
                .withOperatingSystem(params.getOperatingSystem())
                .withDeliveryMethod(params.getDeliveryMethod())
                .withUsage(params.getUsage())
                .withSupport(params.getSupport());
        definitionService.save(platformServiceDefinition);
        return deploymentManager.buildRegistrationMessage(platformServiceDefinition);
    }

    private void validateServiceName(final String serviceName) {
        final Matcher matcher = SERVICE_NAME_PATTERN.matcher(serviceName);
        if (!matcher.matches()) {
            throw new RuntimeException(SERVICE_NAME_FORMAT_ERROR);
        }
    }

    @Override
    public PlatformServiceEntryInfo activateService(final ActivatePlatformServiceParams params) {
        final IRegion region = regionService.findByRegionAlias(params.getRegionName());
        final ITenant tenant = tenantService.findByTenantAliasAndRegionId(params.getTenantDisplayName(), region.getId());
        final PlatformServiceDefinition platformServiceDefinition = definitionService.findByNameAndTenant(params.getServiceName(), tenant)
                .orElseThrow(() -> new RuntimeException(String.format("Platform service %s not found for tenant %s in %s cloud",
                        params.getServiceName(), tenant.getTenantAlias(), tenant.getCloud())));

        final PlatformServiceDeploymentInfo serviceDeploymentInfo = platformServiceDefinition.getServiceDeploymentInfo();
        final PlatformServiceDeploymentType providerType = serviceDeploymentInfo.getProviderType();
        final IPlatformServiceDeploymentManager deploymentManager = deploymentManagerProvider.provideDeploymentManager(providerType);

        final PlatformServiceEntry serviceEntry = deploymentManager.activateService(tenant, region, serviceDeploymentInfo.getProviderParams(),
                params.getVariables(), params.getRequester(), platformServiceDefinition.getName());
        entryService.save(serviceEntry);
        return deploymentManager.convertEntryInfo(serviceEntry);
    }

    @Override
    public Collection<PlatformServiceVariable> getServiceVariables(final String serviceName) {
        final PlatformServiceDefinition platformServiceDefinition = definitionService.findByName(serviceName)
                .orElseThrow(() -> new RuntimeException("Platform service not found " + serviceName));
        final PlatformServiceDeploymentInfo serviceDeploymentInfo = platformServiceDefinition.getServiceDeploymentInfo();
        final PlatformServiceDeploymentType providerType = serviceDeploymentInfo.getProviderType();
        final IPlatformServiceDeploymentManager deploymentManager = deploymentManagerProvider.provideDeploymentManager(providerType);
        return deploymentManager.getServiceVariables(serviceDeploymentInfo.getProviderParams());
    }

    @Override
    public Collection<PlatformServiceEntryInfo> listServiceEntries(final ListPlatformServiceEntriesParams params) {
        Collection<PlatformServiceEntry> serviceEntries = getServiceEntries(params);
        if (CollectionUtils.isEmpty(serviceEntries)) {
            return Collections.emptyList();
        }

        final Collection<PlatformServiceEntryInfo> result = new ArrayList<>(serviceEntries.size());
        final Map<PlatformServiceDeploymentType, List<PlatformServiceEntry>> serviceEntriesByDeploymentType = serviceEntries.stream()
                .collect(Collectors.groupingBy(serviceEntry -> serviceEntry.getDeploymentInfo().getProviderType()));
        for (Map.Entry<PlatformServiceDeploymentType, List<PlatformServiceEntry>> mapEntry : serviceEntriesByDeploymentType.entrySet()) {
            final PlatformServiceDeploymentType providerType = mapEntry.getKey();
            final IPlatformServiceDeploymentManager deploymentManager = deploymentManagerProvider.provideDeploymentManager(providerType);
            final Collection<PlatformServiceEntryInfo> populatedServiceEntries = deploymentManager.getServiceEntryInfos(mapEntry.getValue());
            result.addAll(populatedServiceEntries);
        }
        return result;
    }

    private Collection<PlatformServiceEntry> getServiceEntries(final ListPlatformServiceEntriesParams params) {
        final IRegion region = params.getRegion();
        final ITenant tenant = params.getTenant();
        final String owner = params.getOwner();
        final String serviceName = params.getServiceName();
        final String serviceEntryId = params.getServiceEntryId();

        if (StringUtils.isNotBlank(serviceEntryId)) {
            final PlatformServiceEntry serviceEntry = findServiceEntry(region, tenant, serviceEntryId);
            validateServiceEntryOwner(serviceEntry, owner);
            return Collections.singleton(serviceEntry);
        }
        return entryService.find(region, tenant, serviceName, owner);
    }

    private void validateServiceEntryOwner(final PlatformServiceEntry serviceEntry, final String owner) {
        final boolean belongsToSpecifiedUser = Optional.ofNullable(owner)
                .map(specifiedOwner -> serviceEntry.getOwner().equals(specifiedOwner))
                .orElse(true);
        if (!belongsToSpecifiedUser) {
            throw new RuntimeException(String.format("Platform service entry with ID %s belongs to another user", serviceEntry.getServiceEntryId()));
        }
    }

    @Override
    public Collection<PlatformServiceDefinitionInfo> listServiceDefinitions(final ListPlatformServiceDefinitionsParams params) {
        final Collection<PlatformServiceDefinition> serviceDefinitions = getServiceDefinitions(params);
        if (CollectionUtils.isEmpty(serviceDefinitions)) {
            return Collections.emptyList();
        }

        final Collection<PlatformServiceDefinitionInfo> result = new ArrayList<>(serviceDefinitions.size());
        final Map<PlatformServiceDeploymentType, List<PlatformServiceDefinition>> serviceDefinitionsByDeploymentType = serviceDefinitions.stream()
                .collect(Collectors.groupingBy(serviceDefinition -> serviceDefinition.getServiceDeploymentInfo().getProviderType()));
        for (Map.Entry<PlatformServiceDeploymentType, List<PlatformServiceDefinition>> mapEntry : serviceDefinitionsByDeploymentType.entrySet()) {
            final PlatformServiceDeploymentType providerType = mapEntry.getKey();
            final IPlatformServiceDeploymentManager deploymentManager = deploymentManagerProvider.provideDeploymentManager(providerType);
            final Collection<PlatformServiceDefinitionInfo> populatedServiceEntries = deploymentManager.getServiceDefinitionInfos(mapEntry.getValue());
            result.addAll(populatedServiceEntries);
        }
        return result;
    }

    private Collection<PlatformServiceDefinition> getServiceDefinitions(final ListPlatformServiceDefinitionsParams params) {
        final ITenant tenant = params.getTenant();
        final String serviceName = params.getServiceName();
        final PlatformServiceDeploymentType deploymentType = params.getDeploymentType();

        return Optional.ofNullable(tenant)
                .map(providedTenant -> definitionService.findByTenant(providedTenant, deploymentType))
                .orElseGet(() -> definitionService.findByServiceName(serviceName, deploymentType)
                        .map(Collections::singleton)
                        .orElse(Collections.emptySet()));
    }

    @Override
    public void deactivateService(final DeactivatePlatformServiceParams params) {
        final IRegion region = params.getRegion();
        final ITenant tenant = params.getTenant();
        final String serviceEntryId = params.getServiceEntryId();

        final PlatformServiceEntry serviceEntry = findServiceEntry(region, tenant, serviceEntryId);
        final PlatformServiceDeploymentType providerType = serviceEntry.getDeploymentInfo().getProviderType();
        final IPlatformServiceDeploymentManager deploymentManager = deploymentManagerProvider.provideDeploymentManager(providerType);
        deploymentManager.deactivateService(serviceEntry, params.getRequester());
    }

    private PlatformServiceEntry findServiceEntry(final IRegion region, final ITenant tenant, final String serviceEntryId) {
        return entryService.findById(region, tenant, serviceEntryId)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Platform service entry with ID %s in %s cloud %s tenant not found",
                                serviceEntryId, tenant.getCloud(), tenant.getTenantAlias())));
    }

    @Override
    public String unregisterPlatformService(final String serviceName) {
        final PlatformServiceDefinition platformServiceDefinition = definitionService.findByName(serviceName)
                .orElseThrow(() -> new RuntimeException("Platform service not found " + serviceName));
        final PlatformServiceDeploymentType providerType = platformServiceDefinition.getServiceDeploymentInfo().getProviderType();
        final IPlatformServiceDeploymentManager deploymentManager = deploymentManagerProvider.provideDeploymentManager(providerType);
        final boolean serviceEntryExists = entryService.isServiceActivationExist(platformServiceDefinition.getName());
        deploymentManager.unregisterService(platformServiceDefinition, serviceEntryExists);
        definitionService.delete(platformServiceDefinition);
        LOG.info("Platform service {} definition was deleted", platformServiceDefinition.getName());
        return String.format("Service with name %s had been removed successfully", platformServiceDefinition.getName());
    }

    public static final class ActivatePaasHandlerBody {
        private String tenantName;
        private String tenantDisplayName;
        private String regionName;
        private String cloud;
        private String serviceName;
        private Map<String, TerraformUserVariable> variables;

        public ActivatePaasHandlerBody withTenantName(String tenantName) {
            this.tenantName = tenantName;
            return this;
        }

        public ActivatePaasHandlerBody withTenantDisplayName(String tenantDisplayName) {
            this.tenantDisplayName = tenantDisplayName;
            return this;
        }

        public ActivatePaasHandlerBody withRegionName(String regionName) {
            this.regionName = regionName;
            return this;
        }

        public ActivatePaasHandlerBody withCloud(String cloud) {
            this.cloud = cloud;
            return this;
        }

        public ActivatePaasHandlerBody withServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public ActivatePaasHandlerBody withVariables(Map<String, TerraformUserVariable> variables) {
            this.variables = variables;
            return this;
        }

        public String getTenantName() {
            return tenantName;
        }

        public String getTenantDisplayName() {
            return tenantDisplayName;
        }

        public String getRegionName() {
            return regionName;
        }

        public String getCloud() {
            return cloud;
        }

        public String getServiceName() {
            return serviceName;
        }

        public Map<String, TerraformUserVariable> getVariables() {
            return variables;
        }
    }
}
