package edu.kit.tm.ps.latte_mixxiato.gateway.relay;

import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.message.DestinationAndMessage;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.ClientList;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Packet;

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
            Logger.getGlobal().info("Opened socket on port %s".formatted(port));
            while (true) {
                final Queue<DestinationAndMessage> packetQueue;
                try(final var socket = serverSocket.accept()) {
                    Logger.getGlobal().info("Accepted Socket connection.");
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
                final var processedPacket = node.sphinxProcess(node.client().unpackMessage(packetBytes).packetContent());
                final var flag = processedPacket.routingFlag();
                if (flag.equals(RoutingFlag.DESTINATION)) {
                    final var destinationAndMessage = node.client()
                            .receiveForward(processedPacket.macKey(), processedPacket.packetContent().delta());
                    messageList.add(destinationAndMessage);
                } else {
                    Logger.getGlobal().warning("Received packet with wrong flag %s".formatted(flag));
                }
            } while (is.available() > 0);
        }
        Logger.getGlobal().info("Received %s packet(s)".formatted(messageList.size()));
        return messageList;
    }

    public void handle(Queue<DestinationAndMessage> messages) throws IOException {
        while (!messages.isEmpty()) {
            final var msg = messages.poll();
            assert msg != null;
            final var packet = Packet.parse(msg.message());
            final var clientData = clientList.pop();
            Logger.getGlobal().info("Opening Socket to client %s:%s".formatted(clientData.host(), clientData.port()));
            try (final var outgoingSocket = new Socket(clientData.host(), clientData.port())) {
                try (final var os = outgoingSocket.getOutputStream()) {
                    os.write(packet.toBytes());
                }
            }
        }
    }
}
