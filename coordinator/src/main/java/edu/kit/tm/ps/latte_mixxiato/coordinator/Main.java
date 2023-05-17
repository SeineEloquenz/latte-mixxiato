package edu.kit.tm.ps.latte_mixxiato.coordinator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.InMemoryMixNodeRepository;
import edu.kit.tm.ps.latte_mixxiato.lib.routing.MixNode;
import spark.Spark;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1) {
            Logger.getGlobal().severe("You need to pass the path to the mix config json");
            System.exit(1);
        }
        final var mixes = JsonParser.parseReader(new FileReader(args[0])).getAsJsonArray();
        final var mixNodeRepository = new InMemoryMixNodeRepository();
        mixes.asList().stream()
                .map(JsonElement::getAsJsonObject)
                .map(MixNode::fromJson)
                .forEach(node -> mixNodeRepository.put(node.id(), node));
        Logger.getGlobal().info("Loaded mix data from disk.");
        Spark.get("/api/mixes/all", (req, res) -> {
            final var result = new JsonArray();
            mixNodeRepository.all().stream()
                    .map(MixNode::toJson).toList()
                    .forEach(result::add);
            return result;
        });
        Logger.getGlobal().info("Listening for requests...");
    }
}