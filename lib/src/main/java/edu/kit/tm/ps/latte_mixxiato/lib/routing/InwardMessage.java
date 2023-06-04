package edu.kit.tm.ps.latte_mixxiato.lib.routing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

public record InwardMessage(long peerSeed, byte[] message) {
}
