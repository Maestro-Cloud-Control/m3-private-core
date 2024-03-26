package io.maestro3.agent.terraform.storage.impl.manager;

import io.maestro3.agent.model.base.BaseTenant;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.terraform.manager.ITerraformStackService;
import io.maestro3.agent.terraform.manager.ITerraformTemplateService;
import io.maestro3.agent.terraform.model.CreateOrUpdateTerraformTemplateParams;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.sender.IRabbitMqTerraformMessageSender;
import io.maestro3.agent.terraform.storage.internal.ITerraformTemplateInternalStorageCaller;
import io.maestro3.agent.tf.integration.TerraformPipeLineContext;
import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageInfo;
import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

@Service
public class InternalTerraformTemplateManager extends AbstractTerraformTemplateManager {

    private static final Logger LOG = LoggerFactory.getLogger(InternalTerraformTemplateManager.class);

    @Autowired
    public InternalTerraformTemplateManager(ITerraformTemplateService terraformTemplateService,
                                            ITerraformTemplateInternalStorageCaller internalStorageCaller,
                                            ITerraformStackService stackService,
                                            IRabbitMqTerraformMessageSender terraformMessageSender) {
        super(terraformTemplateService, internalStorageCaller, stackService, terraformMessageSender);
    }

    @Override
    protected TerraformTemplateStorageInfo configureStorage(final TerraformTemplate terraformTemplate,
                                                            final CreateOrUpdateTerraformTemplateParams params) {
        final String tenantName = terraformTemplate.getTenantName();
        internalStorageCaller.uploadTemplate(terraformTemplate, getTemplateContent(params));
        LOG.info("Template {} file for tenant {} was uploaded successfully", terraformTemplate.getName(), tenantName);
        return new TerraformTemplateStorageInfo().withStorageType(TerraformTemplateStorageType.INTERNAL_STORAGE);
    }

    @Override
    protected void updateStorageContent(final TerraformTemplate existingTemplate,
                                        final CreateOrUpdateTerraformTemplateParams params) {
        assertInternalStorageTemplate(existingTemplate);
        internalStorageCaller.updateTemplate(existingTemplate, getTemplateContent(params));
    }

    private void assertInternalStorageTemplate(final TerraformTemplate existingTemplate) {
        final TerraformTemplateStorageInfo storageInfo = existingTemplate.getStorageInfo();
        final TerraformTemplateStorageType templateStorageType = storageInfo.getStorageType();
        if (TerraformTemplateStorageType.INTERNAL_STORAGE != templateStorageType) {
            throw new IllegalStateException(
                    String.format(CHANGE_STORAGE_TYPE_ERROR_MESSAGE, existingTemplate.getName(), templateStorageType));
        }
    }

    private String getTemplateContent(final CreateOrUpdateTerraformTemplateParams params) {
        final CreateOrUpdateTerraformTemplateParams.StorageSpecificInfo storageSpecificInfo = params.getStorageInfo();
        if (storageSpecificInfo instanceof CreateOrUpdateTerraformTemplateParams.InternalStorageSpecificInfo) {
            return ((CreateOrUpdateTerraformTemplateParams.InternalStorageSpecificInfo) storageSpecificInfo).getTemplateContent();
        }
        throw new IllegalArgumentException("Got malformed terraform template params");
    }

    @Override
    protected void clearStorageContent(TerraformTemplate template) {
        try {
            internalStorageCaller.deleteTemplate(template);
        } catch (Exception e) {
            LOG.error("Failed to delete template {} directory for tenant {}. Reason: {}",
                    template.getName(), template.getTenantName(), e.getMessage(), e);
        }
    }

    @Override
    protected File downloadTemplateFromStorage(TerraformPipeLineContext context, File targetDirectory) {
        internalStorageCaller.downloadTemplate(context, targetDirectory);
        return targetDirectory;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public Collection<TerraformTemplateStorageType> getSupportedStorageTypes() {
        return Collections.singleton(TerraformTemplateStorageType.INTERNAL_STORAGE);
    }
}
