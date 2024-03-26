package io.maestro3.agent.terraform.git.util;

import org.apache.http.util.Asserts;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GitUtils {

    private static final String URL_PATTERN = "^http(s)?://%s(\\.\\w+)?.com/";

    private GitUtils() {
        throw new UnsupportedOperationException("Class is not designed for an instantiation");
    }

    public static ParsedGitUrl parseUrl(final String url, final String provider) {
        Asserts.notBlank(url, "git URL");
        Asserts.notBlank(provider, "provider");
        Pattern urlPattern = Pattern.compile(String.format(URL_PATTERN, provider), Pattern.CASE_INSENSITIVE);
        Matcher matcher = urlPattern.matcher(url);
        if (!matcher.lookingAt()) {
            throw new IllegalArgumentException("Malformed git URL: " + url);
        }

        URI gitUri = URI.create(url);
        String path = gitUri.getPath();
        String[] splittedPath = path.split("/");
        if (splittedPath.length != 3) {
            throw new IllegalArgumentException("Malformed git URL: " + url);
        }

        String repoName = splittedPath[2];
        if (repoName.endsWith(".git")) {
            repoName = repoName.replaceAll("\\.git$", "");
        }

        return new ParsedGitUrl(gitUri.getScheme(), gitUri.getHost(), splittedPath[1], repoName);
    }
}
