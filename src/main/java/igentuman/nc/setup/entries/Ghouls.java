package igentuman.nc.setup.entries;

import igentuman.nc.entity.EntityFeralGhoul;
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

    public static void ghouls() {
        addItem("feral_ghoul_spawn_egg",
                () -> new DeferredSpawnEggItem(FERAL_GHOUL, 0x7e9680, 0xc5d1c5, new Item.Properties()))
                .build();
    }
}
