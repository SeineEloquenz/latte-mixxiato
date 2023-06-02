package edu.kit.tm.ps.latte_mixxiato.relay;

import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorClient;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixType;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            Logger.getGlobal().severe("You need to pass the hostname and port the server is listening on");
            System.exit(1);
        }
        final var hostname = args[0];
        final var port = Integer.parseInt(args[1]);
        final var coordinatorClient = new CoordinatorClient(CoordinatorConfig.load());

        final var sphinxFactory = new DefaultSphinxFactory(); //TODO get from coordinator
        final var sphinxClient = sphinxFactory.client();

        final var keyPair = sphinxFactory.pkiGenerator().generateKeyPair();
        final var sphinxNode = sphinxFactory.node(keyPair.priv());

        coordinatorClient.register(MixType.DEAD_DROP, hostname, port, keyPair.pub());

        final var mixNodeRepository = coordinatorClient.waitForMixes();
        final var deadDrop = mixNodeRepository.byType(MixType.DEAD_DROP);

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                final var is = socket.getInputStream();
                final var packets = new LinkedList<SphinxPacket>();
                while (is.available() > 0) {
                    final var packetBytes = is.readNBytes(sphinxClient.params().packetLength());
                    final var processedPacket = sphinxNode.sphinxProcess(sphinxClient.unpackMessage(packetBytes).packetContent());
                    final var flag = processedPacket.routingFlag();
                    if (flag.equals(RoutingFlag.RELAY)) {
                        packets.add(sphinxNode.repack(processedPacket));
                    } else {
                        Logger.getGlobal().warning("Received packet with wrong flag %s".formatted(flag));
                    }
                }
                try (final var outgoingSocket = new Socket(deadDrop.host(), deadDrop.port())) {
                    final var os = outgoingSocket.getOutputStream();
                    for (final var packet : packets) {
                        os.write(sphinxClient.packMessage(packet));
                    }
                }
            }
        }
    }
}
