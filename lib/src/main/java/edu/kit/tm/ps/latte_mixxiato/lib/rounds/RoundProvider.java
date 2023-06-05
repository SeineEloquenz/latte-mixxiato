package edu.kit.tm.ps.latte_mixxiato.lib.rounds;

import java.time.Instant;

public interface RoundProvider {
    RoundInfo timeUntilRoundEnd();

    Instant nextRoundEnd();
}
