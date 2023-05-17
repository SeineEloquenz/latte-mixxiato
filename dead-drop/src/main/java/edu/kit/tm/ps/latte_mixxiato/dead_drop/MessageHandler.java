package edu.kit.tm.ps.latte_mixxiato.dead_drop;

import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Packet;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Receiver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Logger;

public class MessageHandler extends ChannelInboundHandlerAdapter {

    private final Receiver receiver;

    public MessageHandler(final Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) {
        final var packet = (Packet) msg;
        Logger.getGlobal().info("Received packet for message %s with sequence number %s".formatted(packet.uuid(), packet.sequenceNumber()));
        receiver.receive(packet);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();//TODO implement actual error handling
        context.close();
    }
}
