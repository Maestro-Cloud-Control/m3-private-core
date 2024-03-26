package io.maestro3.agent.terraform.exception;

import java.net.HttpURLConnection;

public class ServerException extends RuntimeException {

    private static final int DEFAULT_SERVER_ERROR_CODE = HttpURLConnection.HTTP_INTERNAL_ERROR;

    private final int errorCode;

    public ServerException(Throwable throwable) {
        this("Unexpected error occurred", throwable);
    }

    public ServerException(String message, Throwable throwable) {
        super(message, throwable);
        this.errorCode = DEFAULT_SERVER_ERROR_CODE;
    }

    public ServerException(String message) {
        this(message, DEFAULT_SERVER_ERROR_CODE);
    }

    protected ServerException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
