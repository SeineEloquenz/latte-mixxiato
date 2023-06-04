package edu.kit.tm.ps.latte_mixxiato.gateway;

import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.ProcessedPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.RoundProvider;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Relay;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SynchronizingDispatcher {
    private final List<ProcessedPacket> packets;
    private final ScheduledExecutorService dispatchService;

    public SynchronizingDispatcher(final SphinxNode node, final Relay relay, RoundProvider provider) {
        this.packets = new LinkedList<>();
        this.dispatchService = Executors.newScheduledThreadPool(4);
        this.dispatchService.scheduleAtFixedRate(
                () -> {
                    Logger.getGlobal().info("Reached round end, dispatching %s message(s) now.".formatted(packets.size()));
                    try (final var socket = new Socket(relay.host(), relay.gatewayPort())) {
                        try (final var os = socket.getOutputStream()) {
                            for (ProcessedPacket packet : packets) {
                                final var packedMessage = node.client().packMessage(node.repack(packet));
                                Logger.getGlobal().info("Wrote %s bytes".formatted(packedMessage.length));
                                os.write(packedMessage);
                                Logger.getGlobal().info("Wrote packet to OutputStream.");
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    packets.clear();
                },
                provider.timeUntilRoundEnd().time(),
                10 * 1000,
                TimeUnit.MILLISECONDS);
    }

    public void dispatch(ProcessedPacket packet) {
        packets.add(packet);
        Logger.getGlobal().info("Enqueued packet for current round.");
    }
}
