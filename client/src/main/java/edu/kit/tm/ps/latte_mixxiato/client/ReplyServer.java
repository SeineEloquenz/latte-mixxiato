package edu.kit.tm.ps.latte_mixxiato.client;

import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.packet.message.DestinationAndMessage;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Endpoint;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Packet;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Receiver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class ReplyServer {

    private final int port;
    private final Receiver receiver;

    public ReplyServer(final int port, final Receiver receiver) {
        this.port = port;
        this.receiver = receiver;
    }

    public void listen() throws IOException {
        try (final var serverSocket = new ServerSocket(port)) {
            Logger.getGlobal().info("Opened socket on port %s".formatted(port));
            while (true) {
                try(final var socket = serverSocket.accept()) {
                    Logger.getGlobal().info("Accepted Socket connection.");
                    final var packetList = this.handleConnection(socket);
                    this.handle(packetList);
                }
            }
        }
    }

    private List<Packet> handleConnection(Socket socket) throws IOException {
        final LinkedList<Packet> messageList = new LinkedList<>();
        try (final var is = socket.getInputStream()) {
            do {
                final var packetBytes = is.readAllBytes();
                messageList.add(Packet.parse(packetBytes));
            } while (is.available() > 0);
        }
        Logger.getGlobal().info("Received %s packet(s)".formatted(messageList.size()));
        return messageList;
    }

    private void handle(List<Packet> packets) {
        for (final var packet : packets) {
            receiver.receive(packet);
        }
    }
}
