package edu.kit.tm.ps.latte_mixxiato.coordinator;

import edu.kit.tm.ps.latte_mixxiato.lib.routing.InMemoryMixNodeRepository;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import org.bouncycastle.math.ec.ECPoint;

import java.util.Set;

public class Coordinator {

    private final InMemoryMixNodeRepository mixNodeRepository;

    public Coordinator() {
        this.mixNodeRepository = new InMemoryMixNodeRepository();
    }

    public Set<MixNode> all() {
        return mixNodeRepository.all();
    }
    public MixNode register(String host, int port, ECPoint pubKey) throws MixSetFullException {
        if (mixNodeRepository.size() >= 3) {
            throw new MixSetFullException();
        }
        final var mix = new MixNode(mixNodeRepository.size(), host, port, pubKey);
        mixNodeRepository.put(mix);
        return mix;
    }
}
