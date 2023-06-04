package edu.kit.tm.ps.latte_mixxiato.gateway.routing;

import java.util.LinkedList;
import java.util.Queue;

public class ClientList {

    private final Queue<ClientData> clients;

    public ClientList() {
        this.clients = new LinkedList<>();
    }

    public void record(ClientData clientData) {
        clients.add(clientData);
    }

    public ClientData pop() {
        return clients.poll();
    }

    public void clear() {
        clients.clear();
    }
}
