package edu.kit.tm.ps.latte_mixxiato.mix.dispatcher;

import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.ProcessedPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.RelayInformation;

import java.io.IOException;
import java.util.logging.Logger;

public class AsapDispatcher implements Dispatcher {

    private final SphinxNode node;

    public AsapDispatcher(final SphinxNode node) {
        this.node = node;
    }

    @Override
    public void dispatch(RelayInformation info, ProcessedPacket packet) {
        this.relay(info.node(), packet);
        Logger.getGlobal().info("Dispatched packet.");
    }

    private void relay(MixNode destination, ProcessedPacket processedPacket) {
        try {
            destination.send(node.client(), node.repack(processedPacket));
            Logger.getGlobal().info("Relayed packet to node %s".formatted(destination.id()));
        } catch (IOException e) {
            Logger.getGlobal().warning("Failed to relay packet to node %s".formatted(destination.id()));
            throw new RuntimeException(e);
        }
    }
}
