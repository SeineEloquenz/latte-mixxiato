package edu.kit.tm.ps.latte_mixxiato.dead_drop;

import com.robertsoultanaev.javasphinx.SphinxException;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorClient;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.ReplyBuilder;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.DeadDrop;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, SphinxException {
        if (args.length != 2) {
            LatteLogger.get().error("You need to pass the hostname and gatewayPort the server is listening on");
            System.exit(1);
        }
        final var hostname = args[0];
        final var port = Integer.parseInt(args[1]);
        final var coordinatorClient = new CoordinatorClient(CoordinatorConfig.load());

        final var sphinxFactory = new DefaultSphinxFactory(); //TODO get from coordinator

        final var keyPair = sphinxFactory.pkiGenerator().generateKeyPair();
        final var sphinxNode = sphinxFactory.node(keyPair.priv());

        final var deadDrop = new DeadDrop(hostname, port, keyPair.pub());

        coordinatorClient.register(deadDrop);

        coordinatorClient.waitUntilReady();

        final var gateway = coordinatorClient.gateway();
        final var relay = coordinatorClient.relay();

        final var deadDropServer = new DeadDropServer(port, relay.host(), relay.deadDropPort(),
                new ReplyBuilder(gateway, relay, deadDrop, sphinxFactory.client()), sphinxNode);

        deadDropServer.listen();
    }
}
