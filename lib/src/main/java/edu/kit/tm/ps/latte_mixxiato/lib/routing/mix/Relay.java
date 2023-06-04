package edu.kit.tm.ps.latte_mixxiato.lib.routing.mix;

import com.google.gson.JsonObject;
import com.robertsoultanaev.javasphinx.SerializationUtils;
import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import org.bouncycastle.math.ec.ECPoint;

import java.io.IOException;
import java.net.Socket;

public record Relay(String host, int gatewayPort, int deadDropPort, ECPoint publicKey) {

    public static Relay fromJson(JsonObject json) {
        final var host = json.get("host").getAsString();
        final var gatewayPort = json.get("gatewayPort").getAsInt();
        final var deadDropPort = json.get("deadDropPort").getAsInt();
        final var pubKey = SerializationUtils.decodeECPoint(SerializationUtils.base64decode(json.get("pubKey").getAsString()));
        return new Relay(host, gatewayPort, deadDropPort, pubKey);
    }

    /**
     * Sends a {@link SphinxPacket} via the given {@link SphinxClient}
     * @param client client to use for sending
     * @param packet packet to send
     * @throws IOException thrown if an error occurs opening the connection to the host
     */
    public void send(SphinxClient client, SphinxPacket packet) throws IOException {
        try (final var socket = new Socket(host, gatewayPort)) {
            final var os = socket.getOutputStream();
            os.write(client.packMessage(packet));
        }
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
