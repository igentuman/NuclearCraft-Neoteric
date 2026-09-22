package igentuman.nc.multiblock.accelerator;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static igentuman.nc.NuclearCraft.rl;

public final class AcceleratorTags {

    public static final TagKey<Block> CASING = TagKey.create(Registries.BLOCK, rl("accelerator_casing"));
    public static final TagKey<Block> INNER = TagKey.create(Registries.BLOCK, rl("accelerator_inner"));
    public static final TagKey<Block> COOLERS = TagKey.create(Registries.BLOCK, rl("accelerator_coolers"));
    public static final TagKey<Block> BEAM_PORTS = TagKey.create(Registries.BLOCK, rl("particle_beam_ports"));

    private AcceleratorTags() {
    }
}
