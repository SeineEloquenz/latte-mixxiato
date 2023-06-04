package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SerializationUtils;
import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import com.robertsoultanaev.javasphinx.packet.header.PacketContent;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.DeadDrop;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Relay;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.InwardMessage;
import org.bouncycastle.math.ec.ECPoint;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Endpoint {

    private record RoutingInformation(byte[][] nodesRouting, ECPoint[] nodeKeys, int firstNodeId) {
    }

    private final Gateway gateway;
    private final Relay relay;
    private final DeadDrop deadDrop;
    private final SphinxClient client;

    public Endpoint(Gateway gateway, final Relay relay, final DeadDrop deadDrop, SphinxClient client) {
        this.gateway = gateway;
        this.relay = relay;
        this.deadDrop = deadDrop;
        this.client = client;
    }

    public List<SphinxPacket> splitIntoSphinxPackets(InwardMessage message) {
        final var messageId = UUID.randomUUID();
        byte[] dest = SerializationUtils.encodeLong(UUID.randomUUID().getMostSignificantBits());

        final var packetPayloadSize = client.getMaxPayloadSize() - dest.length - Packet.HEADER_SIZE;
        final var packetsInMessage = (int) Math.ceil((double) message.message().length / packetPayloadSize);
        final var sphinxPackets = new LinkedList<SphinxPacket>();

        for (int i = 0; i < packetsInMessage; i++) {
            final var bucketId = UUID.randomUUID();

            final var packetHeader = ByteBuffer.allocate(Packet.HEADER_SIZE);
            packetHeader.putLong(messageId.getMostSignificantBits());
            packetHeader.putLong(messageId.getLeastSignificantBits());
            packetHeader.putLong(bucketId.getMostSignificantBits());
            packetHeader.putLong(bucketId.getLeastSignificantBits());
            packetHeader.putInt(packetsInMessage);
            packetHeader.putInt(i);

            byte[] packetPayload = copyUpToNum(message.message(), packetPayloadSize * i, packetPayloadSize);
            byte[] sphinxPayload = SerializationUtils.concatenate(packetHeader.array(), packetPayload);

            sphinxPackets.add(createSphinxPacket(dest, sphinxPayload, generateRoutingInformation()));
        }

        return sphinxPackets;
    }

    private SphinxPacket createSphinxPacket(byte[] dest, byte[] message, RoutingInformation routingInformation) {
        PacketContent packetContent = client.createForwardMessage(routingInformation.nodesRouting, routingInformation.nodeKeys, dest, message);
        return client.createPacket(packetContent);
    }

    private RoutingInformation generateRoutingInformation() {
        final byte[][] nodesRouting = new byte[3][];
        final ECPoint[] nodeKeys = new ECPoint[3];

        nodesRouting[0] = client.encodeNode(0, 0);
        nodeKeys[0] = gateway.publicKey();
        nodesRouting[1] = client.encodeNode(1, 0);
        nodeKeys[1] = relay.publicKey();
        nodesRouting[2] = client.encodeNode(2, 0);
        nodeKeys[2] = deadDrop.publicKey();
        return new RoutingInformation(nodesRouting, nodeKeys, 0);
    }

    private byte[] copyUpToNum(byte[] source, int offset, int numBytes) {
        if (offset + numBytes > source.length) {
            numBytes = source.length - offset;
        }

        byte[] result = new byte[numBytes];
        System.arraycopy(source, offset, result, 0, numBytes);

        return result;
    }
}
