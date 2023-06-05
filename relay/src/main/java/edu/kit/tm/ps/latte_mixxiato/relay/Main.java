package edu.kit.tm.ps.latte_mixxiato.relay;

import com.robertsoultanaev.javasphinx.SphinxException;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorClient;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Relay;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            LatteLogger.get().error("You need to pass the hostname, gateway port and dead drop port the server is listening on");
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
        final var gatewayRelay = new RelayServer(gatewayPort, deadDrop.host(), deadDrop.relayPort(), sphinxNode);
        final var deadDropRelay = new RelayServer(deadDropPort, gateway.host(), gateway.relayPort(), sphinxNode);
        final var executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> {
            try {
                gatewayRelay.listen();
            } catch (IOException | SphinxException e) {
                e.printStackTrace();
            }
        });
        executor.submit(() -> {
            try {
                deadDropRelay.listen();
            } catch (IOException | SphinxException e) {
                e.printStackTrace();
            }
        });
    }
}
