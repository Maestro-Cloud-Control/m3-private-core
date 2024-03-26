package io.maestro3.agent.terraform.exception;

import io.maestro3.sdk.internal.util.StringUtils;

import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.Optional;

public class BadRequestException extends ServerException {

    private static final String DEFAULT_BAD_REQUEST_ERROR_MESSAGE = "Bad request";

    public BadRequestException() {
        this(DEFAULT_BAD_REQUEST_ERROR_MESSAGE);
    }

    public BadRequestException(String message) {
        super(message, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    public static void checkNotBlank(String paramValue, String paramName) {
        if (StringUtils.isBlank(paramValue)) {
            throw new BadRequestException(String.format("Parameter %s can not be null or empty", paramName));
        }
    }

    public static void checkNotNull(Object value, String paramName) {
        if (value == null) {
            throw new BadRequestException(String.format("Parameter %s can not be null", paramName));
        }
    }

    public static void checkOneSpecified(String paramValue, boolean flagValue, String message) {
        if (StringUtils.isNotBlank(paramValue) == flagValue) {
            throw new BadRequestException(message);
        }
    }

    public static void checkEqual(Object first, Object second, String message) {
        if (!Objects.equals(first, second)) {
            throw new BadRequestException(message);
        }
    }

    public static void checkUnsupportedParam(Object paramValue, String paramName) {
        if (paramValue != null) {
            throw new BadRequestException(String.format("Parameter %s not supported", paramName));
        }
    }

    public static void checkUnsupportedParam(Boolean paramValue, String paramName) {
        if (Optional.ofNullable(paramValue).orElse(false)) {
            throw new BadRequestException(String.format("Flag parameter %s not supported", paramName));
        }
    }
}
