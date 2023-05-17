package edu.kit.tm.ps.latte_mixxiato.lib.coordinator;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;

public record CoordinatorConfig(String host, int port) {

    public static final String CONFIG_PATH = "coord.json";

    public static CoordinatorConfig load() throws FileNotFoundException {
        final var json = loadJson();
        final var host = json.get("host").getAsString();
        final var port = json.get("port").getAsInt();
        return new CoordinatorConfig(host, port);
    }

    private static JsonObject loadJson() throws FileNotFoundException {
        return JsonParser.parseReader(new FileReader(CONFIG_PATH)).getAsJsonObject();
    }
}
