package edu.kit.tm.ps.latte_mixxiato.dead_drop;

import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorClient;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Endpoint;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Receiver;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            Logger.getGlobal().severe("You need to pass the port the server is listening on");
            System.exit(1);
        }
        final var port = Integer.parseInt(args[0]);
        final var coordinatorClient = new CoordinatorClient(CoordinatorConfig.load());

        final var sphinxFactory = new DefaultSphinxFactory();
        final var mixNodeRepository = coordinatorClient.waitForMixes();
        final var endpoint = new Endpoint(mixNodeRepository, 3, sphinxFactory.client());
        final var receiver = new Receiver(assembledMessage -> System.out.println(new String(assembledMessage.messageBody(), StandardCharsets.UTF_8)));

        //Actual server startup
        final var server = new Server(port, endpoint, receiver);
        try {
            server.run();
        } catch (InterruptedException e) {
            Logger.getGlobal().info("Server interrupted. Stopping.");
            System.exit(1);
        }
    }
}
