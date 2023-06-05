package edu.kit.tm.ps.latte_mixxiato.client;

import com.robertsoultanaev.javasphinx.SphinxException;
import edu.kit.tm.ps.latte_mixxiato.lib.endpoint.Sender;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.InwardMessage;

import java.nio.charset.StandardCharsets;

public class Client {

    private final Sender sender;

    public Client(final Sender sender) {
        this.sender = sender;
    }

    public void sendMessage(String message) throws SphinxException {
        sender.enqueueToSend(new InwardMessage(message.getBytes(StandardCharsets.UTF_8)));
    }
}
