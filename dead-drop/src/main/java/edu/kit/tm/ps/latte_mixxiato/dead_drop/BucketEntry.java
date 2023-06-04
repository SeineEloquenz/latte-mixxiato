package edu.kit.tm.ps.latte_mixxiato.dead_drop;

import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Packet;

import java.util.UUID;

public record BucketEntry(UUID bucketId, Packet packet) {
}
