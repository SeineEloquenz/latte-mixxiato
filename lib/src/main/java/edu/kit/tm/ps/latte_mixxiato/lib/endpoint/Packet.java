package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import java.util.UUID;

public record Packet(UUID uuid, int sequenceNumber, int packetsInMessage, byte[] payload) {
}