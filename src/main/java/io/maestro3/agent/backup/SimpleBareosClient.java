package io.maestro3.agent.backup;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class SimpleBareosClient {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleBareosClient.class);
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String url;
    private final String user;
    private final String password;
    private long lastAuthTimestamp;
    private String token;

    public SimpleBareosClient(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private String authenticate() throws Exception {
        if (System.currentTimeMillis() - lastAuthTimestamp > 15 * 60 * 1000) {//15 min
            String authUrl = url + "/token";
            String jsonRequestBody = String.format("grant_type=password&username=%s&password=%s", user, password);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<String, Object> responseMap = gson.fromJson(response.body(), HashMap.class);
                token = (String) responseMap.get("access_token");
                lastAuthTimestamp = System.currentTimeMillis();
                return token;
            } else {
                throw new RuntimeException("Authentication failed: " + response.body());
            }
        } else {
            return token;
        }
    }

    public boolean runBackupJob(String jobName) throws Exception {
        String runJobUrl = url + "/control/jobs/run";
        String requestBody = String.format("{ \"jobControl\": {\"job\": \"%s\"}}", jobName);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(runJobUrl))
                .header("Authorization", "Bearer " + authenticate())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return true;
        } else {
            LOG.error("Failed to start backup job. Reason: {}", response.body());
            return false;
        }
    }


    public String getJob(String jobName) throws Exception {
        String statusJobUrl = url + "/configuration/jobs/" + jobName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(statusJobUrl))
                .header("Authorization", "Bearer " + authenticate())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("Failed to get job status: " + response.body());
        }
    }
}
