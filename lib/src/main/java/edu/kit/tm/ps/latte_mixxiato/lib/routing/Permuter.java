package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Permuter {

    private int[] permutationIndex;

    public Permuter() {
        this.permutationIndex = null;
    }

    public <T> List<T> permute(List<T> list) {
        final var shuffled = shuffle(list);
        this.permutationIndex = computePermutation(list, shuffled);
        return shuffled;
    }

    public <T> List<T> restoreOrder(List<T> permuted) {
        final var ordered = new ArrayList<T>(permutationIndex.length);
        for (int i = 0; i < permutationIndex.length; i++) {
            ordered.add(permuted.get(permutationIndex[i]));
        }
        return ordered;
    }

    private  <T> List<T> shuffle(List<T> list) {
        final var shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    private  <T> int[] computePermutation(List<T> original, List<T> shuffled) {
        assert original.size() == shuffled.size();
        final var size = original.size();
        Map<T, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            indexMap.put(shuffled.get(i), i);
        }

        int[] permutationIndex = new int[size];
        for (int i = 0; i < size; i++) {
            permutationIndex[i] = indexMap.get(original.get(i));
        }

        return permutationIndex;
    }
}
