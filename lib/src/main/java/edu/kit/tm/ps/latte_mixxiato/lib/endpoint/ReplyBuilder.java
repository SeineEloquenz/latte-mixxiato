package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import com.robertsoultanaev.javasphinx.SerializationUtils;
import com.robertsoultanaev.javasphinx.SphinxClient;
import com.robertsoultanaev.javasphinx.SphinxException;
import com.robertsoultanaev.javasphinx.packet.SphinxPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.DeadDrop;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.Relay;

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
