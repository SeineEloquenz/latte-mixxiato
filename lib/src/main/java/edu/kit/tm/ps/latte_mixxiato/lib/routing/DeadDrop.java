package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import com.google.gson.JsonObject;
import com.robertsoultanaev.javasphinx.SerializationUtils;
import org.bouncycastle.math.ec.ECPoint;

public record DeadDrop(String host, int relayPort, ECPoint publicKey) {

    public static DeadDrop fromJson(JsonObject json) {
        final var host = json.get("host").getAsString();
        final var port = json.get("gatewayPort").getAsInt();
        final var pubKey = SerializationUtils.decodeECPoint(SerializationUtils.base64decode(json.get("pubKey").getAsString()));
        return new DeadDrop(host, port, pubKey);
    }

    public JsonObject toJson() {
        final var json = new JsonObject();
        json.addProperty("host", host);
        json.addProperty("gatewayPort", relayPort);
        json.addProperty("pubKey", SerializationUtils.base64encode(SerializationUtils.encodeECPoint(publicKey)));
        return json;
    }
}