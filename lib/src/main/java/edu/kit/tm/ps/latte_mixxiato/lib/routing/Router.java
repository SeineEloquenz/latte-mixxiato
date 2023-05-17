package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.packet.ProcessedPacket;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import org.msgpack.core.MessagePack;

import java.io.IOException;

public class Router {

    private final MixNodeRepository repository;
    private final SphinxClient client;

    public Router(final MixNodeRepository repository, final SphinxClient client) {
        this.repository = repository;
        this.client = client;
    }

    public RelayInformation findRelay(ProcessedPacket packet) throws IOException {
        final var unpacker = MessagePack.newDefaultUnpacker(packet.routing());
        final var routingLength = unpacker.unpackArrayHeader();
        final var flag = unpacker.unpackString();
        if (!RoutingFlag.RELAY.value().equals(flag)) {
            throw new SphinxException("Packet should not be relayed!");
        }
        final var delay = unpacker.unpackInt();
        final var nextNodeId = unpacker.unpackInt();
        unpacker.close();
        return new RelayInformation(repository.byId(nextNodeId), delay);
    }

    public OutwardMessage findForwardDestination(ProcessedPacket packet) throws IOException {
        final var unpacker = MessagePack.newDefaultUnpacker(packet.routing());
        final var routingLength = unpacker.unpackArrayHeader();
        final var flag = unpacker.unpackString();
        if (!RoutingFlag.DESTINATION.value().equals(flag)) {
            throw new SphinxException("Packet should not be forwarded!");
        }
        unpacker.close();
        final var destinationAndMessage = client.receiveForward(packet.macKey(), packet.packetContent().delta());
        return new OutwardMessage(DestinationEncoding.decode(destinationAndMessage.destination()), destinationAndMessage.message());
    }
}
