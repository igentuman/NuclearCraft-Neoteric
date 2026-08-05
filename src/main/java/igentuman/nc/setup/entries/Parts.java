package igentuman.nc.setup.entries;

import igentuman.nc.block_entity.catalyst.CatalystType;
import igentuman.nc.block_entity.catalyst.EnergyCatalyst;
import igentuman.nc.block_entity.catalyst.SpeedCatalyst;
import igentuman.nc.entity.Q36EnergyFlash;
import igentuman.nc.entity.Q36PulseProjectile;
import igentuman.nc.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.PickaxeItem;
import igentuman.nc.registration.ArmorMaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.NCJukeboxSongs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.registration.ModEntryBuilder.*;

/** Declares crafting component items plus the mod's tool and armor sets. */
public class Parts extends ModEntries {
    public static void parts() {
        String[] parts = {
                "actuator",
                "basic_electric_circuit",
                "bioplastic",
                "chassis",
                "empty_frame",
                "empty_sink",
                "motor",
                "plate_advanced",
                "plate_basic",
                "plate_du",
                "plate_elite",
                "plate_extreme",
                "servo",
                "sic_fiber",
                "steel_frame",
                "coil_copper",
                "coil_magnesium_diboride",
                "coil_bscco",
                "advanced_processor",
                "basic_processor",
                "elite_processor",
                "silicon_boule",
                "silicon_n_doped",
                "silicon_p_doped",
                "silicon_wafer",
                "lithium_ion_cell",
                "neutron_initiator",
                "compression_charge",
                "pu_239_pit",
                "pu_239_core",
        };
        for (String name : parts) {
            addItem(name).build();
        }

        addItem("research_paper", () -> new ResearchPaperItem(new Item.Properties())).build();
        addItem("fission_reactor_plan", () -> new FissionReactorPlanItem(new Item.Properties().stacksTo(1))).build();
    }

    public static final ModEntry ENERGY_UPGRADE =
            addCatalyst("energy_upgrade", CatalystType.ENERGY, EnergyCatalyst::new);

    public static final ModEntry SPEED_UPGRADE =
            addCatalyst("speed_upgrade", CatalystType.SPEED, SpeedCatalyst::new);

    public static void records() {
        for (String name : NCJukeboxSongs.RECORDS.keySet()) {
            addItem(name, () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(NCJukeboxSongs.key(name)))).build();
        }
    }

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

    public static void tools() {
        add("spaxelhoe_tough")
                .item(() -> new PaxelItem(NCTiers.TOUGH,
                        new Item.Properties().stacksTo(1).durability(9000).fireResistant()))
                .build();
        add("spaxelhoe_thorium")
                .item(() -> new PaxelItem(NCTiers.THORIUM,
                        new Item.Properties().stacksTo(1).durability(5000).fireResistant()))
                .build();
        add("multitool")
                .item(() -> new MultitoolItem(new Item.Properties().stacksTo(1)))
                .build();
        add("qnp")
                .item(() -> new QNPItem(NCTiers.QNP,
                        new Item.Properties().stacksTo(1)
                                .attributes(PickaxeItem.createAttributes(NCTiers.QNP, 11, 2F))))
                .build();
    }

    public static void armor() {
        ArmorMaterialEntry tough = ArmorMaterialEntry.builder("tough")
                .durabilityMultiplier(33)
                .defense(3, 6, 8, 3)
                .enchantmentValue(15)
                .equipSound(SoundEvents.ARMOR_EQUIP_DIAMOND)
                .toughness(3.5f)
                .knockbackResistance(0.2f)
                .repairItem(() -> Ingredient.of(get("tough_alloy").materialEntry().ingot().get()))
                .build();
        addArmorSet("tough", tough).build();

        ArmorMaterialEntry hev = ArmorMaterialEntry.builder("hev")
                .durabilityMultiplier(37)
                .defense(3, 5, 7, 3)
                .enchantmentValue(25)
                .equipSound(SoundEvents.ARMOR_EQUIP_NETHERITE)
                .toughness(4.0f)
                .knockbackResistance(0.3f)
                .repairItem(() -> Ingredient.of(get("plate_extreme").item().get()))
                .build();
        addArmorSet("hev", hev)
                .armorFactory((material, type, props) ->
                        new HEVItem(material, type, props.component(DataComponents.UNBREAKABLE, new Unbreakable(false))))
                .build();
    }
}
