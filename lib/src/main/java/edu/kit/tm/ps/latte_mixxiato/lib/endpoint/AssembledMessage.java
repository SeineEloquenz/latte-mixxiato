package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record AssembledMessage(UUID uuid, byte[] messageBody) {
    public String messageContent() {
        return new String(messageBody, StandardCharsets.UTF_8);
    }
}