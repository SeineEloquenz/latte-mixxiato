package edu.kit.tm.ps.latte_mixxiato.mix;

import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.ProcessedPacket;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.RelayInformation;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Router;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class MessageHandler extends ChannelInboundHandlerAdapter {

    private final SphinxNode node;
    private final Router router;

    public MessageHandler(final SphinxNode node, final Router router) {
        this.node = node;
        this.router = router;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws IOException {
        final var packet = (SphinxPacket) msg;
        final var processedPacket = node.sphinxProcess(packet.packetContent());
        final var flag = processedPacket.routingFlag();

        if (flag.equals(RoutingFlag.RELAY)) {
            final var relayInformation = router.findRelay(processedPacket);
            this.relay(relayInformation.node(), processedPacket);
        } else if (flag.equals(RoutingFlag.DESTINATION)) {
            final var outwardMessage = router.findForwardDestination(processedPacket);
            Logger.getGlobal().info("Sending message outward to %s".formatted(outwardMessage.address()));
            outwardMessage.send();
        }
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

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();//TODO implement actual error handling
        context.close();
    }
}
