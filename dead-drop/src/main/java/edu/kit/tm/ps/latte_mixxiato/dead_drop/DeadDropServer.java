package edu.kit.tm.ps.latte_mixxiato.dead_drop;

import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.message.DestinationAndMessage;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.ReplyBuilder;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.packet.Packet;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class DeadDropServer {

    private final int myPort;
    private final String targetHost;
    private final int targetPort;
    private final ReplyBuilder replyBuilder;
    private final SphinxNode node;
    private final Store store;

    public DeadDropServer(final int myPort, final String targetHost, final int targetPort, final ReplyBuilder replyBuilder, final SphinxNode node) {
        this.myPort = myPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.replyBuilder = replyBuilder;
        this.node = node;
        this.store = new Store();
    }

    public void listen() throws IOException, SphinxException {
        try (final var serverSocket = new ServerSocket(myPort)) {
            LatteLogger.get().debug("Opened socket on port %s".formatted(myPort));
            while (true) {
                try(final var socket = serverSocket.accept()) {
                    LatteLogger.get().debug("Accepted Socket connection.");
                    final var packetList = this.handleConnection(socket);
                    this.handle(packetList);
                }
                LatteLogger.get().info("Sending %s reply(s) to %s:%s".formatted(store.size(), targetHost, targetPort));
                try (final var outgoingSocket = new Socket(targetHost, targetPort)) {
                    try (final var os = outgoingSocket.getOutputStream()) {
                        while (!store.isEmpty()) {
                            final var replyPacket = replyBuilder.repackReply(store.popReply());
                            final var replyBytes = node.client().packMessage(replyPacket);
                            os.write(replyBytes);
                        }
                    }
                }
                LatteLogger.get().debug("Sent reply(s)");
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

    public void handle(List<DestinationAndMessage> messages) {
        for (final var msg : messages) {
            final var packet = Packet.parse(msg.message());
            final var bucketEntry = new BucketEntry(packet.bucketId(), packet);
            store.add(bucketEntry);
        }
    }
}
