package edu.kit.tm.ps.latte_mixxiato.mix.dispatcher;

import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.ProcessedPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.RoundProvider;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.RelayInformation;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SynchronizingDispatcher implements Dispatcher {
    private final SphinxNode node;
    private final Map<ProcessedPacket, RelayInformation> packets;
    private final ScheduledExecutorService dispatchService;

    public SynchronizingDispatcher(final SphinxNode node, RoundProvider provider) {
        this.node = node;
        this.packets = new ConcurrentHashMap<>();
        this.dispatchService = Executors.newScheduledThreadPool(4);
        this.dispatchService.scheduleAtFixedRate(
                () -> {
                    Logger.getGlobal().info("Reached round end, dispatching all messages now.");
                    packets.forEach((packet, relayInfo) -> this.relay(relayInfo.node(), packet));
                    packets.clear();
                },
                provider.timeUntilRoundEnd().time(),
                10 * 1000,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispatch(RelayInformation info, ProcessedPacket packet) {
        packets.put(packet, info);
        Logger.getGlobal().info("Enqueued packet for current round.");
    }

    private void relay(MixNode destination, ProcessedPacket processedPacket) {
        try {
            destination.send(node.client(), node.repack(processedPacket));
            Logger.getGlobal().info("Relayed packet to node %s".formatted(destination.id()));
        } catch (IOException e) {
            Logger.getGlobal().warning("Failed to relay packet to node %s".formatted(destination.id()));
            throw new RuntimeException(e);
        }
    }
}
