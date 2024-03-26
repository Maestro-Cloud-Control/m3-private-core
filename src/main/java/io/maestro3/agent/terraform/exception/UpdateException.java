package io.maestro3.agent.terraform.exception;

public class UpdateException extends RuntimeException {

    private final Reason reason;

    public UpdateException(Reason reason) {
        super(reason.message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        NOTHING_TO_UPDATE("Nothing to update");

        private final String message;

        Reason(String message) {
            this.message = message;
        }
    }
}
