package edu.kit.tm.ps.latte_mixxiato.client;

import com.robertsoultanaev.javasphinx.SphinxException;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorClient;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.BucketIdGenerator;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.MessageBuilder;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Receiver;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Sender;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.FixedRoundProvider;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Main {

    private static final int ARGS_LENGTH = 2;

    public static void main(String[] args) throws IOException {
        if (args.length < ARGS_LENGTH) {
            LatteLogger.get().error("You need to pass the port on which the client listens for replies and the conversation partners seed.");
            System.exit(1);
        }
        final var port = Integer.parseInt(args[0]);
        final var seed = Long.parseLong(args[1]);

        final var coordinatorClient = new CoordinatorClient(CoordinatorConfig.load());

        final var sphinxFactory = new DefaultSphinxFactory(); //TODO configure or get from coordinator
        coordinatorClient.waitUntilReady();

        final var gateway = coordinatorClient.gateway();

        final var roundProvider = new FixedRoundProvider(55, ChronoUnit.SECONDS);
        final var idGenerator = new BucketIdGenerator(seed, roundProvider);

        final var endpoint = new MessageBuilder(gateway, coordinatorClient.relay(), coordinatorClient.deadDrop(), sphinxFactory.client(), port);
        final var sender = new Sender(gateway, endpoint, sphinxFactory.client(), roundProvider, idGenerator);
        final var receiver = new Receiver(assembledMessage -> System.out.println(assembledMessage.messageContent()));

        final var replyServer = new ReplyServer(port, receiver);
        final var client = new Client(port, sender);

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                replyServer.listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        handleArgs(client, args);
        handleUserInput(client);

        System.out.println("Goodbye.");
    }

    private static void handleArgs(Client client, String[] args) {
        final var messages = Arrays.stream(args).skip(ARGS_LENGTH).toList();
        if (messages.size() > 0) {
            LatteLogger.get().info("Sending messages from cli...");
            messages.forEach(msg -> enqueue(client, msg));
        }
    }

    private static void handleUserInput(Client client) {
        final var scanner = new Scanner(System.in);
        System.out.println("Type your messages and confirm with enter. Entering :q will quit.");
        while (scanner.hasNext()) {
            final var line = scanner.nextLine();
            if (":q".equals(line)) {
                break;
            }
            enqueue(client, line);
        }
    }

    private static void enqueue(Client client, String message) {
        LatteLogger.get().info("Enqueueing message %s".formatted(message));
        try {
            client.sendMessage(message);
        } catch (SphinxException e) {
            e.printStackTrace();
        }
    }
}