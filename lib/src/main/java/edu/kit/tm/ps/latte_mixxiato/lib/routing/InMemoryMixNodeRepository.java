package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryMixNodeRepository implements MixNodeRepository {
    private final Map<MixType, MixNode> nodes;

    public InMemoryMixNodeRepository() {
        this.nodes = new ConcurrentHashMap<>();
    }

    public void put(MixNode node) {
        nodes.put(node.type(), node);
    }

    @Override
    public MixNode byType(MixType type) {
        return nodes.get(type);
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
