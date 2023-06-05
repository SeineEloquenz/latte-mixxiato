package edu.kit.tm.ps.latte_mixxiato.gateway.relay;

import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.message.DestinationAndMessage;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.ClientData;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Packet;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Permuter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class RelayGateway {

    private final int port;
    private final List<ClientData> clientList;
    private final SphinxNode node;
    private final Permuter permuter;

    public RelayGateway(final int port, final List<ClientData> clientList, final SphinxNode node, final Permuter permuter) {
        this.port = port;
        this.clientList = clientList;
        this.node = node;
        this.permuter = permuter;
    }

    public void listen() throws IOException, SphinxException {
        try (final var serverSocket = new ServerSocket(port)) {
            LatteLogger.get().debug("Opened socket on port %s".formatted(port));
            while (true) {
                final List<DestinationAndMessage> reordered;
                try(final var socket = serverSocket.accept()) {
                    LatteLogger.get().debug("Accepted Socket connection.");
                    final var packetList = this.handleConnection(socket);
                    reordered = permuter.restoreOrder(packetList);
                }
                this.handle(reordered);
            }
        }
    }

    private List<DestinationAndMessage> handleConnection(Socket socket) throws IOException, SphinxException {
        final var messageList = new LinkedList<DestinationAndMessage>();
        try (final var is = socket.getInputStream()) {
            do {
                final var packetBytes = is.readNBytes(1254);
                if (packetBytes.length == 0) {
                    break;
                }
                final var processedPacket = node.sphinxProcess(node.client().unpackMessage(packetBytes).packetContent());
                final var flag = processedPacket.routingFlag();
                if (flag.equals(RoutingFlag.DESTINATION)) {
                    final var destinationAndMessage = node.client()
                            .receiveForward(processedPacket.macKey(), processedPacket.packetContent().delta());
                    messageList.add(destinationAndMessage);
                } else {
                    LatteLogger.get().warn("Received packet with wrong flag %s".formatted(flag));
                }
            } while (true);
        }
        LatteLogger.get().info("Received %s packet(s)".formatted(messageList.size()));
        return messageList;
    }

    public void handle(List<DestinationAndMessage> messages) throws IOException {
        for (final var msg : messages) {
            final var packet = Packet.parse(msg.message());
            final var clientData = clientList.get(0);
            clientList.remove(0);
            LatteLogger.get().debug("Opening Socket to client %s:%s".formatted(clientData.host(), clientData.port()));
            try (final var outgoingSocket = new Socket(clientData.host(), clientData.port())) {
                try (final var os = outgoingSocket.getOutputStream()) {
                    os.write(packet.toBytes());
                }
            }
        }
    }
}
