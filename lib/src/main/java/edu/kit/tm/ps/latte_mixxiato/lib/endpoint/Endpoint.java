package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SerializationUtils;
import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import com.robertsoultanaev.javasphinx.packet.header.PacketContent;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.DeadDrop;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Relay;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.InwardMessage;
import org.bouncycastle.math.ec.ECPoint;

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

    public List<SphinxPacket> splitIntoSphinxPackets(InwardMessage message) throws SphinxException {
        final var messageId = UUID.randomUUID();
        byte[] dest = SerializationUtils.encodeLong(UUID.randomUUID().getMostSignificantBits());

        final var packetPayloadSize = client.getMaxPayloadSize() - dest.length - Packet.HEADER_SIZE;
        final var packetsInMessage = (int) Math.ceil((double) message.message().length / packetPayloadSize);
        final var sphinxPackets = new LinkedList<SphinxPacket>();

        for (int i = 0; i < packetsInMessage; i++) {
            final var bucketId = UUID.randomUUID();
            byte[] packetPayload = copyUpToNum(message.message(), packetPayloadSize * i, packetPayloadSize);
            final var packet = new Packet(messageId, bucketId, packetsInMessage, i, packetPayload);

            sphinxPackets.add(createSphinxPacket(dest, packet.toBytes(), inboundRoutingInformation()));
        }

        return sphinxPackets;
    }

    public SphinxPacket repackReply(Packet packet) throws SphinxException {
        byte[] dest = SerializationUtils.encodeLong(UUID.randomUUID().getMostSignificantBits());
        return createSphinxPacket(dest, packet.toBytes(), outboundRoutingInformation());
    }

    private SphinxPacket createSphinxPacket(byte[] dest, byte[] message, RoutingInformation routingInformation) throws SphinxException {
        PacketContent packetContent = client.createForwardMessage(routingInformation.nodesRouting, routingInformation.nodeKeys, dest, message);
        return client.createPacket(packetContent);
    }

    private RoutingInformation inboundRoutingInformation() throws SphinxException {
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

    private RoutingInformation outboundRoutingInformation() throws SphinxException {
        final byte[][] nodesRouting = new byte[2][];
        final ECPoint[] nodeKeys = new ECPoint[2];

        nodesRouting[1] = client.encodeNode(0, 0);
        nodeKeys[1] = gateway.publicKey();
        nodesRouting[0] = client.encodeNode(1, 0);
        nodeKeys[0] = relay.publicKey();
        return new RoutingInformation(nodesRouting, nodeKeys, 1);
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
