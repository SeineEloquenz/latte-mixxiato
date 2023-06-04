package edu.kit.tm.ps.latte_mixxiato.relay;

import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorClient;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Relay;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            Logger.getGlobal().severe("You need to pass the hostname, gateway gatewayPort and dead drop gatewayPort the server is listening on");
            System.exit(1);
        }
        final var hostname = args[0];
        final var gatewayPort = Integer.parseInt(args[1]);
        final var deadDropPort = Integer.parseInt(args[2]);
        final var coordinatorClient = new CoordinatorClient(CoordinatorConfig.load());

        final var sphinxFactory = new DefaultSphinxFactory(); //TODO get from coordinator

        final var keyPair = sphinxFactory.pkiGenerator().generateKeyPair();
        final var sphinxNode = sphinxFactory.node(keyPair.priv());

        final var relay = new Relay(hostname, gatewayPort, deadDropPort, keyPair.pub());
        coordinatorClient.register(relay);

        coordinatorClient.waitUntilReady();

        final var gateway = coordinatorClient.gateway();
        final var deadDrop = coordinatorClient.deadDrop();
        final var gatewayRelay = new RelayServer(gatewayPort, gateway.host(), gateway.relayPort(), sphinxNode);
        final var deadDropRelay = new RelayServer(deadDropPort, deadDrop.host(), deadDrop.relayPort(), sphinxNode);
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
