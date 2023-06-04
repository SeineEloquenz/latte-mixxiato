package edu.kit.tm.ps.latte_mixxiato.client;

import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorClient;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Endpoint;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Sender;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.FixedRoundProvider;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        final var coordinatorClient = new CoordinatorClient(CoordinatorConfig.load());

        final var messages = Arrays.stream(args).skip(2).toList();

        final var sphinxFactory = new DefaultSphinxFactory(); //TODO configure or get from coordinator
        coordinatorClient.waitUntilReady();

        final var gateway = coordinatorClient.gateway();

        final var endpoint = new Endpoint(gateway, coordinatorClient.relay(), coordinatorClient.deadDrop(), sphinxFactory.client());
        final var sender = new Sender(gateway, endpoint, sphinxFactory.client(), new FixedRoundProvider(55, ChronoUnit.SECONDS));
        final var client = new Client(sender);

        final var scanner = new Scanner(System.in);
        Logger.getGlobal().info("Sending messages from cli...");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        messages.forEach(msg -> enqueue(client, msg));
        Logger.getGlobal().info("Type your messages and confirm with enter. Entering :q will quit.");
        while (scanner.hasNext()) {
            final var line = scanner.nextLine();
            if (":q".equals(line)) {
                break;
            }
            enqueue(client, line);
        }
        Logger.getGlobal().info("Goodbye.");
    }

    private static void enqueue(Client client, String message) {
        Logger.getGlobal().info("Enqueueing message %s".formatted(message));
        client.sendMessage(message);
    }
}