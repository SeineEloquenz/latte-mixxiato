package edu.kit.tm.ps.latte_mixxiato.dead_drop;

import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorClient;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixType;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;
import java.net.ServerSocket;
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

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                final var is = socket.getInputStream();
                while (is.available() > 0) {
                    final var packetBytes = is.readNBytes(sphinxClient.params().packetLength());
                    final var processedPacket = sphinxNode.sphinxProcess(sphinxClient.unpackMessage(packetBytes).packetContent());
                    final var flag = processedPacket.routingFlag();
                    if (flag.equals(RoutingFlag.DESTINATION)) {
                        final var destinationAndMessage = sphinxClient.receiveForward(processedPacket.macKey(), processedPacket.packetContent().delta());
                        //TODO actually handle packet
                    } else {
                        Logger.getGlobal().warning("Received packet with wrong flag %s".formatted(flag));
                    }
                }
            }
        }
    }
}
