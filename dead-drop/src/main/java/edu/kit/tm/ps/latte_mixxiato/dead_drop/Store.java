package edu.kit.tm.ps.latte_mixxiato.dead_drop;

import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Packet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Stack;
import java.util.UUID;

public class Store {

    private final Queue<BucketEntry> messages;
    private final Map<UUID, Stack<BucketEntry>> buckets;

    public Store() {
        this.messages = new LinkedList<>();
        this.buckets = new HashMap<>();
    }

    public void add(BucketEntry entry) {
        messages.add(entry);
        buckets.putIfAbsent(entry.bucketId(), new Stack<>());
        buckets.get(entry.bucketId()).add(entry);
    }

    public Packet popReply() {
        final var reply = Optional.ofNullable(messages.poll())
                .map(head -> {
                    final var replyToBe = buckets.get(head.bucketId()).pop();
                    assert replyToBe != null;
                    return replyToBe;
                })
                .map(BucketEntry::packet)
                .orElse(null);
        if (this.size() == 0) {
            this.clear();
        }
        return reply;
    }

    private void clear() {
        messages.clear();
        buckets.clear();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public int size() {
        return messages.size();
    }
}
