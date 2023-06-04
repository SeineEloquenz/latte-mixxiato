package edu.kit.tm.ps.latte_mixxiato.relay;

import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Relay {

    private final int myPort;
    private final String targetHost;
    private final int targetPort;
    private final SphinxNode node;

    public Relay(final int myPort, final String targetHost, final int targetPort, final SphinxNode node) {
        this.myPort = myPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.node = node;
    }

    public void listen() throws IOException {
        try (final var serverSocket = new ServerSocket(myPort)) {
            while (true) {
                final var socket = serverSocket.accept();
                final var packetList = new LinkedList<SphinxPacket>();
                this.handleConnection(socket, packetList);
                this.send(packetList);
            }
        }
    }

    private void handleConnection(Socket socket, final List<SphinxPacket> packetList) throws IOException {
        final var is = socket.getInputStream();
        while (is.available() > 0) {
            final var packetBytes = is.readNBytes(node.client().params().packetLength());
            final var processedPacket = node.sphinxProcess(node.client().unpackMessage(packetBytes).packetContent());
            final var flag = processedPacket.routingFlag();
            if (flag.equals(RoutingFlag.RELAY)) {
                packetList.add(node.repack(processedPacket));
                Logger.getGlobal().info("Received packet");
            } else {
                Logger.getGlobal().warning("Received packet with wrong flag %s".formatted(flag));
            }
        }
    }

    public void send(List<SphinxPacket> packets) throws IOException {
        try (final var outgoingSocket = new Socket(targetHost, targetPort)) {
            final var os = outgoingSocket.getOutputStream();
            for (final var packet : packets) {
                os.write(node.client().packMessage(packet));
            }
        }
    }
}
