package edu.kit.tm.ps.latte_mixxiato.coordinator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.robertsoultanaev.javasphinx.SerializationUtils;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.InMemoryMixNodeRepository;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import org.eclipse.jetty.server.Response;
import spark.Spark;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        final var coordinator = new Coordinator();
        Logger.getGlobal().info("Loaded mix data from disk.");
        Spark.get("/api/mixes/all", (req, res) -> {
            final var result = new JsonArray();
            coordinator.all().stream()
                    .map(MixNode::toJson).toList()
                    .forEach(result::add);
            return result;
        });
        Spark.post("/api/register", (req, res) -> {
            if (req.body().isBlank()) {
                res.status(Response.SC_BAD_REQUEST);
                return "";
            }
            final var reqJson = JsonParser.parseString(req.body()).getAsJsonObject();
            final var mix = coordinator.register(reqJson.get("host").getAsString(), reqJson.get("port").getAsInt(),
                    SerializationUtils.decodeECPoint(SerializationUtils.base64decode(reqJson.get("pubKey").getAsString())));
            final var resBody = new JsonObject();
            resBody.addProperty("id", mix.id());
            Logger.getGlobal().info("Registered mix %s on %s:%s with pubKey %s"
                    .formatted(mix.id(), mix.host(), mix.port(), mix.publicKey()));
            return resBody;
        });
        Logger.getGlobal().info("Listening for requests...");
    }
}