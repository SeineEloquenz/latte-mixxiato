package edu.kit.tm.ps.latte_mixxiato.mix;

import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorClient;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Router;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            Logger.getGlobal().severe("The server needs the following arguments:");
            Logger.getGlobal().severe("1: port the server listens on");
            Logger.getGlobal().severe("2: hostname the server is reachable under");
            System.exit(1);
        }
        final var port = Integer.parseInt(args[0]);
        final var host = args[1];
        final var coordinatorClient = new CoordinatorClient(CoordinatorConfig.load());

        final var sphinxFactory = new DefaultSphinxFactory(); //TODO set realistic parameters or get from coordinator
        final var keyPair = sphinxFactory.pkiGenerator().generateKeyPair();

        coordinatorClient.register(host, port, keyPair.pub());
        Logger.getGlobal().info("Registered with coordinator");

        final var mixRepository = coordinatorClient.waitForMixes();
        Logger.getGlobal().info("Retrieved mix list from coordinator");
        final var router = new Router(mixRepository, sphinxFactory.client());

        //Actual server startup
        final var server = new Server(port, sphinxFactory.client(), sphinxFactory.node(keyPair.priv()), router);
        try {
            server.run();
        } catch (InterruptedException e) {
            Logger.getGlobal().info("Server interrupted. Stopping.");
            System.exit(1);
        }
    }
}
