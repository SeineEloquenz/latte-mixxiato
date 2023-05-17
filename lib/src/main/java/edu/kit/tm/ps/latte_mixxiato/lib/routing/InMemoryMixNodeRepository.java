package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class InMemoryMixNodeRepository implements MixNodeRepository {
    private final Map<Integer, MixNode> nodes;

    public InMemoryMixNodeRepository() {
        this.nodes = new HashMap<>();
    }

    public void put(int id, MixNode node) {
        nodes.put(id, node);
    }

    @Override
    public MixNode byId(int nodeId) {
        return nodes.get(nodeId);
    }

    @Override
    public Set<MixNode> all() {
        return new HashSet<>(nodes.values());
    }
}
