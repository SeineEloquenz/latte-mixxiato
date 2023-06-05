package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PermuterTest {

    private Permuter permuter;
    private List<String> originalList;

    @Before
    public void setup() {
        permuter = new Permuter();
        originalList = new ArrayList<>();
        originalList.add("a");
        originalList.add("b");
        originalList.add("c");
        originalList.add("d");
        originalList.add("e");
    }

    @Test
    public void testPermutation() {
        final var permuted = permuter.permute(originalList);
        Assert.assertNotSame(originalList, permuted);
        final var restored = permuter.restoreOrder(permuted);
        assertEqual(originalList, restored);
    }

    @Test
    public void testRepeatedPermutations() {
        testPermutation();
        testPermutation();
    }

    private void assertEqual(List<String> first, List<String> second) {
        for (int i = 0; i < first.size(); i++) {
            Assert.assertEquals(first.get(i), second.get(i));
        }
    }
}