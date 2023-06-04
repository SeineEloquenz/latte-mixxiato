package edu.kit.tm.ps.latte_mixxiato.client;

import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Sender;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.InwardMessage;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Client {

    private final Sender sender;

    public Client(final Sender sender) {
        this.sender = sender;
    }

    public void sendMessage(String message) {
        sender.enqueueToSend(new InwardMessage(message.getBytes(StandardCharsets.UTF_8)));
    }
}
