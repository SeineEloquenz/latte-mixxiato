package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SerializationUtils;
import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.InwardMessage;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.DeadDrop;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Relay;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MessageBuilder extends Endpoint {

    private final BucketIdGenerator idGenerator;

    public MessageBuilder(final BucketIdGenerator idGenerator, final Gateway gateway, final Relay relay, final DeadDrop deadDrop, final SphinxClient client) {
        super(gateway, relay, deadDrop, client);
        this.idGenerator = idGenerator;
    }

    public List<SphinxPacket> splitIntoSphinxPackets(InwardMessage message) throws SphinxException {
        final var messageId = UUID.randomUUID();
        byte[] dest = SerializationUtils.encodeLong(UUID.randomUUID().getMostSignificantBits());

        final var packetPayloadSize = client.getMaxPayloadSize() - dest.length - Packet.HEADER_SIZE;
        final var packetsInMessage = (int) Math.ceil((double) message.message().length / packetPayloadSize);
        final var sphinxPackets = new LinkedList<SphinxPacket>();

        for (int i = 0; i < packetsInMessage; i++) {
            final var bucketId = idGenerator.next();
            byte[] packetPayload = copyUpToNum(message.message(), packetPayloadSize * i, packetPayloadSize);
            final var packet = new Packet(messageId, bucketId, packetsInMessage, i, packetPayload);

            sphinxPackets.add(createSphinxPacket(dest, packet.toBytes(), inboundRoutingInformation()));
        }

        return sphinxPackets;
    }
}
