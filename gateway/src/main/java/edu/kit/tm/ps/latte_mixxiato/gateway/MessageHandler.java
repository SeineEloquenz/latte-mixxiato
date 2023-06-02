package edu.kit.tm.ps.latte_mixxiato.gateway;

import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.util.logging.Logger;

public class MessageHandler extends ChannelInboundHandlerAdapter {

    private final SphinxNode node;
    private final SynchronizingDispatcher dispatcher;

    public MessageHandler(final SphinxNode node, final SynchronizingDispatcher dispatcher) {
        this.node = node;
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws IOException {
        final var packet = (SphinxPacket) msg;
        final var processedPacket = node.sphinxProcess(packet.packetContent());
        final var flag = processedPacket.routingFlag();

        if (flag.equals(RoutingFlag.RELAY)) {
            dispatcher.dispatch(processedPacket);
        } else {
            Logger.getGlobal().warning("Found wrong flag %s".formatted(flag));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();//TODO implement actual error handling
        context.close();
    }
}
