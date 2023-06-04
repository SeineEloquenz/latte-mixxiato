package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.RoundProvider;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.InwardMessage;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Sender {

    private final Gateway gateway;
    private final MessageBuilder messageBuilder;
    private final Queue<SphinxPacket> packetQueue;

    public Sender(final Gateway gateway, final MessageBuilder messageBuilder, final SphinxClient client, final RoundProvider provider) {
        this.gateway = gateway;
        this.messageBuilder = messageBuilder;
        this.packetQueue = new LinkedList<>();
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(
                () -> {
                    Logger.getGlobal().info("Reached round end, sending message.");
                    Optional.ofNullable(packetQueue.peek())
                            .or(this::generateNoisePacket)
                            .ifPresent(packet -> {
                                try {
                                    this.send(client, packet);
                                    packetQueue.poll();
                                } catch (IOException e) {
                                    Logger.getGlobal().warning("Failed to send packet Trying again in next round. Stacktrace: ");
                                    e.printStackTrace();
                                } catch (SphinxException e) {
                                    e.printStackTrace();
                                }
                            });
                },
                provider.timeUntilRoundEnd().time(),
                10 * 1000,
                TimeUnit.MILLISECONDS);
    }

    public void enqueueToSend(InwardMessage message) throws SphinxException {
        final var packets = messageBuilder.splitIntoSphinxPackets(message);
        packetQueue.addAll(packets);
    }

    private Optional<SphinxPacket> generateNoisePacket() {
        final List<SphinxPacket> packets;
        try {
            packets = messageBuilder.splitIntoSphinxPackets(new InwardMessage("<empty>".getBytes(StandardCharsets.UTF_8)));
            return Optional.ofNullable(packets.get(0));
        } catch (SphinxException e) {
            Logger.getGlobal().warning("Failed to generate noise packet, Stacktrace:");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private void send(SphinxClient client, SphinxPacket packet) throws IOException, SphinxException {
        try (final var socket = new Socket(gateway.host(), gateway.clientPort())) {
            final var os = socket.getOutputStream();
            os.write(client.packMessage(packet));
        }
    }
}
