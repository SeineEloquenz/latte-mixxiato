package edu.kit.tm.ps.latte_mixxiato.gateway.client;

import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.ProcessedPacket;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.ClientData;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.ClientList;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.PacketWithSender;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.RoundProvider;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Relay;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SynchronizingDispatcher {
    private final List<PacketWithSender> packets;
    private final SphinxNode node;
    private final ScheduledExecutorService dispatchService;

    public SynchronizingDispatcher(final SphinxNode node, final Relay relay, RoundProvider provider, final ClientList clientList) {
        this.node = node;
        this.packets = new LinkedList<>();
        this.dispatchService = Executors.newScheduledThreadPool(4);
        this.dispatchService.scheduleAtFixedRate(
                () -> {
                    clientList.clear();
                    LatteLogger.get().info("Reached round end, sending %s message(s) to %s:%s."
                            .formatted(packets.size(), relay.host(), relay.gatewayPort()));
                    try (final var socket = new Socket(relay.host(), relay.gatewayPort())) {
                        try (final var os = socket.getOutputStream()) {
                            for (final var packetWithSender : packets) {
                                final var packedMessage = node.client().packMessage(packetWithSender.packet());
                                os.write(packedMessage);
                                LatteLogger.get().debug("Wrote %s bytes".formatted(packedMessage.length));
                                clientList.record(packetWithSender.clientData());
                            }
                        }
                    } catch (IOException | SphinxException e) {
                        e.printStackTrace();
                    }
                    packets.clear();
                },
                provider.timeUntilRoundEnd().time(),
                10 * 1000,
                TimeUnit.MILLISECONDS);
    }

    public synchronized void dispatch(ClientData clientData, ProcessedPacket packet) {
        packets.add(new PacketWithSender(clientData, node.repack(packet)));
        LatteLogger.get().debug("Enqueued packet for current round.");
    }
}
