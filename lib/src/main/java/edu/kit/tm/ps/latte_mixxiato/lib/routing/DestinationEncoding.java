package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import com.robertsoultanaev.javasphinx.SerializationUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class DestinationEncoding {

    private static final int ADDRESS_LENGTH = 4;
    private static final int PORT_LENGTH = 4;

    public static byte[] encode(InetSocketAddress address) {
        return SerializationUtils.concatenate(address.getAddress().getAddress(), SerializationUtils.encodeInt(address.getPort()));
    }

    public static InetSocketAddress decode(byte[] destination) {
        final var addressBytes = SerializationUtils.slice(destination, ADDRESS_LENGTH);
        final var port = SerializationUtils.slice(destination, ADDRESS_LENGTH, ADDRESS_LENGTH + PORT_LENGTH);
        try {
            return new InetSocketAddress(InetAddress.getByAddress(addressBytes), SerializationUtils.decodeInt(port));
        } catch (UnknownHostException e) {
            System.out.println("Illegal IP address decoded!"); //TODO actually handle this correctly
            throw new RuntimeException(e);
        }
    }
}
