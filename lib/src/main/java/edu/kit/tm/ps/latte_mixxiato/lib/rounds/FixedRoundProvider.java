package edu.kit.tm.ps.latte_mixxiato.lib.rounds;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class FixedRoundProvider implements RoundProvider {

    @Override
    public RoundInfo timeUntilRoundEnd() {
        final var currentTime = Instant.now();
        final var roundEnd = nextRoundEnd();
        return new RoundInfo(roundEnd.toEpochMilli() - currentTime.toEpochMilli(), TimeUnit.MILLISECONDS);
    }

    private Instant nextRoundEnd() {
        return Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(1, ChronoUnit.MINUTES);
    }
}
