package edu.kit.tm.ps.latte_mixxiato.mix.dispatcher;

import com.robertsoultanaev.javasphinx.packet.ProcessedPacket;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.RelayInformation;

public interface Dispatcher {
    void dispatch(RelayInformation info, ProcessedPacket packet);
}
