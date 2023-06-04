package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SerializationUtils;
import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import com.robertsoultanaev.javasphinx.packet.header.PacketContent;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.DestinationEncoding;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.DeadDrop;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Relay;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.OutwardMessage;
import org.bouncycastle.math.ec.ECPoint;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Endpoint {

    public static int PACKET_HEADER_SIZE = 24;

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

    public List<SphinxPacket> splitIntoSphinxPackets(OutwardMessage message) {
        UUID messageId = UUID.randomUUID();
        byte[] dest = DestinationEncoding.encode(message.address());

        final var packetPayloadSize = client.getMaxPayloadSize() - dest.length - PACKET_HEADER_SIZE;
        final var packetsInMessage = (int) Math.ceil((double) message.message().length / packetPayloadSize);
        final var sphinxPackets = new LinkedList<SphinxPacket>();

        for (int i = 0; i < packetsInMessage; i++) {
            final var packetHeader = ByteBuffer.allocate(Endpoint.PACKET_HEADER_SIZE);
            packetHeader.putLong(messageId.getMostSignificantBits());
            packetHeader.putLong(messageId.getLeastSignificantBits());
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

    public Packet parseMessageToPacket(byte[] message) {
        byte[] headerBytes = Arrays.copyOfRange(message, 0, Endpoint.PACKET_HEADER_SIZE);
        ByteBuffer byteBuffer = ByteBuffer.wrap(headerBytes);
        long uuidHigh = byteBuffer.getLong();
        long uuidLow = byteBuffer.getLong();

        int packetsInMessage = byteBuffer.getInt();
        int sequenceNumber = byteBuffer.getInt();
        final var uuid = new UUID(uuidHigh, uuidLow);
        byte[] payload = Arrays.copyOfRange(message, 24, message.length);

        return new Packet(uuid, sequenceNumber, packetsInMessage, payload);
    }
}
