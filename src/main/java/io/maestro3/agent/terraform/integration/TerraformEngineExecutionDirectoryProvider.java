package io.maestro3.agent.terraform.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.management.interfaces.service.IExecutionDirectoryProvider;

import java.util.Optional;

@Service
public class TerraformEngineExecutionDirectoryProvider implements IExecutionDirectoryProvider {

    private final String baseDir;

    @Autowired
    public TerraformEngineExecutionDirectoryProvider(
            @Value("${terraform.base.dir:terraform}") String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public Optional<String> getBaseExecutionDirectory() {
        return Optional.of(baseDir);
    }
}
