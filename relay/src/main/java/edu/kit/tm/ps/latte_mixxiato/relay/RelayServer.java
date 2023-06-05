package edu.kit.tm.ps.latte_mixxiato.relay;

import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class RelayServer {

    private final int myPort;
    private final String targetHost;
    private final int targetPort;
    private final SphinxNode node;
    private Function<List<SphinxPacket>, List<SphinxPacket>> reorderer;

    public RelayServer(final int myPort, final String targetHost, final int targetPort, final SphinxNode node, final Function<List<SphinxPacket>, List<SphinxPacket>> reorderer) {
        this.myPort = myPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.node = node;
        this.reorderer = reorderer;
    }

    public void listen() throws IOException, SphinxException {
        try (final var serverSocket = new ServerSocket(myPort)) {
            LatteLogger.get().debug("Opened socket on port %s".formatted(myPort));
            while (true) {
                try (final var socket = serverSocket.accept()) {
                    LatteLogger.get().debug("Accepted Socket connection.");
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
                if (packetBytes.length == 0) {
                    break;
                }
                final var processedPacket = node.sphinxProcess(node.client().unpackMessage(packetBytes).packetContent());
                final var flag = processedPacket.routingFlag();
                if (flag.equals(RoutingFlag.RELAY)) {
                    packetList.add(node.repack(processedPacket));
                } else {
                    LatteLogger.get().warn("Received packet with wrong flag %s".formatted(flag));
                }
            } while (true);
        }
        LatteLogger.get().info("Received %s packet(s)".formatted(packetList.size()));
        final var reordered = reorderer.apply(packetList);
        LatteLogger.get().debug("Reordered packets");
        return reordered;
    }

    public void send(List<SphinxPacket> packets) throws IOException, SphinxException {
        LatteLogger.get().debug("Opening outgoing Socket to %s:%s".formatted(targetHost, targetPort));
        try (final var outgoingSocket = new Socket(targetHost, targetPort)) {
            try (final var os = outgoingSocket.getOutputStream()) {
                for (final var packet : packets) {
                    os.write(node.client().packMessage(packet));
                }
            }
        }
        LatteLogger.get().info("Sent %s packet(s) to %s:%s.".formatted(packets.size(), targetHost, targetPort));
    }
}
