package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SerializationUtils;
import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import com.robertsoultanaev.javasphinx.packet.header.PacketContent;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.DestinationEncoding;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNodeRepository;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixType;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.OutwardMessage;
import org.bouncycastle.math.ec.ECPoint;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Endpoint {

    public static int PACKET_HEADER_SIZE = 24;

    private record RoutingInformation(byte[][] nodesRouting, ECPoint[] nodeKeys, int firstNodeId) {
    }

    private final SphinxClient client;
    private final MixNodeRepository mixNodeRepository;
    private final int numRouteNodes;

    public Endpoint(MixNodeRepository mixNodeRepository, int numRouteNodes, SphinxClient client) {
        this.mixNodeRepository = mixNodeRepository;
        this.client = client;
        this.numRouteNodes = numRouteNodes;
    }

    public Map<SphinxPacket, MixNode> splitIntoSphinxPackets(OutwardMessage message) {
        UUID messageId = UUID.randomUUID();
        byte[] dest = DestinationEncoding.encode(message.address());

        final var packetPayloadSize = client.getMaxPayloadSize() - dest.length - PACKET_HEADER_SIZE;
        final var packetsInMessage = (int) Math.ceil((double) message.message().length / packetPayloadSize);
        final var sphinxPackets = new HashMap<SphinxPacket, MixNode>();

        for (int i = 0; i < packetsInMessage; i++) {
            final var packetHeader = ByteBuffer.allocate(Endpoint.PACKET_HEADER_SIZE);
            packetHeader.putLong(messageId.getMostSignificantBits());
            packetHeader.putLong(messageId.getLeastSignificantBits());
            packetHeader.putInt(packetsInMessage);
            packetHeader.putInt(i);

            byte[] packetPayload = copyUpToNum(message.message(), packetPayloadSize * i, packetPayloadSize);
            byte[] sphinxPayload = SerializationUtils.concatenate(packetHeader.array(), packetPayload);

            RoutingInformation routingInformation = generateRoutingInformation(this.numRouteNodes, generateDelays(numRouteNodes));

            final var targetMix = mixNodeRepository.byType(MixType.GATEWAY);
            sphinxPackets.put(createSphinxPacket(dest, sphinxPayload, routingInformation), targetMix);
        }

        return sphinxPackets;
    }

    private int[] generateDelays(int numRouteNodes) {
        final var rand = new Random();
        final var delays = new int[numRouteNodes];
        for (int i = 0; i < delays.length; i++) {
            delays[i] = rand.nextInt(10000);
        }
        return delays;
    }

    private SphinxPacket createSphinxPacket(byte[] dest, byte[] message, RoutingInformation routingInformation) {
        PacketContent packetContent = client.createForwardMessage(routingInformation.nodesRouting, routingInformation.nodeKeys, dest, message);
        return client.createPacket(packetContent);
    }

    private RoutingInformation generateRoutingInformation(int numRouteNodes, int[] delays) {
        assert delays.length == numRouteNodes;
        final byte[][] nodesRouting;
        final ECPoint[] nodeKeys;

        final var nodePool = mixNodeRepository.all().stream()
                .sorted(Comparator.comparingInt(node -> node.type().ordinal()))
                .mapToInt(node -> node.type().ordinal())
                .toArray();
        int[] usedNodes = client.route(nodePool, numRouteNodes);

        nodesRouting = new byte[usedNodes.length][];
        for (int i = 0; i < usedNodes.length; i++) {
            nodesRouting[i] = client.encodeNode(usedNodes[i], delays[i]);
        }

        nodeKeys = new ECPoint[usedNodes.length];
        for (int i = 0; i < usedNodes.length; i++) {
            nodeKeys[i] = mixNodeRepository.byType(MixType.values()[usedNodes[i]]).publicKey();
        }

        return new RoutingInformation(nodesRouting, nodeKeys, usedNodes[0]);
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
