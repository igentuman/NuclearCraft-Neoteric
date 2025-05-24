package igentuman.nc.world.placement;

import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.setup.registration.Registries.PLACEMENT_MODIFIER;

public class NCPlacementModifierTypes {
    public static final RegistryObject<PlacementModifierType<HeightmapChunkPlacement>> HEIGHTMAP_CHUNK =
        PLACEMENT_MODIFIER.register("heightmap_chunk", () ->
            () -> HeightmapChunkPlacement.CODEC
        );

    public static void init() {
    }
}
