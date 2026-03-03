package igentuman.nc.world.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.setup.registration.Registries.PLACEMENT_MODIFIER;

public class NCPlacementModifierTypes {
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<?>> HEIGHTMAP_CHUNK =
        PLACEMENT_MODIFIER.register("heightmap_chunk", () -> codec(HeightmapChunkPlacement.CODEC));

    private static <P extends PlacementModifier> PlacementModifierType<P> codec(MapCodec<P> mapCodec) {
        return () -> mapCodec;
    }

    public static void init() {
    }
}
