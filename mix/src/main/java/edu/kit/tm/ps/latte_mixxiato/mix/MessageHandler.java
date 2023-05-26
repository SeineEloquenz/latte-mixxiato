package edu.kit.tm.ps.latte_mixxiato.mix;

import com.robertsoultanaev.javasphinx.SphinxNode;
import com.robertsoultanaev.javasphinx.packet.RoutingFlag;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Router;
import edu.kit.tm.ps.latte_mixxiato.mix.dispatcher.Dispatcher;
import edu.kit.tm.ps.latte_mixxiato.mix.dispatcher.SynchronizingDispatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.util.logging.Logger;

public class MessageHandler extends ChannelInboundHandlerAdapter {

    private final SphinxNode node;
    private final Router router;
    private final Dispatcher dispatcher;

    public MessageHandler(final SphinxNode node, final Router router, final Dispatcher dispatcher) {
        this.node = node;
        this.router = router;
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws IOException {
        final var packet = (SphinxPacket) msg;
        final var processedPacket = node.sphinxProcess(packet.packetContent());
        final var flag = processedPacket.routingFlag();

        if (flag.equals(RoutingFlag.RELAY)) {
            final var relayInformation = router.findRelay(processedPacket);
            dispatcher.dispatch(relayInformation, processedPacket);
        } else if (flag.equals(RoutingFlag.DESTINATION)) {
            final var outwardMessage = router.findForwardDestination(processedPacket);
            Logger.getGlobal().info("Sending message outward to %s".formatted(outwardMessage.address()));
            outwardMessage.send();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();//TODO implement actual error handling
        context.close();
    }
}
