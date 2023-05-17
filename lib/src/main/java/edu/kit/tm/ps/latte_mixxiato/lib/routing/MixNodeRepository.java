package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import java.util.Set;

public interface MixNodeRepository {
    MixNode byId(int nodeId);

    Set<MixNode> all();
}
