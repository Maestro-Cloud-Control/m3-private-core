package io.maestro3.agent.terraform.git.exception;

public class GitResourceNotFoundException extends GitException {

    public GitResourceNotFoundException(String message) {
        super(message);
    }
}
