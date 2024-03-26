package io.maestro3.agent.terraform.model;

public class DeleteTerraformTemplateResult {
    private final boolean success;
    private final String message;

    public DeleteTerraformTemplateResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
