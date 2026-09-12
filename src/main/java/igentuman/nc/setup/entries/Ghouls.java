package igentuman.nc.setup.entries;

import igentuman.nc.entity.EntityFeralGhoul;
import igentuman.nc.entity.EntityBlockProjectile;
import igentuman.nc.entity.EntityWastelandBoss;
import igentuman.nc.setup.ModEntries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.registration.ModEntryBuilder.addEntityType;
import static igentuman.nc.registration.ModEntryBuilder.addItem;

/** Registers the Feral Ghoul entity type and its spawn egg item. */
public class Ghouls extends ModEntries {

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFeralGhoul>> FERAL_GHOUL =
            addEntityType("feral_ghoul",
                    EntityType.Builder.<EntityFeralGhoul>of(EntityFeralGhoul::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityWastelandBoss>> FERAL_GHOUL_BOSS =
            addEntityType("feral_ghoul_boss",
                    EntityType.Builder.<EntityWastelandBoss>of(EntityWastelandBoss::new, MobCategory.MONSTER)
                            .sized(1.95F, 4.35F)
                            .fireImmune()
                            .clientTrackingRange(12));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityBlockProjectile>> WASTELAND_PROJECTILE =
            addEntityType("wasteland_projectile",
                    EntityType.Builder.<EntityBlockProjectile>of(EntityBlockProjectile::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(16)
                            .updateInterval(1));

    public static void ghouls() {
        addItem("feral_ghoul_spawn_egg",
                () -> new DeferredSpawnEggItem(FERAL_GHOUL, 0x7e9680, 0xc5d1c5, new Item.Properties()))
                .build();
        addItem("feral_ghoul_boss_spawn_egg",
                () -> new DeferredSpawnEggItem(FERAL_GHOUL_BOSS, 0x344c38, 0x9bcf67, new Item.Properties()))
                .build();
    }
}
