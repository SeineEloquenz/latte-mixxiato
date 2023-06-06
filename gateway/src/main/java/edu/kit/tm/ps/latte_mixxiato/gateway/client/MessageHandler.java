package edu.kit.tm.ps.latte_mixxiato.gateway.client;

import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import edu.kit.tm.ps.latte_mixxiato.gateway.routing.ClientData;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MessageHandler extends ChannelInboundHandlerAdapter {

    private final SphinxNode node;
    private final SynchronizingDispatcher dispatcher;

    public MessageHandler(final SphinxNode node, final SynchronizingDispatcher dispatcher) {
        this.node = node;
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws IOException, SphinxException {
        final var packet = (SphinxPacket) msg;
        final var processedPacket = node.sphinxProcess(packet.packetContent());
        final var flag = processedPacket.routingFlag();

        if (flag.equals(RoutingFlag.RELAY)) {
            final InetSocketAddress senderAddress = (InetSocketAddress) context.channel().remoteAddress();
            final var replyPort = processedPacket.relayInfo().additionalInfo();
            dispatcher.dispatch(new ClientData(senderAddress.getHostString(), replyPort), processedPacket);
        } else {
            LatteLogger.get().warn("Found wrong flag %s".formatted(flag));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();//TODO implement actual error handling
        context.close();
    }
}
