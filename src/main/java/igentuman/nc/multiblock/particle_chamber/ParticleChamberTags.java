package igentuman.nc.multiblock.particle_chamber;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static igentuman.nc.NuclearCraft.rl;

public final class ParticleChamberTags {

    public static final TagKey<Block> TARGET_CASING = TagKey.create(Registries.BLOCK, rl("target_chamber_casing"));
    public static final TagKey<Block> DECAY_CASING = TagKey.create(Registries.BLOCK, rl("decay_chamber_casing"));
    public static final TagKey<Block> COLLISION_CASING = TagKey.create(Registries.BLOCK, rl("collision_chamber_casing"));
    public static final TagKey<Block> INNER = TagKey.create(Registries.BLOCK, rl("target_chamber_inner"));
    public static final TagKey<Block> DETECTORS = TagKey.create(Registries.BLOCK, rl("particle_chamber_detectors"));

    private ParticleChamberTags() {
    }
}
