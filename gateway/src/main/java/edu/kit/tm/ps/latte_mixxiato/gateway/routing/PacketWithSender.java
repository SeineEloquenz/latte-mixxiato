package edu.kit.tm.ps.latte_mixxiato.gateway.routing;

import com.robertsoultanaev.javasphinx.packet.SphinxPacket;

public record PacketWithSender(ClientData clientData, SphinxPacket packet) {
}
