package io.maestro3.agent.terraform.sender.impl;

import io.maestro3.agent.terraform.sender.IRabbitMqTerraformMessageSender;
import io.maestro3.agent.tf.integration.task.ExecuteTerraformTaskRequest;
import io.maestro3.agent.tf.util.JsonUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqTerraformMessageSender implements IRabbitMqTerraformMessageSender {

    private final RabbitTemplate rabbitTemplate;
    private final String terraformExchange;
    private final String terraformQueue;

    @Autowired
    public RabbitMqTerraformMessageSender(RabbitTemplate rabbitTemplate,
                                          @Value("${rabbitmq.terraform.exchange}") String terraformExchange,
                                          @Value("${rabbitmq.terraform.queue}") String terraformQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.terraformExchange = terraformExchange;
        this.terraformQueue = terraformQueue;
    }

    @Override
    public void send(ExecuteTerraformTaskRequest terraformTaskRequest) {
        String taskMessage = JsonUtils.convertToJson(terraformTaskRequest);
        rabbitTemplate.convertAndSend(terraformExchange, terraformQueue, taskMessage);
    }
}
