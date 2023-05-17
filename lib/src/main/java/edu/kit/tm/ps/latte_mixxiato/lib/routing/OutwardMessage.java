package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public record OutwardMessage(InetSocketAddress address, byte[] message) {

    public void send() throws IOException {
        try (final var socket = new Socket(address.getAddress(), address.getPort())) {
            final var os = socket.getOutputStream();
            os.write(message);
        }
    }
}
