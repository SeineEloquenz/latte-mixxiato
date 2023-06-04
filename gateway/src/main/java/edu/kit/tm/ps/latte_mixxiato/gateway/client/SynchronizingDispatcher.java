package edu.kit.tm.ps.latte_mixxiato.gateway.client;

import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.ProcessedPacket;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.ClientData;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.ClientList;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.PacketWithSender;
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
                    Logger.getGlobal().info("Reached round end, dispatching %s message(s) now.".formatted(packets.size()));
                    try (final var socket = new Socket(relay.host(), relay.gatewayPort())) {
                        try (final var os = socket.getOutputStream()) {
                            for (final var packetWithSender : packets) {
                                final var packedMessage = node.client().packMessage(packetWithSender.packet());
                                os.write(packedMessage);
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

    public void dispatch(ClientData clientData, ProcessedPacket packet) {
        packets.add(new PacketWithSender(clientData, node.repack(packet)));
        Logger.getGlobal().info("Enqueued packet for current round.");
    }
}
