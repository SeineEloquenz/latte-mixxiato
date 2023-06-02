package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import java.util.Set;

public interface MixNodeRepository {

    public static final int DESIRED_MIX_AMOUNT = 3;
    MixNode byType(MixType type);

    Set<MixNode> all();

    int size();
    void clear();
}
