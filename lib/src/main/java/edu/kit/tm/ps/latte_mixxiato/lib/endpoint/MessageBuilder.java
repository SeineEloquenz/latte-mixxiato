package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SerializationUtils;
import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
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

    private final BucketIdGenerator idGenerator;


    public MessageBuilder(final BucketIdGenerator idGenerator, final Gateway gateway, final Relay relay, final DeadDrop deadDrop, final SphinxClient client) {
        super(gateway, relay, deadDrop, client);
        this.idGenerator = idGenerator;
    }

    public List<Packet> splitIntoPackets(InwardMessage message) throws SphinxException {
        final var messageId = UUID.randomUUID();

        final var packetPayloadSize = client.getMaxPayloadSize() - DEST_LENGTH - Packet.HEADER_SIZE;
        final var packetsInMessage = (int) Math.ceil((double) message.message().length / packetPayloadSize);
        final var packets = new LinkedList<Packet>();

        for (int i = 0; i < packetsInMessage; i++) {
            final var bucketId = idGenerator.next();
            byte[] packetPayload = copyUpToNum(message.message(), packetPayloadSize * i, packetPayloadSize);
            final var packet = new Packet(messageId, bucketId, packetsInMessage, i, packetPayload);

            packets.add(packet);
        }

        return packets;
    }

    public Packet makeNoisePacket() {
        return new Packet(UUID.randomUUID(), idGenerator.next(), 1, 0, "<empty>".getBytes(StandardCharsets.UTF_8));
    }

    public SphinxPacket makeOnion(Packet packet) throws SphinxException {
        byte[] dest = SerializationUtils.encodeLong(UUID.randomUUID().getMostSignificantBits());
        return createSphinxPacket(dest, packet.toBytes(), inboundRoutingInformation());
    }
}
