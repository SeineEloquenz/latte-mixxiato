package edu.kit.tm.ps.latte_mixxiato.lib.endpoint;

import java.util.UUID;

public record AssembledMessage(UUID uuid, byte[] messageBody) {
}