package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SerializationUtils;
import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.packet.Packet;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.packet.UnaddressedPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.DeadDrop;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.InwardMessage;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Relay;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MessageBuilder extends Endpoint {

    private static final int DEST_LENGTH = Long.BYTES;

    private final int replyPort;


    public MessageBuilder(final Gateway gateway, final Relay relay, final DeadDrop deadDrop, final SphinxClient client, final int replyPort) {
        super(gateway, relay, deadDrop, client);
        this.replyPort = replyPort;
    }

    public List<UnaddressedPacket> splitIntoPackets(InwardMessage message) throws SphinxException {
        final var messageId = UUID.randomUUID();

        final var packetPayloadSize = client.getMaxPayloadSize() - DEST_LENGTH - Packet.HEADER_SIZE;
        final var packetsInMessage = (int) Math.ceil((double) message.message().length / packetPayloadSize);
        final var packets = new LinkedList<UnaddressedPacket>();

        for (int i = 0; i < packetsInMessage; i++) {
            byte[] packetPayload = copyUpToNum(message.message(), packetPayloadSize * i, packetPayloadSize);
            final var packet = new UnaddressedPacket(messageId, packetsInMessage, i, packetPayload);

            packets.add(packet);
        }

        return packets;
    }

    public UnaddressedPacket makeNoisePacket() {
        return new UnaddressedPacket(UUID.randomUUID(), 1, 0, "<empty>".getBytes(StandardCharsets.UTF_8));
    }

    public SphinxPacket makeOnion(Packet packet) throws SphinxException {
        byte[] dest = SerializationUtils.encodeLong(UUID.randomUUID().getMostSignificantBits());
        return createSphinxPacket(dest, packet.toBytes(), inboundRoutingInformation(replyPort));
    }
}
