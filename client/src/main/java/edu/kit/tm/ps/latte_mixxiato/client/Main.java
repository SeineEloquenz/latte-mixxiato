package edu.kit.tm.ps.latte_mixxiato.client;

import com.robertsoultanaev.javasphinx.SphinxException;
import edu.kit.tm.ps.latte_mixxiato.lib.client.ClientInfo;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorClient;
import edu.kit.tm.ps.latte_mixxiato.lib.coordinator.CoordinatorConfig;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.BucketIdGenerator;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Endpoint;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.MessageBuilder;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Receiver;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Sender;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.FixedRoundProvider;
import edu.kit.tm.ps.latte_mixxiato.lib.sphinx.DefaultSphinxFactory;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Main {

    private static final int ARGS_LENGTH = 1;

    public static void main(String[] args) throws IOException {
        if (args.length < ARGS_LENGTH) {
            Logger.getGlobal().severe("You need to pass the conversation partners seed.");
            System.exit(1);
        }
        final var seed = Long.parseLong(args[0]);

        final var coordinatorClient = new CoordinatorClient(CoordinatorConfig.load());

        final var messages = Arrays.stream(args).skip(ARGS_LENGTH).toList();

        final var sphinxFactory = new DefaultSphinxFactory(); //TODO configure or get from coordinator
        coordinatorClient.waitUntilReady();

        final var gateway = coordinatorClient.gateway();

        final var endpoint = new MessageBuilder(new BucketIdGenerator(), gateway, coordinatorClient.relay(), coordinatorClient.deadDrop(), sphinxFactory.client());
        final var sender = new Sender(gateway, endpoint, sphinxFactory.client(), new FixedRoundProvider(55, ChronoUnit.SECONDS));
        final var receiver = new Receiver(assembledMessage -> System.out.println(assembledMessage.messageContent()));

        final var replyServer = new ReplyServer(ClientInfo.PORT, receiver);
        final var client = new Client(sender);

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                replyServer.listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        final var scanner = new Scanner(System.in);
        Logger.getGlobal().info("Sending messages from cli...");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        messages.forEach(msg -> enqueue(client, seed, msg));
        Logger.getGlobal().info("Type your messages and confirm with enter. Entering :q will quit.");
        while (scanner.hasNext()) {
            final var line = scanner.nextLine();
            if (":q".equals(line)) {
                break;
            }
            enqueue(client, seed , line);
        }
        Logger.getGlobal().info("Goodbye.");
    }

    private static void enqueue(Client client, long peerSeed, String message) {
        Logger.getGlobal().info("Enqueueing message %s".formatted(message));
        try {
            client.sendMessage(peerSeed, message);
        } catch (SphinxException e) {
            e.printStackTrace();
        }
    }
}