package edu.kit.tm.ps.latte_mixxiato.client;

import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Sender;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.OutwardMessage;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Client {

    private final Sender sender;

    public Client(final Sender sender) {
        this.sender = sender;
    }

    public void sendMessage(InetSocketAddress recipient, String message) {
        sender.enqueueToSend(new OutwardMessage(recipient, message.getBytes(StandardCharsets.UTF_8)));
    }
}
