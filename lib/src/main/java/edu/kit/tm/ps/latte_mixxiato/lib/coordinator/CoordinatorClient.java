package edu.kit.tm.ps.latte_mixxiato.lib.coordinator;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CoordinatorClient {

    private final OkHttpClient client;
    private final String host;
    private final int port;

    public CoordinatorClient(CoordinatorConfig config) {
        this.client = new OkHttpClient();
        this.host = config.host();
        this.port = config.port();
    }

    public Set<MixNode> getAllMixes() throws IOException {
        final var request = new Request.Builder()
                .url("%s:%s/api/mixes/all".formatted(host, port))
                .build();
        final var mixes = new HashSet<MixNode>();
        try (final var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Call to coordinator was not successful. Error code %s".formatted(response.code()));
            } else {
                assert response.body() != null;
                final var mixArray = JsonParser.parseString(response.body().string()).getAsJsonArray();
                mixArray.asList().stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(MixNode::fromJson)
                        .forEach(mixes::add);
                return mixes;
            }
        }
    }
}
