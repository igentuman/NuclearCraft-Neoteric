package igentuman.nc.setup.registration;

import igentuman.nc.block.bomb.entity.PrimedFissionBombEntity;
import igentuman.nc.entity.EntityFeralGhoul;
import igentuman.nc.entity.EntityWastelandBoss;
import igentuman.nc.entity.EntityBlockProjectile;
import igentuman.nc.entity.Q36EnergyFlash;
import igentuman.nc.entity.Q36PulseProjectile;
import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.entity.anomaly.BurningAnomalyEntity;
import igentuman.nc.entity.anomaly.ElectricAnomalyEntity;
import igentuman.nc.entity.anomaly.GravitationalAnomalyEntity;
import igentuman.nc.entity.anomaly.PsychoAnomalyEntity;
import igentuman.nc.entity.anomaly.RadioactiveAnomalyEntity;
import igentuman.nc.entity.anomaly.TeleportingAnomalyEntity;
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

    // spawner, hence .noSummon(). Large tracking range so renderer/FX engage before the effect radius.
    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<GravitationalAnomalyEntity>> GRAVITATIONAL_ANOMALY =
            Registries.ENTITIES.register("gravitational_anomaly",
                    () -> EntityType.Builder.<GravitationalAnomalyEntity>of(GravitationalAnomalyEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build("gravitational_anomaly"));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<ElectricAnomalyEntity>> ELECTRIC_ANOMALY =
            Registries.ENTITIES.register("electric_anomaly",
                    () -> EntityType.Builder.<ElectricAnomalyEntity>of(ElectricAnomalyEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build("electric_anomaly"));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<RadioactiveAnomalyEntity>> RADIOACTIVE_ANOMALY =
            Registries.ENTITIES.register("radioactive_anomaly",
                    () -> EntityType.Builder.<RadioactiveAnomalyEntity>of(RadioactiveAnomalyEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build("radioactive_anomaly"));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<BurningAnomalyEntity>> BURNING_ANOMALY =
            Registries.ENTITIES.register("burning_anomaly",
                    () -> EntityType.Builder.<BurningAnomalyEntity>of(BurningAnomalyEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .fireImmune()
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build("burning_anomaly"));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<PsychoAnomalyEntity>> PSYCHO_ANOMALY =
            Registries.ENTITIES.register("psycho_anomaly",
                    () -> EntityType.Builder.<PsychoAnomalyEntity>of(PsychoAnomalyEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build("psycho_anomaly"));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<TeleportingAnomalyEntity>> TELEPORTING_ANOMALY =
            Registries.ENTITIES.register("teleporting_anomaly",
                    () -> EntityType.Builder.<TeleportingAnomalyEntity>of(TeleportingAnomalyEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build("teleporting_anomaly"));

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
        event.put(GRAVITATIONAL_ANOMALY.get(), AnomalyEntity.createAttributes().build());
        event.put(ELECTRIC_ANOMALY.get(), AnomalyEntity.createAttributes().build());
        event.put(RADIOACTIVE_ANOMALY.get(), AnomalyEntity.createAttributes().build());
        event.put(BURNING_ANOMALY.get(), AnomalyEntity.createAttributes().build());
        event.put(PSYCHO_ANOMALY.get(), AnomalyEntity.createAttributes().build());
        event.put(TELEPORTING_ANOMALY.get(), AnomalyEntity.createAttributes().build());
    }
}
