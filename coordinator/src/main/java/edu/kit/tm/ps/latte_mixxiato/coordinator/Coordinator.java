package edu.kit.tm.ps.latte_mixxiato.coordinator;

import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.DeadDrop;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Gateway;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.mix.Relay;

import java.util.Optional;

public class Coordinator {

    private Gateway gateway;
    private Relay relay;
    private DeadDrop deadDrop;

    public Coordinator() {
        this.gateway = null;
        this.relay = null;
        this.deadDrop = null;
    }

    public void register(Gateway gateway) {
        this.gateway = gateway;
    }

    public void register(Relay relay) {
        this.relay = relay;
    }

    public void register(DeadDrop deadDrop) {
        this.deadDrop = deadDrop;
    }

    public boolean ready() {
        return gateway != null && relay != null && deadDrop != null;
    }

    public Optional<Gateway> gateway() {
        return Optional.ofNullable(gateway);
    }

    public Optional<Relay> relay() {
        return Optional.ofNullable(relay);
    }

    public Optional<DeadDrop> deadDrop() {
        return Optional.ofNullable(deadDrop);
    }
}
