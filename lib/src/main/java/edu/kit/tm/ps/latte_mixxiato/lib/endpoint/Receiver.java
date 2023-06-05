package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SerializationUtils;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.packet.Packet;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Receiver {

    private final Map<UUID, Set<Packet>> messageStore;
    private final ReassemblyHandler reassemblyHandler;

    public Receiver(final ReassemblyHandler reassemblyHandler) {
        this.reassemblyHandler = reassemblyHandler;
        this.messageStore = new ConcurrentHashMap<>();
    }

    /**
     * Reeceives a packet and if this packet completes a message, the {@link ReassemblyHandler} is called
     * @param packet packet to receive
     */
    public void receive(Packet packet) {
        messageStore.putIfAbsent(packet.uuid(), Collections.synchronizedSet(new HashSet<>()));
        final var partialMessage = messageStore.get(packet.uuid());
        partialMessage.add(packet);
        if (packet.packetsInMessage() == partialMessage.size()) {
            final var assembledMessage = reassemble(partialMessage);
            reassemblyHandler.onReassembly(assembledMessage);
            messageStore.remove(packet.uuid());
        }
    }

    /**
     * Reassemble an {@link AssembledMessage} from received {@link Packet}s
     * @param packets received packets, may not be empty
     * @return the assembled message
     */
    private AssembledMessage reassemble(Set<Packet> packets) {
        assert packets.size() != 0;
        final var uuid = packets.stream().findAny().get().uuid();
        byte[][] payloads = new byte[packets.size()][];
        packets.stream()
                .sorted(Comparator.comparingInt(Packet::sequenceNumber))
                .forEach(packet -> payloads[packet.sequenceNumber()] = packet.payload());
        byte[] message = SerializationUtils.concatenate(payloads);

        return new AssembledMessage(uuid, message);
    }
}
