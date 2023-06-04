package edu.kit.tm.ps.latte_mixxiato.relay;

import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class RelayServer {

    private final int myPort;
    private final String targetHost;
    private final int targetPort;
    private final SphinxNode node;

    public RelayServer(final int myPort, final String targetHost, final int targetPort, final SphinxNode node) {
        this.myPort = myPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.node = node;
    }

    public void listen() throws IOException, SphinxException {
        try (final var serverSocket = new ServerSocket(myPort)) {
            Logger.getGlobal().info("Opened socket on port %s".formatted(myPort));
            while (true) {
                try (final var socket = serverSocket.accept()) {
                    Logger.getGlobal().info("Accepted Socket connection.");
                    final var packetList = this.handleConnection(socket);
                    this.send(packetList);
                }
            }
        }
    }

    private List<SphinxPacket> handleConnection(Socket socket) throws IOException, SphinxException {
        final var packetList = new LinkedList<SphinxPacket>();
        try (final var is = socket.getInputStream()) {
            do {
                final var packetBytes = is.readNBytes(1254);
                final var processedPacket = node.sphinxProcess(node.client().unpackMessage(packetBytes).packetContent());
                final var flag = processedPacket.routingFlag();
                if (flag.equals(RoutingFlag.RELAY)) {
                    packetList.add(node.repack(processedPacket));
                } else {
                    Logger.getGlobal().warning("Received packet with wrong flag %s".formatted(flag));
                }
            } while (is.available() > 0);
        }
        Logger.getGlobal().info("Received %s packet(s)".formatted(packetList.size()));
        return packetList;
    }

    public void send(List<SphinxPacket> packets) throws IOException, SphinxException {
        Logger.getGlobal().info("Opening outgoing Socket to %s:%s".formatted(targetHost, targetPort));
        try (final var outgoingSocket = new Socket(targetHost, targetPort)) {
            try (final var os = outgoingSocket.getOutputStream()) {
                for (final var packet : packets) {
                    os.write(node.client().packMessage(packet));
                }
            }
        }
        Logger.getGlobal().info("Sent %s packet(s).".formatted(packets.size()));
    }
}
