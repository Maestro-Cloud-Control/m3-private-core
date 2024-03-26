package io.maestro3.agent.terraform.git.util;

import java.net.URI;
import java.net.URISyntaxException;

public class ParsedGitUrl {
    private final String protocol;
    private final String domain;
    private final String groupName;
    private final String repositoryName;

    public ParsedGitUrl(String protocol, String domain, String groupName, String repositoryName) {
        this.protocol = protocol;
        this.domain = domain;
        this.groupName = groupName;
        this.repositoryName = repositoryName;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getDomain() {
        return domain;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getFullRepositoryName() {
        return String.join("/", groupName, repositoryName);
    }

    public String getDomainUrl() throws URISyntaxException {
        return new URI(protocol, domain, null, null).toString();
    }
}
