package edu.kit.tm.ps.latte_mixxiato.lib.routing;

public record InwardMessage(int replyPort, byte[] message) {
}
