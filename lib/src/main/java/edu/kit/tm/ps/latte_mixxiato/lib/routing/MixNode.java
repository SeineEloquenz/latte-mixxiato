package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import com.google.gson.JsonObject;
import com.robertsoultanaev.javasphinx.SerializationUtils;
import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import org.bouncycastle.math.ec.ECPoint;

import java.io.IOException;
import java.net.Socket;

public record MixNode(int id, String host, int port, ECPoint publicKey) {

    public static MixNode fromJson(JsonObject json) {
        final var id = json.get("id").getAsInt();
        final var host = json.get("host").getAsString();
        final var port = json.get("port").getAsInt();
        final var pubKey = SerializationUtils.decodeECPoint(SerializationUtils.base64decode(json.get("pubKey").getAsString()));
        return new MixNode(id, host, port, pubKey);
    }

    /**
     * Sends a {@link SphinxPacket} via the given {@link SphinxClient}
     * @param client client to use for sending
     * @param packet packet to send
     * @throws IOException thrown if an error occurs opening the connection to the host
     */
    public void send(SphinxClient client, SphinxPacket packet) throws IOException {
        try (final var socket = new Socket(host, port)) {
            final var os = socket.getOutputStream();
            os.write(client.packMessage(packet));
        }
    }

    public JsonObject toJson() {
        final var json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("host", host);
        json.addProperty("port", port);
        json.addProperty("pubKey", SerializationUtils.base64encode(SerializationUtils.encodeECPoint(publicKey)));
        return json;
    }
}
