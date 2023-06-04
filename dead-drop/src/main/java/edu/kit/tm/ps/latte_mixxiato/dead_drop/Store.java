package edu.kit.tm.ps.latte_mixxiato.dead_drop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class Store {

    private final List<BucketEntry> messages;
    private final Map<UUID, Set<BucketEntry>> buckets;

    public Store() {
        this.messages = new LinkedList<>();
        this.buckets = new HashMap<>();
    }

    public synchronized void put(BucketEntry entry) {
        messages.add(entry);
        buckets.putIfAbsent(entry.bucketId(), new HashSet<>());
        buckets.get(entry.bucketId()).add(entry);
    }

    public synchronized void clear() {
        messages.clear();
        buckets.clear();
    }

    public void forEach(Consumer<BucketEntry> action) {
        messages.forEach(action);
    }
}
