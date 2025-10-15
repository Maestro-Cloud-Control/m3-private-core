package io.maestro3.agent.terraform.variable;

public class ServerVariableResolvingContext {
    private final String requester;

    public ServerVariableResolvingContext(String requester) {
        this.requester = requester;
    }

    public String getRequester() {
        return requester;
    }
}
