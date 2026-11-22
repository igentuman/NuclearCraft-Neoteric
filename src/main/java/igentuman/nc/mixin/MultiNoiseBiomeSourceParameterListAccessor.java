package igentuman.nc.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Updates the loaded biome parameters before world generation starts. */
@Mixin(MultiNoiseBiomeSourceParameterList.class)
public interface MultiNoiseBiomeSourceParameterListAccessor {
    @Mutable
    @Accessor("parameters")
    void nc$setParameters(Climate.ParameterList<Holder<Biome>> parameters);
}
