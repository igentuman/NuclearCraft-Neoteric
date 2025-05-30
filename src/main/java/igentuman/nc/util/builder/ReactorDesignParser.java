package igentuman.nc.util.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.NC_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.NC_MATERIAL_BLOCKS;
import static net.minecraft.world.level.block.Blocks.AIR;

public class ReactorDesignParser {

    public static HashMap<BlockPos, Block> parseStructure(String input) {
        HashMap<BlockPos, Block> blockMap = new HashMap<>();

        JsonElement jsonElement = null;
        try {
            jsonElement = JsonParser.parseReader(new FileReader(input));
        } catch (FileNotFoundException e) {

        }

        try {
            if(jsonElement == null) {
                jsonElement = JsonParser.parseString(input);
            }

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            JsonObject compressedReactor = jsonObject.getAsJsonObject("CompressedReactor");

            for (Map.Entry<String, JsonElement> entry : compressedReactor.entrySet()) {
                String componentType = entry.getKey();
                JsonElement positionsElement = entry.getValue();

                if (!positionsElement.isJsonArray()) continue;

                positionsElement.getAsJsonArray().forEach(posObj -> {
                    JsonObject positionObject = posObj.getAsJsonObject();
                    int x = positionObject.get("X").getAsInt();
                    int y = positionObject.get("Y").getAsInt();
                    int z = positionObject.get("Z").getAsInt();

                    BlockPos pos = new BlockPos(x, y, z);
                    blockMap.put(pos, getBlock(componentType.toLowerCase()));
                });
            }

        } catch (JsonSyntaxException e) {
            debugLog("Invalid JSON syntax in input: " + e.getMessage());
        }

        return blockMap;
    }

    private static Block getBlock(String componentType) {
        if(componentType.equals("fuelcell")) {
            return FISSION_BLOCKS.get("fission_reactor_solid_fuel_cell").get();
        }
        if(FISSION_BLOCKS.containsKey(componentType + "_heat_sink")) {
            return FISSION_BLOCKS.get(componentType + "_heat_sink").get();
        }
        if(FISSION_BLOCKS.containsKey("liquid_" + componentType + "_heat_sink")) {
            return FISSION_BLOCKS.get("liquid_" + componentType + "_heat_sink").get();
        }
        if(NC_BLOCKS.containsKey(componentType + "_block")) {
            return NC_BLOCKS.get(componentType + "_block").get();
        }
        if(NC_MATERIAL_BLOCKS.containsKey(componentType)) {
            return NC_MATERIAL_BLOCKS.get(componentType).get();
        }
        return AIR;
    }

}
