package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import com.robertsoultanaev.javasphinx.packet.header.PacketContent;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.DeadDrop;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Relay;
import org.bouncycastle.math.ec.ECPoint;

public abstract class Endpoint {

    private record RoutingInformation(byte[][] nodesRouting, ECPoint[] nodeKeys, int firstNodeId) {
    }
    private final Gateway gateway;
    private final Relay relay;
    private final DeadDrop deadDrop;
    protected final SphinxClient client;

    public Endpoint(Gateway gateway, final Relay relay, final DeadDrop deadDrop, SphinxClient client) {
        this.gateway = gateway;
        this.relay = relay;
        this.deadDrop = deadDrop;
        this.client = client;
    }

    protected SphinxPacket createSphinxPacket(byte[] dest, byte[] message, Endpoint.RoutingInformation routingInformation) throws SphinxException {
        PacketContent packetContent = client.createForwardMessage(routingInformation.nodesRouting, routingInformation.nodeKeys, dest, message);
        return client.createPacket(packetContent);
    }

    protected RoutingInformation inboundRoutingInformation() throws SphinxException {
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

    protected RoutingInformation outboundRoutingInformation() throws SphinxException {
        final byte[][] nodesRouting = new byte[2][];
        final ECPoint[] nodeKeys = new ECPoint[2];

        nodesRouting[1] = client.encodeNode(0, 0);
        nodeKeys[1] = gateway.publicKey();
        nodesRouting[0] = client.encodeNode(1, 0);
        nodeKeys[0] = relay.publicKey();
        return new RoutingInformation(nodesRouting, nodeKeys, 1);
    }

    protected byte[] copyUpToNum(byte[] source, int offset, int numBytes) {
        if (offset + numBytes > source.length) {
            numBytes = source.length - offset;
        }

        byte[] result = new byte[numBytes];
        System.arraycopy(source, offset, result, 0, numBytes);

        return result;
    }
}
