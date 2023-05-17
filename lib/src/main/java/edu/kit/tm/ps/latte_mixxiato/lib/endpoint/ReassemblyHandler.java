package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

@FunctionalInterface
public interface ReassemblyHandler {
    void onReassembly(AssembledMessage message);
}
