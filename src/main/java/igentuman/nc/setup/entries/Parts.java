package igentuman.nc.setup.entries;

import igentuman.nc.item.MultitoolItem;
import igentuman.nc.item.NCTiers;
import igentuman.nc.item.PaxelItem;
import igentuman.nc.registration.ArmorMaterialEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import static igentuman.nc.registration.ModEntryBuilder.*;

public class Parts extends ModEntries {
    public static void parts() {
        String[] parts = {
                "actuator",
                "research_paper",
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
                "neutron_initiator",
                "compression_charge",
                "pu_239_pit",
                "pu_239_core",
        };
        for (String name : parts) {
            addItem(name).build();
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
    }
}
