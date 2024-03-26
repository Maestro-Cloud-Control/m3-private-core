package io.maestro3.agent.terraform.exception;

public class ConcurrentActionException extends RuntimeException {

    public ConcurrentActionException(String message) {
        super(message);
    }
}
