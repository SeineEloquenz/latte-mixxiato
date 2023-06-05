package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.RoundProvider;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.InwardMessage;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sender {

    private final Gateway gateway;
    private final MessageBuilder messageBuilder;
    private final Queue<Packet> packetQueue;

    public Sender(final Gateway gateway, final MessageBuilder messageBuilder, final SphinxClient client, final RoundProvider provider) {
        this.gateway = gateway;
        this.messageBuilder = messageBuilder;
        this.packetQueue = new LinkedList<>();
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(
                () -> Optional.ofNullable(packetQueue.peek())
                        .or(() -> Optional.ofNullable(messageBuilder.makeNoisePacket()))
                        .ifPresent(packet -> {
                            try {
                                LatteLogger.get().info("Reached round end, sending packet(bucket=%s,pim=%s,seq=%s)."
                                        .formatted(packet.bucketId(), packet.packetsInMessage(), packet.sequenceNumber()));
                                this.send(client, messageBuilder.makeOnion(packet));
                                packetQueue.poll();
                            } catch (IOException e) {
                                LatteLogger.get().warn("Failed to send packet Trying again in next round. Stacktrace: ");
                                e.printStackTrace();
                            } catch (SphinxException e) {
                                LatteLogger.get().warn("Got SphinxException when packing packet. Stacktrace: ");
                                e.printStackTrace();
                            }
                        }),
                provider.timeUntilRoundEnd().time(),
                10 * 1000,
                TimeUnit.MILLISECONDS);
    }

    public void enqueueToSend(InwardMessage message) throws SphinxException {
        final var packets = messageBuilder.splitIntoPackets(message);
        packetQueue.addAll(packets);
    }

    private void send(SphinxClient client, SphinxPacket packet) throws IOException, SphinxException {
        try (final var socket = new Socket(gateway.host(), gateway.clientPort())) {
            final var os = socket.getOutputStream();
            os.write(client.packMessage(packet));
        }
    }
}
