package io.maestro3.agent.terraform.sender;

import io.maestro3.agent.tf.integration.task.ExecuteTerraformTaskRequest;

public interface IRabbitMqTerraformMessageSender {
    void send(ExecuteTerraformTaskRequest terraformTaskRequest);
}
