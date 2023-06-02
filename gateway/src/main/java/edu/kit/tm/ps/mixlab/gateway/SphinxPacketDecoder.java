package edu.kit.tm.ps.mixlab.gateway;

import com.robertsoultanaev.javasphinx.SphinxClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.logging.Logger;

public class SphinxPacketDecoder extends ByteToMessageDecoder {

    private final SphinxClient client;

    public SphinxPacketDecoder(final SphinxClient client) {
        this.client = client;
    }

    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf buffer, final List<Object> list) {
        if (buffer.readableBytes() < client.params().packetLength()) {
            Logger.getGlobal().warning("Got %s readable bytes but expected %s"
                    .formatted(buffer.readableBytes(), client.params().packetLength()));
            return;
        }
        final var bytes = ByteBufUtil.getBytes(buffer);
        buffer.skipBytes(buffer.readableBytes());
        final var packet = client.unpackMessage(bytes);
        list.add(packet);
    }
}
