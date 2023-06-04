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
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            Logger.getGlobal().severe("You need to pass the hostname, gateway port and dead drop port the server is listening on");
            System.exit(1);
        }
        final var hostname = args[0];
        final var gatewayPort = Integer.parseInt(args[1]);
        final var deadDropPort = Integer.parseInt(args[2]);
        final var coordinatorClient = new CoordinatorClient(CoordinatorConfig.load());

        final var sphinxFactory = new DefaultSphinxFactory(); //TODO get from coordinator

        final var keyPair = sphinxFactory.pkiGenerator().generateKeyPair();
        final var sphinxNode = sphinxFactory.node(keyPair.priv());

        coordinatorClient.register(MixType.RELAY, hostname, gatewayPort, keyPair.pub());
        Logger.getGlobal().info("Registered with coordinator");

        final var mixNodeRepository = coordinatorClient.waitForMixes();
        final var gateway = mixNodeRepository.byType(MixType.GATEWAY);
        final var deadDrop = mixNodeRepository.byType(MixType.DEAD_DROP);
        final var gatewayRelay = new Relay(gatewayPort, gateway.host(), gateway.port(), sphinxNode);
        final var deadDropRelay = new Relay(deadDropPort, deadDrop.host(), deadDrop.port(), sphinxNode);
        final var executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> {
            try {
                gatewayRelay.listen();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        executor.submit(() -> {
            try {
                deadDropRelay.listen();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
