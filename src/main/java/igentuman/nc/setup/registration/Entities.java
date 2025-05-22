package igentuman.nc.setup.registration;

import igentuman.nc.entity.EntityFeralGhoul;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Entities {

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<EntityFeralGhoul>> FERAL_GHOUL =
            Registries.ENTITIES.register("feral_ghoul", 
                    () -> EntityType.Builder.<EntityFeralGhoul>of(EntityFeralGhoul::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f)
                            .build("feral_ghoul"));
    
    public static void registerSpawnPlacements() {
        SpawnPlacements.register(FERAL_GHOUL.get(), 
                SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EntityFeralGhoul::checkFeralGhoulSpawnRules);
    }
    
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(FERAL_GHOUL.get(), EntityFeralGhoul.createAttributes().build());
    }
}
