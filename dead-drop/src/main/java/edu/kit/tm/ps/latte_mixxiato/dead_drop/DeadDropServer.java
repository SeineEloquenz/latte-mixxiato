package edu.kit.tm.ps.latte_mixxiato.dead_drop;

import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.message.DestinationAndMessage;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Endpoint;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Packet;

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
    private final Endpoint endpoint;
    private final SphinxNode node;
    private final Store store;

    public DeadDropServer(final int myPort, final String targetHost, final int targetPort, final Endpoint endpoint, final SphinxNode node) {
        this.myPort = myPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.endpoint = endpoint;
        this.node = node;
        this.store = new Store();
    }

    public void listen() throws IOException, SphinxException {
        try (final var serverSocket = new ServerSocket(myPort)) {
            Logger.getGlobal().info("Opened socket on port %s".formatted(myPort));
            while (true) {
                try(final var socket = serverSocket.accept()) {
                    Logger.getGlobal().info("Accepted Socket connection.");
                    final var packetList = this.handleConnection(socket);
                    this.handle(packetList);
                }
                Logger.getGlobal().info("Sending %s reply(s)".formatted(store.size()));
                Logger.getGlobal().info("Opening outgoing Socket to %s:%s".formatted(targetHost, targetPort));
                try (final var outgoingSocket = new Socket(targetHost, targetPort)) {
                    try (final var os = outgoingSocket.getOutputStream()) {
                        while (!store.isEmpty()) {
                            final var replyPacket = endpoint.repackReply(store.popReply());
                            final var replyBytes = node.client().packMessage(replyPacket);
                            os.write(replyBytes);
                            Logger.getGlobal().info("Wrote %s bytes".formatted(replyBytes.length));
                        }
                    }
                }
                Logger.getGlobal().info("Sent reply(s)");
            }
        }
    }

    private List<DestinationAndMessage> handleConnection(Socket socket) throws IOException, SphinxException {
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

    public void handle(List<DestinationAndMessage> messages) {
        for (final var msg : messages) {
            final var packet = Packet.parse(msg.message());
            final var bucketEntry = new BucketEntry(packet.bucketId(), packet);
            store.add(bucketEntry);
        }
    }
}
