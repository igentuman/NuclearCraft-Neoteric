package igentuman.nc.setup.entries;

import igentuman.nc.entity.Q36EnergyFlash;
import igentuman.nc.entity.Q36PulseProjectile;
import igentuman.nc.item.Q36Item;
import igentuman.nc.setup.ModEntries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.registration.ModEntryBuilder.addEntityType;
import static igentuman.nc.registration.ModEntryBuilder.addItem;

public class Q36 extends ModEntries {

    public static final DeferredHolder<EntityType<?>, EntityType<Q36PulseProjectile>> Q36_PULSE_PROJECTILE =
            addEntityType("q36_pulse_projectile",
                    EntityType.Builder.<Q36PulseProjectile>of(Q36PulseProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(1));

    public static final DeferredHolder<EntityType<?>, EntityType<Q36EnergyFlash>> Q36_ENERGY_FLASH =
            addEntityType("q36_energy_flash",
                    EntityType.Builder.<Q36EnergyFlash>of(Q36EnergyFlash::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .noSummon()
                            .clientTrackingRange(8)
                            .updateInterval(20));

    public static void q36() {
        addItem("q36_quantite_disruptor",
                () -> new Q36Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)))
                .build();
    }
}
