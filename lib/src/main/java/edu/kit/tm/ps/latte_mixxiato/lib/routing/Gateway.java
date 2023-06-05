package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import com.google.gson.JsonObject;
import com.robertsoultanaev.javasphinx.SerializationUtils;
import org.bouncycastle.math.ec.ECPoint;

public record Gateway(String host, int clientPort, int relayPort, ECPoint publicKey) {

    public static Gateway fromJson(JsonObject json) {
        final var host = json.get("host").getAsString();
        final var clientPort = json.get("clientPort").getAsInt();
        final var relayPort = json.get("relayPort").getAsInt();
        final var pubKey = SerializationUtils.decodeECPoint(SerializationUtils.base64decode(json.get("pubKey").getAsString()));
        return new Gateway(host, clientPort, relayPort, pubKey);
    }

    public JsonObject toJson() {
        final var json = new JsonObject();
        json.addProperty("host", host);
        json.addProperty("clientPort", clientPort);
        json.addProperty("relayPort", relayPort);
        json.addProperty("pubKey", SerializationUtils.base64encode(SerializationUtils.encodeECPoint(publicKey)));
        return json;
    }
}