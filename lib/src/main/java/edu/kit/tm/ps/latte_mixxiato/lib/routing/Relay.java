package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import com.google.gson.JsonObject;
import com.robertsoultanaev.javasphinx.SerializationUtils;
import org.bouncycastle.math.ec.ECPoint;

public record Relay(String host, int gatewayPort, int deadDropPort, ECPoint publicKey) {

    public static Relay fromJson(JsonObject json) {
        final var host = json.get("host").getAsString();
        final var gatewayPort = json.get("gatewayPort").getAsInt();
        final var deadDropPort = json.get("deadDropPort").getAsInt();
        final var pubKey = SerializationUtils.decodeECPoint(SerializationUtils.base64decode(json.get("pubKey").getAsString()));
        return new Relay(host, gatewayPort, deadDropPort, pubKey);
    }

    public JsonObject toJson() {
        final var json = new JsonObject();
        json.addProperty("host", host);
        json.addProperty("gatewayPort", gatewayPort);
        json.addProperty("deadDropPort", deadDropPort);
        json.addProperty("pubKey", SerializationUtils.base64encode(SerializationUtils.encodeECPoint(publicKey)));
        return json;
    }
}
