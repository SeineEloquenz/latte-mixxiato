package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.RoundProvider;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.OutwardMessage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Sender {

    private final Endpoint endpoint;
    private final SphinxClient client;
    private final Queue<Map.Entry<SphinxPacket, MixNode>> packetQueue;
    private final ScheduledExecutorService service;

    public Sender(final Endpoint endpoint, final SphinxClient client, final RoundProvider provider) {
        this.endpoint = endpoint;
        this.client = client;
        this.packetQueue = new LinkedList<>();
        this.service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(
                () -> {
                    Logger.getGlobal().info("Reached round end, sending message.");
                    Optional.ofNullable(packetQueue.peek()).ifPresent(entry -> {
                        try {
                            entry.getValue().send(client, entry.getKey());
                            packetQueue.poll();
                        } catch (IOException e) {
                            Logger.getGlobal().warning("Failed to send packet Trying again in next round. Stacktrace: ");
                            e.printStackTrace();
                        }
                    });
                },
                provider.timeUntilRoundEnd().time(),
                10 * 1000,
                TimeUnit.MILLISECONDS);
    }

    public void enqueueToSend(OutwardMessage message) {
        final var packets = endpoint.splitIntoSphinxPackets(message);
        packetQueue.addAll(packets.entrySet());
    }
}
