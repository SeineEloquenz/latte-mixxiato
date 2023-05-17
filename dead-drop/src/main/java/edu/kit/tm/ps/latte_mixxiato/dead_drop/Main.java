package edu.kit.tm.ps.latte_mixxiato.dead_drop;

import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxParams;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Endpoint;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Receiver;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.CoordinatorMixNodeRepository;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1) {
            Logger.getGlobal().severe("You need to pass the port the server is listening on");
            System.exit(1);
        }
        final var port = Integer.parseInt(args[0]);
        final var coordinatorConfig = CoordinatorConfig.load();

        final var mixNodeRepository = new CoordinatorMixNodeRepository(coordinatorConfig);
        final var endpoint = new Endpoint(mixNodeRepository, 3, new SphinxClient(new SphinxParams()));
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
