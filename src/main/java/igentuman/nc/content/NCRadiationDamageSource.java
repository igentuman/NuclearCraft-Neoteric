package igentuman.nc.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

import static igentuman.nc.NuclearCraft.rl;

public class NCRadiationDamageSource {
    public static void init() {

    }

    public static ResourceKey<DamageType> RADIATION_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, rl("radiation"));
    public static final DamageSource RADIATION(ServerPlayer player) {
        return RADIATION(player.level());
    }

    public static final DamageSource RADIATION(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RADIATION_TYPE));
    }
}
