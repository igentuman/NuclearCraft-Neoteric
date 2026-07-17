package igentuman.nc.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JSONUtil {

    /**
     * Load all JSON files from a specific subfolder in the config directory.
     * @param subfolder
     * @return
     */
    public static List<JsonArray> loadAllJsonFromConfig(String subfolder) {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve("NuclearCraft").resolve(subfolder);

        List<JsonArray> jsonArrayList = new ArrayList<>();

        File[] jsonFiles = configDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (jsonFiles == null || jsonFiles.length == 0) {
            System.err.println("No JSON files found in config directory.");
            return null;
        }

        for (File jsonFile : jsonFiles) {
            try (FileReader reader = new FileReader(jsonFile)) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                if (jsonElement.isJsonArray()) {
                    jsonArrayList.add(jsonElement.getAsJsonArray());
                } else {
                    System.err.println("File " + jsonFile.getName() + " does not contain a JSON array.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return jsonArrayList;
    }
}
