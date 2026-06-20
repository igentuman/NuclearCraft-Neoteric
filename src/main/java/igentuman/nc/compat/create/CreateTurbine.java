package igentuman.nc.compat.create;

import igentuman.nc.block.turbine.TurbineBearingKineticBlock;
import igentuman.nc.block.turbine.entity.TurbineBearingKineticBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.multiblock.turbine.TurbineRegistration.*;
import static igentuman.nc.setup.registration.Registries.BLOCK_ENTITIES;

/**
 * All references to Create's kinetic classes live here so the core turbine
 * classes never force-load them. Every method is only reached behind an
 * {@code isCreateLoaded()} guard; resolution of this class (and the Create
 * types it touches) stays lazy until Create is actually present.
 */
public class CreateTurbine {

    public static void registerBearing() {
        RegistryObject<Block> bearing = addBlock("turbine_bearing", () -> new TurbineBearingKineticBlock(TURBINE_BLOCKS_PROPERTIES));
        TURBINE_BE.put("turbine_bearing",
                BLOCK_ENTITIES.register("turbine_bearing",
                        () -> BlockEntityType.Builder.of(TurbineBearingKineticBE::new, bearing.get())
                                .build(null)));
    }

    public static void tickBearing(BlockEntity be) {
        if (be instanceof TurbineBearingKineticBE tile) {
            tile.tick();
        }
    }

    public static void applyKinetic(BlockEntity be, float rpm, float stressCapacity) {
        if (be instanceof TurbineBearingKineticBE bearingBE) {
            bearingBE.setStressCapacity(stressCapacity);
            bearingBE.setRPM(rpm);
        }
    }
}
