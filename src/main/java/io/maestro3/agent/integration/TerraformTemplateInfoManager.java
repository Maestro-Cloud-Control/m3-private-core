package io.maestro3.agent.integration;

import io.maestro3.agent.terraform.manager.ITerraformTemplateService;
import io.maestro3.agent.terraform.model.TerraformTemplateInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.integration.manager.ITerraformTemplateInfoManager;
import team.syndicate.terraform.integration.model.ITerraformTemplateInfo;
import team.syndicate.terraform.integration.model.TemplateStatus;

import java.util.Optional;
import java.util.Set;

@Service
public class TerraformTemplateInfoManager implements ITerraformTemplateInfoManager {

    private final ITerraformTemplateService templateService;

    @Autowired
    public TerraformTemplateInfoManager(ITerraformTemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public Optional<ITerraformTemplateInfo> getTemplateInfo(String tenantName, String templateName) {
        return templateService.find(tenantName, templateName)
                .map(TerraformTemplateInfoWrapper::new);
    }

    @Override
    public void updateTemplateStatus(ITerraformTemplateInfo template, TemplateStatus newStatus) {
        if (template == null) {
            return;
        }
        if (template.getStatus() == newStatus) {
            return;
        }

        templateService.updateTemplateStatus(template.getTemplateId(), newStatus);
        template.setStatus(newStatus);
    }

    @Override
    public void updateTemplateProviders(ITerraformTemplateInfo template, Set<String> newProviders) {
        if (template == null) {
            return;
        }
        if (newProviders.equals(template.getProviders())) {
            return;
        }

        templateService.updateTemplateProviders(template.getTemplateId(), newProviders);
        template.setProviders(newProviders);
    }
}
