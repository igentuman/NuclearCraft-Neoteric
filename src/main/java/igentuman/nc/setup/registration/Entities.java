package igentuman.nc.setup.registration;

import igentuman.nc.entity.EntityFeralGhoul;
import igentuman.nc.entity.EntityWastelandBoss;
import igentuman.nc.entity.EntityWastelandProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import igentuman.api.platform.NCSpawning;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import igentuman.api.platform.NCRegistration;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.NuclearCraft.MODID;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
public class Entities {

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFeralGhoul>> FERAL_GHOUL =
            NCRegistration.registerEntity(Registries.ENTITIES, "feral_ghoul",
                    () -> EntityType.Builder.<EntityFeralGhoul>of(EntityFeralGhoul::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f)
                            .build("feral_ghoul"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityWastelandBoss>> FERAL_GHOUL_BOSS =
            NCRegistration.registerEntity(Registries.ENTITIES, "feral_ghoul_boss",
                    () -> EntityType.Builder.<EntityWastelandBoss>of(EntityWastelandBoss::new, MobCategory.MONSTER)
                            .sized(1.3f, 2.9f)  // 50% larger than regular ghoul
                            .fireImmune()       // Boss is immune to fire damage
                            .build("feral_ghoul_boss"));

    // Wasteland projectile entity
    public static final DeferredHolder<EntityType<?>, EntityType<EntityWastelandProjectile>> WASTELAND_PROJECTILE =
            NCRegistration.registerEntity(Registries.ENTITIES, "wasteland_projectile",
                    () -> EntityType.Builder.<EntityWastelandProjectile>of((type, level) ->
                            new EntityWastelandProjectile(type, level), MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(4)
                            .updateInterval(5)
                            .build("wasteland_projectile"));

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        NCSpawning.register(event, FERAL_GHOUL.get(),
                NCSpawning.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EntityFeralGhoul::checkFeralGhoulSpawnRules);

        NCSpawning.register(event, FERAL_GHOUL_BOSS.get(),
                NCSpawning.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EntityWastelandBoss::checkFeralGhoulBossSpawnRules);
    }
    
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(FERAL_GHOUL.get(), EntityFeralGhoul.createAttributes().build());
        event.put(FERAL_GHOUL_BOSS.get(), EntityWastelandBoss.createAttributes().build());
    }
}
