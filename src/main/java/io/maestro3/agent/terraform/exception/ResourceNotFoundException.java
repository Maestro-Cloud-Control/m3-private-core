package io.maestro3.agent.terraform.exception;

import java.net.HttpURLConnection;

public class ResourceNotFoundException extends ServerException {

    private static final String DEFAULT_ERROR_MESSAGE = "Resource not found";
    private static final String DETAILED_ERROR_MESSAGE = "%s %s not found";

    public ResourceNotFoundException() {
        this(DEFAULT_ERROR_MESSAGE);
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpURLConnection.HTTP_NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        this(String.format(DETAILED_ERROR_MESSAGE, resourceType, identifier));
    }

    public static ResourceNotFoundException tenant(String identifier) {
        return new ResourceNotFoundException(String.format(DETAILED_ERROR_MESSAGE, "Tenant", identifier));
    }

    public static ResourceNotFoundException region(String identifier) {
        return new ResourceNotFoundException(String.format(DETAILED_ERROR_MESSAGE, "Region", identifier));
    }
}
