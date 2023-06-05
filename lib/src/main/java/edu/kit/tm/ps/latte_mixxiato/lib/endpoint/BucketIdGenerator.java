package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;
import edu.kit.tm.ps.latte_mixxiato.lib.rounds.RoundProvider;

import java.util.Random;
import java.util.UUID;

public class BucketIdGenerator {

    private final long seed;
    private final RoundProvider roundProvider;

    public BucketIdGenerator(final long seed, final RoundProvider roundProvider) {
        this.seed = seed;
        this.roundProvider = roundProvider;
    }

    public UUID next() {
        final var nextSeed = seed + roundProvider.nextRoundEnd().toEpochMilli();
        LatteLogger.get().info(String.valueOf(nextSeed));
        final var currentRandom = new Random(nextSeed);
        final var high = currentRandom.nextLong();
        final var low = currentRandom.nextLong();
        return new UUID(high, low);
    }
}
