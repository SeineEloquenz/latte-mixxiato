package edu.kit.tm.ps.latte_mixxiato.coordinator;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.DeadDrop;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Relay;
import org.eclipse.jetty.server.Response;
import spark.Spark;

import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        final var coordinator = new Coordinator();
        Spark.get("/api/ready", (req, res) -> {
            final var body = new JsonObject();
            body.addProperty("ready", coordinator.ready());
            return body;
        });
        Spark.get("/api/gateway", (req, res) ->
                coordinator.gateway().map(Gateway::toJson).orElseGet(JsonObject::new));
        Spark.get("/api/relay", (req, res) ->
                coordinator.relay().map(Relay::toJson).orElseGet(JsonObject::new));
        Spark.get("/api/deadDrop", (req, res) ->
                coordinator.deadDrop().map(DeadDrop::toJson).orElseGet(JsonObject::new));

        Spark.post("/api/gateway/register", (req, res) -> {
            if (req.body().isBlank()) {
                res.status(Response.SC_BAD_REQUEST);
                return "";
            }
            final var reqJson = JsonParser.parseString(req.body()).getAsJsonObject();
            final var gateway = Gateway.fromJson(reqJson);
            coordinator.register(gateway);
            Logger.getGlobal().info("Registered gateway");
            return new JsonObject();
        });
        Spark.post("/api/relay/register", (req, res) -> {
            if (req.body().isBlank()) {
                res.status(Response.SC_BAD_REQUEST);
                return "";
            }
            final var reqJson = JsonParser.parseString(req.body()).getAsJsonObject();
            final var gateway = Relay.fromJson(reqJson);
            coordinator.register(gateway);
            Logger.getGlobal().info("Registered gateway");
            return new JsonObject();
        });
        Spark.post("/api/deadDrop/register", (req, res) -> {
            if (req.body().isBlank()) {
                res.status(Response.SC_BAD_REQUEST);
                return "";
            }
            final var reqJson = JsonParser.parseString(req.body()).getAsJsonObject();
            final var gateway = DeadDrop.fromJson(reqJson);
            coordinator.register(gateway);
            Logger.getGlobal().info("Registered gateway");
            return new JsonObject();
        });
        Logger.getGlobal().info("Listening for requests...");
    }
}