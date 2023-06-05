package edu.kit.tm.ps.latte_mixxiato.lib.coordinator;

import com.google.gson.JsonParser;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.DeadDrop;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Relay;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class CoordinatorClient {

    private final OkHttpClient client;
    private final String apiEndpoint;

    public CoordinatorClient(CoordinatorConfig config) {
        this.client = new OkHttpClient();
        this.apiEndpoint = "%s:%s/api".formatted(config.host(), config.port());
    }

    public Gateway gateway() throws IOException {
        final var request = buildRequest("/gateway")
                .build();
        try (final var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Call to coordinator was not successful. Error code %s".formatted(response.code()));
            } else {
                assert response.body() != null;
                final var body = JsonParser.parseString(response.body().string()).getAsJsonObject();
                return Gateway.fromJson(body);
            }
        }
    }

    public Relay relay() throws IOException {
        final var request = buildRequest("/relay")
                .build();
        try (final var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Call to coordinator was not successful. Error code %s".formatted(response.code()));
            } else {
                assert response.body() != null;
                final var body = JsonParser.parseString(response.body().string()).getAsJsonObject();
                return Relay.fromJson(body);
            }
        }
    }

    public DeadDrop deadDrop() throws IOException {
        final var request = buildRequest("/deadDrop")
                .build();
        try (final var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Call to coordinator was not successful. Error code %s".formatted(response.code()));
            } else {
                assert response.body() != null;
                final var body = JsonParser.parseString(response.body().string()).getAsJsonObject();
                return DeadDrop.fromJson(body);
            }
        }
    }

    public void register(Gateway gateway) throws IOException {
        final var json = gateway.toJson();
        final var request = buildRequest("/gateway/register")
                .method("POST", RequestBody.create(json.toString(), MediaType.get("application/json")))
                .build();
        try (final var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Registration was unsuccessful. Error code %s".formatted(response.code()));
            }
            LatteLogger.get().info("Registered with coordinator");
        }
    }

    public void register(Relay relay) throws IOException {
        final var json = relay.toJson();
        final var request = buildRequest("/relay/register")
                .method("POST", RequestBody.create(json.toString(), MediaType.get("application/json")))
                .build();
        try (final var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Registration was unsuccessful. Error code %s".formatted(response.code()));
            }
            LatteLogger.get().info("Registered with coordinator");
        }
    }

    public void register(DeadDrop deadDrop) throws IOException {
        final var json = deadDrop.toJson();
        final var request = buildRequest("/deadDrop/register")
                .method("POST", RequestBody.create(json.toString(), MediaType.get("application/json")))
                .build();
        try (final var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Registration was unsuccessful. Error code %s".formatted(response.code()));
            }
        }
        LatteLogger.get().info("Registered with coordinator");
    }

    public void waitUntilReady() throws IOException {
        //TODO do this in a less bad way
        while (!this.ready()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        LatteLogger.get().info("Coordinator is ready.");
    }

    private boolean ready() throws IOException {
        final var request = buildRequest("/ready")
                .build();
        try (final var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Call to coordinator was not successful. Error code %s".formatted(response.code()));
            } else {
                assert response.body() != null;
                final var body = JsonParser.parseString(response.body().string()).getAsJsonObject();
                return body.get("ready").getAsBoolean();
            }
        }
    }

    private Request.Builder buildRequest(String path) {
        return new Request.Builder()
                .url(apiEndpoint + "/" + path);
    }
}
