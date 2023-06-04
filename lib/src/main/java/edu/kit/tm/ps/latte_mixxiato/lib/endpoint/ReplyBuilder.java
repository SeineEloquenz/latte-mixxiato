package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SerializationUtils;
import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import com.robertsoultanaev.javasphinx.packet.header.PacketContent;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.DeadDrop;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Relay;

import java.util.UUID;

public class ReplyBuilder extends Endpoint {

    public ReplyBuilder(final Gateway gateway, final Relay relay, final DeadDrop deadDrop, final SphinxClient client) {
        super(gateway, relay, deadDrop, client);
    }

    public SphinxPacket repackReply(Packet packet) throws SphinxException {
        byte[] dest = SerializationUtils.encodeLong(UUID.randomUUID().getMostSignificantBits());
        return createSphinxPacket(dest, packet.toBytes(), outboundRoutingInformation());
    }
}
