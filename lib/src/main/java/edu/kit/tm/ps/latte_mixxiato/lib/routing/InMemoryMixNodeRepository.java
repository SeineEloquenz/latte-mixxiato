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

    public void put(MixNode node) {
        nodes.put(node.id(), node);
    }

    @Override
    public MixNode byId(int nodeId) {
        return nodes.get(nodeId);
    }

    @Override
    public Set<MixNode> all() {
        return new HashSet<>(nodes.values());
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public void clear() {
        nodes.clear();
    }
}
