package edu.kit.tm.ps.latte_mixxiato.client;

import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorClient;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Endpoint;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Sender;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNodeRepository;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            Logger.getGlobal().severe("You need to pass the recipient ip address and port");
            System.exit(1);
        }
        final var targetHost = args[0];
        final var targetPort = Integer.parseInt(args[1]);
        final var targetAddress = new InetSocketAddress(targetHost, targetPort);
        final var coordinatorClient = new CoordinatorClient(CoordinatorConfig.load());

        final var messages = Arrays.stream(args).skip(2).toList();

        final var sphinxFactory = new DefaultSphinxFactory(); //TODO configure or get from coordinator
        final var mixNodeRepository = coordinatorClient.waitForMixes();

        final var endpoint = new Endpoint(mixNodeRepository, MixNodeRepository.DESIRED_MIX_AMOUNT, sphinxFactory.client());
        final var sender = new Sender(endpoint, sphinxFactory.client());
        final var client = new Client(sender);

        final var scanner = new Scanner(System.in);
        Logger.getGlobal().info("Sending messages from cli...");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        messages.forEach(msg -> send(client, targetAddress, msg));
        Logger.getGlobal().info("Type your messages and confirm with enter. Entering :q will quit.");
        while (scanner.hasNext()) {
            final var line = scanner.nextLine();
            if (":q".equals(line)) {
                break;
            }
            send(client, targetAddress, line);
        }
        Logger.getGlobal().info("Goodbye.");
    }

    private static void send(Client client, InetSocketAddress destination, String message) {
        try {
            Logger.getGlobal().info("Sending message %s to %s%n".formatted(message, destination));
            client.sendMessage(destination, message);
        } catch (IOException e) {
            Logger.getGlobal().severe("Error occured while message to %s:%s, dumping Stacktrace:"
                    .formatted(destination.getHostName(), destination.getPort()));
            e.printStackTrace();
        }
    }
}