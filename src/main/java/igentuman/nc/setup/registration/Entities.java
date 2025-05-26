package igentuman.nc.setup.registration;

import igentuman.nc.entity.EntityFeralGhoul;
import igentuman.nc.entity.EntityWastelandBoss;
import igentuman.nc.entity.EntityWastelandProjectile;
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
    
    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<EntityWastelandBoss>> FERAL_GHOUL_BOSS =
            Registries.ENTITIES.register("feral_ghoul_boss",
                    () -> EntityType.Builder.<EntityWastelandBoss>of(EntityWastelandBoss::new, MobCategory.MONSTER)
                            .sized(1.3f, 2.9f)  // 50% larger than regular ghoul
                            .fireImmune()       // Boss is immune to fire damage
                            .build("feral_ghoul_boss"));

    // Wasteland projectile entity
    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<EntityWastelandProjectile>> WASTELAND_PROJECTILE =
            Registries.ENTITIES.register("wasteland_projectile",
                    () -> EntityType.Builder.<EntityWastelandProjectile>of((type, level) ->
                            new EntityWastelandProjectile(type, level), MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(4)
                            .updateInterval(5)
                            .build("wasteland_projectile"));

    public static void registerSpawnPlacements() {
        SpawnPlacements.register(FERAL_GHOUL.get(), 
                SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EntityFeralGhoul::checkFeralGhoulSpawnRules);

        SpawnPlacements.register(FERAL_GHOUL_BOSS.get(),
                SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EntityWastelandBoss::checkFeralGhoulBossSpawnRules);
    }
    
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(FERAL_GHOUL.get(), EntityFeralGhoul.createAttributes().build());
        event.put(FERAL_GHOUL_BOSS.get(), EntityWastelandBoss.createAttributes().build());
    }
}
