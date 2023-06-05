package edu.kit.tm.ps.latte_mixxiato.gateway.relay;

import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.message.DestinationAndMessage;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.ClientList;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Packet;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class RelayGateway {

    private final int port;
    private final ClientList clientList;
    private final SphinxNode node;

    public RelayGateway(final int port, final ClientList clientList, final SphinxNode node) {
        this.port = port;
        this.clientList = clientList;
        this.node = node;
    }

    public void listen() throws IOException, SphinxException {
        try (final var serverSocket = new ServerSocket(port)) {
            LatteLogger.get().debug("Opened socket on port %s".formatted(port));
            while (true) {
                final Queue<DestinationAndMessage> packetQueue;
                try(final var socket = serverSocket.accept()) {
                    LatteLogger.get().debug("Accepted Socket connection.");
                    packetQueue = this.handleConnection(socket);
                }
                this.handle(packetQueue);
            }
        }
    }

    private Queue<DestinationAndMessage> handleConnection(Socket socket) throws IOException, SphinxException {
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

    public void handle(Queue<DestinationAndMessage> messages) throws IOException {
        while (!messages.isEmpty()) {
            final var msg = messages.poll();
            assert msg != null;
            final var packet = Packet.parse(msg.message());
            final var clientData = clientList.pop();
            LatteLogger.get().debug("Opening Socket to client %s:%s".formatted(clientData.host(), clientData.port()));
            try (final var outgoingSocket = new Socket(clientData.host(), clientData.port())) {
                try (final var os = outgoingSocket.getOutputStream()) {
                    os.write(packet.toBytes());
                }
            }
        }
    }
}
