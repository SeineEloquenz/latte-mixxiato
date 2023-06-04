package edu.kit.tm.ps.latte_mixxiato.dead_drop;

import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import com.robertsoultanaev.javasphinx.packet.message.DestinationAndMessage;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.DestinationEncoding;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class DeadDropServer {

    private final int myPort;
    private final String targetHost;
    private final int targetPort;
    private final SphinxNode node;

    public DeadDropServer(final int myPort, final String targetHost, final int targetPort, final SphinxNode node) {
        this.myPort = myPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.node = node;
    }

    public void listen() throws IOException {
        try (final var serverSocket = new ServerSocket(myPort)) {
            Logger.getGlobal().info("Opened socket on port %s".formatted(myPort));
            while (true) {
                try(final var socket = serverSocket.accept()) {
                    Logger.getGlobal().info("Accepted Socket connection.");
                    final var packetList = this.handleConnection(socket);
                    this.handle(packetList);
                }
            }
        }
    }

    private List<DestinationAndMessage> handleConnection(Socket socket) throws IOException {
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

    public void handle(List<DestinationAndMessage> messages) throws IOException {
        for (final var msg : messages) {
            Logger.getGlobal().info("Received message to %s".formatted(DestinationEncoding.decode(msg.destination())));
        }
    }
}
