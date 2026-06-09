package igentuman.nc.setup.registration;

import igentuman.nc.block.bomb.entity.PrimedFissionBombEntity;
import igentuman.nc.entity.EntityFeralGhoul;
import igentuman.nc.entity.EntityWastelandBoss;
import igentuman.nc.entity.EntityBlockProjectile;
import igentuman.nc.entity.Q36EnergyFlash;
import igentuman.nc.entity.Q36PulseProjectile;
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
    public static final RegistryObject<EntityType<EntityBlockProjectile>> BLOCK_PROJECTILE =
            Registries.ENTITIES.register("wasteland_projectile",
                    () -> EntityType.Builder.<EntityBlockProjectile>of((type, level) ->
                            new EntityBlockProjectile(type, level), MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .build("wasteland_projectile"));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<Q36PulseProjectile>> Q36_PULSE_PROJECTILE =
            Registries.ENTITIES.register("q36_pulse_projectile",
                    () -> EntityType.Builder.<Q36PulseProjectile>of(Q36PulseProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("q36_pulse_projectile"));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<Q36EnergyFlash>> Q36_ENERGY_FLASH =
            Registries.ENTITIES.register("q36_energy_flash",
                    () -> EntityType.Builder.<Q36EnergyFlash>of(Q36EnergyFlash::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .noSummon()
                            .clientTrackingRange(8)
                            .updateInterval(20)
                            .build("q36_energy_flash"));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<PrimedFissionBombEntity>> PRIMED_FISSION_BOMB =
            Registries.ENTITIES.register("primed_fission_bomb",
                    () -> EntityType.Builder.<PrimedFissionBombEntity>of(PrimedFissionBombEntity::new, MobCategory.MISC)
                            .sized(0.98F, 0.98F)
                            .noSummon()
                            .fireImmune()
                            .clientTrackingRange(16)
                            .updateInterval(20)
                            .build("primed_fission_bomb"));

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
