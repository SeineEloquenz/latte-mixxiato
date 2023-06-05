package edu.kit.tm.ps.latte_mixxiato.gateway.client;

import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxException;
import edu.kit.tm.ps.latte_mixxiato.lib.logging.LatteLogger;
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
    protected void decode(final ChannelHandlerContext context, final ByteBuf buffer, final List<Object> list) throws SphinxException {
        if (buffer.readableBytes() < client.params().packetLength()) {
            LatteLogger.get().warn("Got %s readable bytes but expected %s"
                    .formatted(buffer.readableBytes(), client.params().packetLength()));
            return;
        }
        final var bytes = ByteBufUtil.getBytes(buffer);
        buffer.skipBytes(buffer.readableBytes());
        final var packet = client.unpackMessage(bytes);
        list.add(packet);
    }
}
