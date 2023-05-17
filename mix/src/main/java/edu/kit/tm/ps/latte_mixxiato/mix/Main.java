package edu.kit.tm.ps.latte_mixxiato.mix;

import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.CoordinatorMixNodeRepository;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Router;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            Logger.getGlobal().severe("The server needs the following arguments:");
            Logger.getGlobal().severe("1: port the server listens on");
            Logger.getGlobal().severe("2: private key of the server");
            System.exit(1);
        }
        final var port = Integer.parseInt(args[0]);
        final var privateKey = new BigInteger(args[1]);
        final var coordinatorConfig = CoordinatorConfig.load();

        final var sphinxFactory = new DefaultSphinxFactory(); //TODO set realistic parameters or get from coordinator
        final var mixRepository = new CoordinatorMixNodeRepository(coordinatorConfig);
        mixRepository.sync();
        final var router = new Router(mixRepository, sphinxFactory.client());

        //Actual server startup
        final var server = new Server(port, sphinxFactory.client(), sphinxFactory.node(privateKey), router);
        try {
            server.run();
        } catch (InterruptedException e) {
            Logger.getGlobal().info("Server interrupted. Stopping.");
            System.exit(1);
        }
    }
}
