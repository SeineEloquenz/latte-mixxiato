package edu.kit.tm.ps.latte_mixxiato.lib.coordinator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.robertsoultanaev.javasphinx.SerializationUtils;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.InMemoryMixNodeRepository;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNodeRepository;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.bouncycastle.math.ec.ECPoint;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CoordinatorClient {

    private final OkHttpClient client;
    private final String apiEndpoint;

    public CoordinatorClient(CoordinatorConfig config) {
        this.client = new OkHttpClient();
        this.apiEndpoint = "%s:%s/api".formatted(config.host(), config.port());
    }

    public Set<MixNode> getAllMixes() throws IOException {
        final var request = buildRequest("/mixes/all")
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

    public int register(String host, int port, ECPoint pubKey) throws IOException {
        final var json = new JsonObject();
        json.addProperty("host", host);
        json.addProperty("port", port);
        json.addProperty("pubKey", SerializationUtils.base64encode(SerializationUtils.encodeECPoint(pubKey)));
        final var request = buildRequest("/mixes/register")
                .method("POST", RequestBody.create(json.toString(), MediaType.get("application/json")))
                .build();
        try (final var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Registration was unsuccessful. Error code %s".formatted(response.code()));
            }
            assert response.body() != null;
            return JsonParser.parseString(response.body().string()).getAsJsonObject().get("id").getAsInt();
        }

    }

    public MixNodeRepository waitForMixes() throws IOException {
        final var repository = new InMemoryMixNodeRepository();
        var mixes = this.getAllMixes();
        //TODO do this in a less bad way
        while (mixes.size() != MixNodeRepository.DESIRED_MIX_AMOUNT) {
            mixes = this.getAllMixes();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        mixes.forEach(repository::put);
        return repository;
    }

    private Request.Builder buildRequest(String path) {
        return new Request.Builder()
                .url(apiEndpoint + "/" + path);
    }
}
