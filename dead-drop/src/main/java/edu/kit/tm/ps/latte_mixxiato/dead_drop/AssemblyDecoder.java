package edu.kit.tm.ps.latte_mixxiato.dead_drop;

import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Endpoint;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class AssemblyDecoder extends ByteToMessageDecoder {

    private final Endpoint endpoint;

    public AssemblyDecoder(final Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf buffer, final List<Object> list) {
        final var bytes = ByteBufUtil.getBytes(buffer);
        buffer.skipBytes(buffer.readableBytes());
        final var message = endpoint.parseMessageToPacket(bytes);
        list.add(message);
    }
}
