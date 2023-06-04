package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class BucketIdGenerator {
    private final Map<Long, Random> randoms;

    public BucketIdGenerator() {
        this.randoms = new HashMap<>();
    }

    public UUID next(long peerSeed) {
        randoms.putIfAbsent(peerSeed, new Random(peerSeed));
        final var rand = randoms.get(peerSeed);
        final var high = rand.nextLong();
        final var low = rand.nextLong();
        return new UUID(high, low);
    }
}
