package edu.kit.tm.ps.latte_mixxiato.coordinator;

import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixType;
import org.bouncycastle.math.ec.ECPoint;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Coordinator {

    private final Map<MixType, MixNode> mixes;

    public Coordinator() {
        this.mixes = new ConcurrentHashMap<>();
    }

    public MixNode register(MixType type, String host, int port, ECPoint pubKey) {
        final var node = new MixNode(type, host, port, pubKey);
        mixes.put(type, node);
        return node;
    }

    public Optional<MixNode> getNode(MixType type) {
        return Optional.ofNullable(mixes.get(type));
    }

    public Set<MixNode> all() {
        return new HashSet<>(mixes.values());
    }
}
