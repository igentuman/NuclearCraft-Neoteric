package igentuman.nc.setup.entries;

import igentuman.nc.item.HEVItem;
import igentuman.nc.item.MultitoolItem;
import igentuman.nc.item.NCTiers;
import igentuman.nc.item.PaxelItem;
import igentuman.nc.item.QNPItem;
import igentuman.nc.item.ResearchPaperItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.PickaxeItem;
import igentuman.nc.registration.ArmorMaterialEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.NCJukeboxSongs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.crafting.Ingredient;

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
    }

    public static void records() {
        for (String name : NCJukeboxSongs.RECORDS.keySet()) {
            addItem(name, () -> new Item(new Item.Properties().stacksTo(1).jukeboxPlayable(NCJukeboxSongs.key(name)))).build();
        }
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
