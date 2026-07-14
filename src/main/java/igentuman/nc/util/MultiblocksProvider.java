package igentuman.nc.util;

import igentuman.nc.NuclearCraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static igentuman.nc.NuclearCraft.rlFromString;

/** Reload listener that loads, validates, and serves multiblock structure templates from resources. */
public class MultiblocksProvider implements PreparableReloadListener {

    public static List<MultiblockStructure> structures = new ArrayList<>();
    private static MultiblocksProvider INSTANCE = new MultiblocksProvider();

    public static MultiblocksProvider getInstance() {
        return INSTANCE;
    }

    public static List<MultiblockStructure> getStructures() {
        return structures;
    }
    
    /**
     * Sets the structures list. Used for client-side synchronization.
     * @param newStructures The new structures to set
     */
    public static void setStructures(List<MultiblockStructure> newStructures) {
        structures.clear();

        structures.addAll(newStructures);
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, 
                                          ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, 
                                          Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            return loadMultiblockStructures(resourceManager);
        }, backgroundExecutor).thenCompose(preparationBarrier::wait).thenAcceptAsync(loadedStructures -> {
            structures.clear();
            structures.addAll(loadedStructures);
        }, gameExecutor);
    }

    private static List<MultiblockStructure> loadMultiblockStructures(ResourceManager resourceManager) {
        List<MultiblockStructure> loadedStructures = new ArrayList<>();
        loadedStructures.addAll(loadFromLocation(resourceManager, "structures"));
        return loadedStructures;
    }

    private static List<MultiblockStructure> loadFromLocation(ResourceManager resourceManager, String structures) {
        List<MultiblockStructure> tmp = new ArrayList<>();
        // Get all .nbt files from the structures directory
        Map<ResourceLocation, Resource> structureFiles = resourceManager.listResources(structures,
                location -> location.getPath().endsWith(".nbt"));

        for (Map.Entry<ResourceLocation, Resource> entry : structureFiles.entrySet()) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            try {
                CompoundTag nbt = NbtIo.readCompressed(resource.open(), NbtAccounter.unlimitedHeap());

                // Validate that all blocks in the structure exist
                if (validateStructureBlocks(nbt)) {
                    String fileName = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
                    tmp.add(new MultiblockStructure(location, nbt, fileName));
                } else {
                    System.out.println("Skipping structure " + location + " due to missing blocks");
                }

            } catch (IOException e) {
                System.err.println("Failed to load structure from " + location + ": " + e.getMessage());
            }
        }

        return tmp;
    }


    /**
     * @deprecated Use getStructures() instead. This method is kept for backward compatibility.
     */
    @Deprecated
    public static List<MultiblockStructure> loadMultiblockStructures() {
        return getStructures();
    }

    /**
     * Loads a single structure by name directly from the classpath JAR. Used as a fallback when
     * the reload-listener-provided list is empty (e.g. on the client before world join, or in
     * JEI/EMI plugin init).
     *
     * @param name structure name without extension; resolves to
     *             {@code /data/<modid>/structures/<name>.nbt}
     * @return loaded structure, or null if file missing or invalid
     */
    public static MultiblockStructure loadStructureFromClasspath(String name) {
        String path = "/data/" + NuclearCraft.MODID + "/structures/" + name + ".nbt";
        try (InputStream is = MultiblocksProvider.class.getResourceAsStream(path)) {
            if (is == null) return null;
            CompoundTag nbt = NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
            if (!validateStructureBlocks(nbt)) return null;
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(NuclearCraft.MODID,
                    "structures/" + name + ".nbt");
            return new MultiblockStructure(rl, nbt, name + ".nbt");
        } catch (IOException e) {
            System.err.println("Failed to load structure " + name + " from classpath: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validates that all blocks referenced in the structure NBT exist in the registry
     *
     * @param nbt The structure NBT data
     * @return true if all blocks exist, false otherwise
     */
    private static boolean validateStructureBlocks(CompoundTag nbt) {
        if (!nbt.contains("palette", Tag.TAG_LIST)) {
            return false; // No palette means no blocks to validate
        }

        ListTag palette = nbt.getList("palette", Tag.TAG_COMPOUND);

        for (int i = 0; i < palette.size(); i++) {
            CompoundTag blockState = palette.getCompound(i);
            String blockId = blockState.getString("Name");

            if (blockId.isEmpty()) {
                continue; // Skip empty block names
            }

            try {
                ResourceLocation blockLocation = rlFromString(blockId);
                if (!BuiltInRegistries.BLOCK.containsKey(blockLocation)) {
                    System.out.println("Missing block in structure: " + blockId);
                    return false;
                }
            } catch (Exception e) {
                System.err.println("Error validating block " + blockId + ": " + e.getMessage());
                return false;
            }
        }

        return true;
    }

}
