package igentuman.nc.setup.entries;

import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.entity.anomaly.BurningAnomalyEntity;
import igentuman.nc.entity.anomaly.ElectricAnomalyEntity;
import igentuman.nc.entity.anomaly.GravitationalAnomalyEntity;
import igentuman.nc.entity.anomaly.PsychoAnomalyEntity;
import igentuman.nc.entity.anomaly.RadioactiveAnomalyEntity;
import igentuman.nc.entity.anomaly.TeleportingAnomalyEntity;
import igentuman.nc.item.ResoniteCrystalItem;
import igentuman.nc.setup.ModEntries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.registration.ModEntryBuilder.addEntityType;
import static igentuman.nc.registration.ModEntryBuilder.addItem;

public class Anomalies extends ModEntries {

    public static final DeferredHolder<EntityType<?>, EntityType<GravitationalAnomalyEntity>> GRAVITATIONAL_ANOMALY =
            addEntityType("gravitational_anomaly",
                    EntityType.Builder.<GravitationalAnomalyEntity>of(GravitationalAnomalyEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .fireImmune()
                            .noSummon()
                            .clientTrackingRange(16)
                            .updateInterval(3));

    public static final DeferredHolder<EntityType<?>, EntityType<ElectricAnomalyEntity>> ELECTRIC_ANOMALY =
            addEntityType("electric_anomaly",
                    EntityType.Builder.<ElectricAnomalyEntity>of(ElectricAnomalyEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .fireImmune()
                            .noSummon()
                            .clientTrackingRange(16)
                            .updateInterval(3));

    public static final DeferredHolder<EntityType<?>, EntityType<RadioactiveAnomalyEntity>> RADIOACTIVE_ANOMALY =
            addEntityType("radioactive_anomaly",
                    EntityType.Builder.<RadioactiveAnomalyEntity>of(RadioactiveAnomalyEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .fireImmune()
                            .noSummon()
                            .clientTrackingRange(16)
                            .updateInterval(3));

    public static final DeferredHolder<EntityType<?>, EntityType<BurningAnomalyEntity>> BURNING_ANOMALY =
            addEntityType("burning_anomaly",
                    EntityType.Builder.<BurningAnomalyEntity>of(BurningAnomalyEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .fireImmune()
                            .noSummon()
                            .clientTrackingRange(16)
                            .updateInterval(3));

    public static final DeferredHolder<EntityType<?>, EntityType<PsychoAnomalyEntity>> PSYCHO_ANOMALY =
            addEntityType("psycho_anomaly",
                    EntityType.Builder.<PsychoAnomalyEntity>of(PsychoAnomalyEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .fireImmune()
                            .noSummon()
                            .clientTrackingRange(16)
                            .updateInterval(3));

    public static final DeferredHolder<EntityType<?>, EntityType<TeleportingAnomalyEntity>> TELEPORTING_ANOMALY =
            addEntityType("teleporting_anomaly",
                    EntityType.Builder.<TeleportingAnomalyEntity>of(TeleportingAnomalyEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .fireImmune()
                            .noSummon()
                            .clientTrackingRange(16)
                            .updateInterval(3));

    public static void anomalies() {
        addItem("resonite_shard", () -> new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(64))).build();
        addItem("resonite_crystal", () -> new ResoniteCrystalItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))).build();
    }
}
