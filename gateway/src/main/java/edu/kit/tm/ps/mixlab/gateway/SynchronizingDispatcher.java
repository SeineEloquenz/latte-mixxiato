package edu.kit.tm.ps.mixlab.gateway;

import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.ProcessedPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.RoundProvider;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.RelayInformation;
import okhttp3.Dispatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SynchronizingDispatcher {
    private final List<ProcessedPacket> packets;
    private final ScheduledExecutorService dispatchService;

    public SynchronizingDispatcher(final SphinxNode node, final MixNode relay, RoundProvider provider) {
        this.packets = new LinkedList<>();
        this.dispatchService = Executors.newScheduledThreadPool(4);
        this.dispatchService.scheduleAtFixedRate(
                () -> {
                    Logger.getGlobal().info("Reached round end, dispatching all messages now.");
                    try (final var socket = new Socket(relay.host(), relay.port())) {
                        final var os = socket.getOutputStream();
                        for (ProcessedPacket packet : packets) {
                            os.write(node.client().packMessage(node.repack(packet)));
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
