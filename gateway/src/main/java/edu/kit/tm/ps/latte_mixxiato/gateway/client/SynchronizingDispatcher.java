package edu.kit.tm.ps.latte_mixxiato.gateway.client;

import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.ProcessedPacket;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.ClientData;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.PacketWithSender;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.RoundProvider;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Permuter;
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
    private final Relay relay;
    private final List<ClientData> clientList;
    private final Permuter permuter;

    public SynchronizingDispatcher(final SphinxNode node, final Relay relay, RoundProvider provider, final List<ClientData> clientList, final Permuter permuter) {
        this.node = node;
        this.relay = relay;
        this.clientList = clientList;
        this.permuter = permuter;
        this.packets = new LinkedList<>();
        ScheduledExecutorService dispatchService = Executors.newSingleThreadScheduledExecutor();
        dispatchService.scheduleAtFixedRate(
                this::handleSend,
                provider.timeUntilRoundEnd().time(),
                10 * 1000,
                TimeUnit.MILLISECONDS);
    }

    private void handleSend() {
        clientList.clear();
        packets.forEach(packetWithSender -> clientList.add(packetWithSender.clientData()));

        final var reordered = permuter.permute(packets);

        LatteLogger.get().info("Reached round end, sending %s message(s) to %s:%s."
                .formatted(packets.size(), relay.host(), relay.gatewayPort()));
        try (final var socket = new Socket(relay.host(), relay.gatewayPort())) {
            try (final var os = socket.getOutputStream()) {
                for (final var packetWithSender : reordered) {
                    final var packedMessage = node.client().packMessage(packetWithSender.packet());
                    os.write(packedMessage);
                    LatteLogger.get().debug("Wrote %s bytes".formatted(packedMessage.length));
                }
            }
        } catch (IOException | SphinxException e) {
            e.printStackTrace();
        }

        packets.clear();
    }

    public synchronized void dispatch(ClientData clientData, ProcessedPacket packet) {
        packets.add(new PacketWithSender(clientData, node.repack(packet)));
        LatteLogger.get().debug("Enqueued packet for current round.");
    }
}
