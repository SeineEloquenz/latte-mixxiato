package edu.kit.tm.ps.latte_mixxiato.lib.rounds;

import junit.framework.TestCase;
import org.junit.Assert;

import java.time.Instant;

public class FixedRoundProviderTest extends TestCase {

    private final FixedRoundProvider roundProvider = new FixedRoundProvider();
    public void testTimeUntilRoundEnd() {
        final var roundEnd = roundProvider.timeUntilRoundEnd();
        Assert.assertTrue("Next round time is not greater than 0", roundEnd.time() > 0);
    }
}