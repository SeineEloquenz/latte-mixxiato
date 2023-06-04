package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import java.util.Random;
import java.util.UUID;

public class BucketIdGenerator {

    private final long seed;
    private final Random random;

    public BucketIdGenerator(final long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    public UUID next() {
        final var high = random.nextLong();
        final var low = random.nextLong();
        return new UUID(high, low);
    }
}
