package edu.kit.tm.ps.latte_mixxiato.lib.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LatteLogger {

    private LatteLogger() {

    }

    public static Logger get() {
        return LoggerFactory.getLogger("");
    }
}
