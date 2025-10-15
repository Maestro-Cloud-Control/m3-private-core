package io.maestro3.agent.terraform.integration.context;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.springframework.stereotype.Component;
import team.syndicate.terraform.engine.management.interfaces.model.ContextExtensionDeserializer;
import team.syndicate.terraform.engine.terraform.integration.IContextExtension;

import java.io.IOException;

@Component
public class TerraformEngineContextExtensionDeserializer extends ContextExtensionDeserializer {

    @Override
    public IContextExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return jsonParser.getCodec().readValue(jsonParser, PaasTerraformPipeLineContextExtension.class);
    }
}
