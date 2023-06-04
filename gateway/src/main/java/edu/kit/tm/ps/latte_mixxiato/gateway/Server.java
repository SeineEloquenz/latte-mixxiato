package edu.kit.tm.ps.latte_mixxiato.gateway;

import com.robertsoultanaev.javasphinx.SphinxException;
import edu.kit.tm.ps.latte_mixxiato.gateway.client.ClientGateway;
import edu.kit.tm.ps.latte_mixxiato.gateway.relay.RelayGateway;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final ClientGateway clientGateway;
    private final RelayGateway relayGateway;
    private final ExecutorService service;

    public Server(final ClientGateway clientGateway, final RelayGateway relayGateway) {
        this.clientGateway = clientGateway;
        this.relayGateway = relayGateway;
        this.service = Executors.newFixedThreadPool(2);
    }

    public void run() {
        service.submit(() -> {
            try {
                clientGateway.listen();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        service.submit(() -> {
            try {
                relayGateway.listen();
            } catch (IOException | SphinxException e) {
                e.printStackTrace();
            }
        });
    }

}
