package edu.kit.tm.ps.latte_mixxiato.client;

import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Receiver;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.packet.Packet;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ReplyServer {

    private final int port;
    private final Receiver receiver;

    public ReplyServer(final int port, final Receiver receiver) {
        this.port = port;
        this.receiver = receiver;
    }

    public void listen() throws IOException {
        try (final var serverSocket = new ServerSocket(port)) {
            LatteLogger.get().debug("Opened socket on port %s".formatted(port));
            while (true) {
                try(final var socket = serverSocket.accept()) {
                    LatteLogger.get().debug("Accepted Socket connection.");
                    final var packet = this.handleConnection(socket);
                    receiver.receive(packet);
                }
            }
        }
    }

    private Packet handleConnection(Socket socket) throws IOException {
        try (final var is = socket.getInputStream()) {
            final var packetBytes = is.readAllBytes();
            LatteLogger.get().info("Received packet.");
            return Packet.parse(packetBytes);
        }
    }
}
