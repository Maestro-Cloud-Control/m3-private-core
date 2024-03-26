package io.maestro3.agent.amqp.listener;

import io.maestro3.agent.terraform.manager.ITerraformTemplateService;
import io.maestro3.agent.tf.executor.task.ITerraformTaskExecutor;
import io.maestro3.agent.tf.executor.task.provider.TerraformTaskExecutorProvider;
import io.maestro3.agent.tf.integration.task.ExecuteTerraformTaskRequest;
import io.maestro3.agent.tf.integration.task.TerraformTask;
import io.maestro3.sdk.internal.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqTerraformMessageReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMqTerraformMessageReceiver.class);

    private final ITerraformTemplateService templateService;
    private final TerraformTaskExecutorProvider executorProvider;

    @Autowired
    public RabbitMqTerraformMessageReceiver(ITerraformTemplateService templateService,
                                            TerraformTaskExecutorProvider executorProvider) {
        this.templateService = templateService;
        this.executorProvider = executorProvider;
    }

    @RabbitListener(
            id = "terraform-task",
            queues = "${rabbitmq.terraform.queue}",
            containerFactory = "terraformListenerContainer",
            group = "rabbitReceivers"
    )
    public void receiveTerraformTask(String message) {
        try {
            ExecuteTerraformTaskRequest terraformTaskRequest = JsonUtils.parseJson(message, ExecuteTerraformTaskRequest.class);
            TerraformTask task = terraformTaskRequest.getTask();
            ITerraformTaskExecutor executor = executorProvider.provideExecutor(task);
            if (executor == null) {
                LOG.error("Executor for task {} not found", task);
                return;
            }

            String templateId = terraformTaskRequest.getTemplateId();
            try {
                executor.executeTask(terraformTaskRequest);
            } catch (Exception e) {
                LOG.error("Failed to process request", e);
            } finally {
                templateService.removeTask(templateId, terraformTaskRequest.getId());
            }
        } catch (Exception ex) {
            LOG.error("Failed to process request", ex);
        }
    }
}
