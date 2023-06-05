package edu.kit.tm.ps.latte_mixxiato.lib.endpoint.packet;

import java.util.UUID;

public record UnaddressedPacket(UUID uuid, int packetsInMessage, int sequenceNumber, byte[] payload) {
    public Packet address(UUID bucketId) {
        return new Packet(uuid, bucketId, packetsInMessage, sequenceNumber, payload);
    }
}