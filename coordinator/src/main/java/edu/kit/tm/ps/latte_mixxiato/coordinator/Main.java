package edu.kit.tm.ps.latte_mixxiato.coordinator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.robertsoultanaev.javasphinx.SerializationUtils;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixType;
import org.eclipse.jetty.server.Response;
import spark.Request;
import spark.Spark;

import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        final var coordinator = new Coordinator();
        Spark.get("/api/mixes/all", (req, res) -> {
            final var result = new JsonArray();
            coordinator.all().stream()
                    .map(MixNode::toJson).toList()
                    .forEach(result::add);
            return result;
        });
        Spark.post("/api/gateway/register", (req, res) -> register(req, res, coordinator));
        Spark.post("/api/relay/register", (req, res) -> register(req, res, coordinator));
        Spark.post("/api/dead-drop/register", (req, res) -> register(req, res, coordinator));
        Logger.getGlobal().info("Listening for requests...");
    }

    private static Object register(Request req, spark.Response res, Coordinator coordinator) {
        if (req.body().isBlank()) {
            res.status(Response.SC_BAD_REQUEST);
            return "";
        }
        final var reqJson = JsonParser.parseString(req.body()).getAsJsonObject();
        final var mix = coordinator.register(
                MixType.values()[reqJson.get("type").getAsInt()],
                reqJson.get("host").getAsString(),
                reqJson.get("port").getAsInt(),
                SerializationUtils.decodeECPoint(SerializationUtils.base64decode(reqJson.get("pubKey").getAsString())));
        final var resBody = new JsonObject();
        resBody.addProperty("type", mix.type().ordinal());
        Logger.getGlobal().info("Registered mix %s on %s:%s with pubKey %s"
                .formatted(mix.type(), mix.host(), mix.port(), mix.publicKey()));
        return resBody;
    }
}