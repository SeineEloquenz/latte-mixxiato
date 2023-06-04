package edu.kit.tm.ps.latte_mixxiato.gateway;

import edu.kit.tm.ps.latte_mixxiato.gateway.client.ClientGateway;
import edu.kit.tm.ps.latte_mixxiato.gateway.client.SynchronizingDispatcher;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.ClientList;
import edu.kit.tm.ps.latte_mixxiato.gateway.relay.RelayGateway;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorClient;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.FixedRoundProvider;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            Logger.getGlobal().severe("You need to pass the hostname, client and relay port the server is listening on");
            System.exit(1);
        }
        final var host = args[0];
        final var clientPort = Integer.parseInt(args[1]);
        final var relayPort = Integer.parseInt(args[2]);
        final var coordinatorClient = new CoordinatorClient(CoordinatorConfig.load());

        final var sphinxFactory = new DefaultSphinxFactory(); //TODO set realistic parameters or get from coordinator
        final var keyPair = sphinxFactory.pkiGenerator().generateKeyPair();

        final var gateway = new Gateway(host, clientPort, relayPort, keyPair.pub());
        coordinatorClient.register(gateway);

        coordinatorClient.waitUntilReady();
        final var sphinxNode = sphinxFactory.node(keyPair.priv());

        final var clientList = new ClientList();

        final SynchronizingDispatcher dispatcher = new SynchronizingDispatcher(sphinxNode,
                coordinatorClient.relay(), new FixedRoundProvider(1, ChronoUnit.MINUTES), clientList);

        final var clientGateway = new ClientGateway(clientPort, sphinxNode, dispatcher);
        final var relayGateway = new RelayGateway(relayPort, clientList, sphinxNode);

        //Actual server startup
        final var server = new Server(clientGateway, relayGateway);
        server.run();
    }
}
