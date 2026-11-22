package igentuman.nc.setup.entries;

import igentuman.nc.api.catalyst.Catalyst;
import igentuman.nc.api.catalyst.CatalystDef;
import igentuman.nc.api.catalyst.CatalystFactory;
import igentuman.nc.api.catalyst.CatalystRegistry;
import igentuman.nc.api.catalyst.CatalystType;
import igentuman.nc.block_entity.catalyst.EnergyCatalyst;
import igentuman.nc.block_entity.catalyst.QuantumCatalyst;
import igentuman.nc.block_entity.catalyst.SpeedCatalyst;
import igentuman.nc.block_entity.catalyst.StackCatalyst;
import igentuman.nc.entity.Q36EnergyFlash;
import igentuman.nc.entity.Q36PulseProjectile;
import igentuman.nc.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import igentuman.nc.registration.ArmorMaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.NCJukeboxSongs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.util.Unit;
import net.minecraft.world.item.component.TooltipDisplay;
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
                "cocoa_solids",
                "flour",
                "ground_cocoa_nibs",
                "roasted_cocoa_beans",
                "salt",
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
                "empty_detector",
                "wire_chamber_casing",
                "scintillator_pwo",
                "scintillator_plastic",
                "laser_assembly",
                "wire_gold_tungsten",
        };
        for (String name : parts) {
            addItem(name).build();
        }

        var researchPaper = addItem("research_paper", ResearchPaperItem::new).build().item();
        // The Leacher handles ore-source items itself.
        CatalystFactory oreSource = host -> new Catalyst(CatalystType.ORE_SOURCE, host) {};
        CatalystRegistry.register(new CatalystDef(researchPaper, CatalystType.ORE_SOURCE, oreSource));
        CatalystRegistry.register(new CatalystDef(() -> Items.FILLED_MAP, CatalystType.ORE_SOURCE, oreSource));
        addItem("fission_reactor_plan", props -> new FissionReactorPlanItem(props.stacksTo(1))).build();

        String[] collectors = {
                "water_collector",
                "compact_water_collector",
                "dense_water_collector",
                "nitrogen_collector",
                "compact_nitrogen_collector",
                "dense_nitrogen_collector",
                "helium_collector",
                "compact_helium_collector",
                "dense_helium_collector",
                "lava_collector",
        };
        for (String name : collectors) {
            addItem(name).build();
        }
    }

    public static final ModEntry ENERGY_UPGRADE =
            addCatalyst("energy_upgrade", CatalystType.ENERGY, EnergyCatalyst::new,
                    props -> new ProcessorUpgradeItem(ProcessorUpgradeItem.Type.ENERGY, props));

    public static final ModEntry SPEED_UPGRADE =
            addCatalyst("speed_upgrade", CatalystType.SPEED, SpeedCatalyst::new,
                    props -> new ProcessorUpgradeItem(ProcessorUpgradeItem.Type.SPEED, props));

    public static final ModEntry STACK_UPGRADE =
            addCatalyst("stack_upgrade", CatalystType.SPEED, StackCatalyst::new,
                    props -> new ProcessorUpgradeItem(ProcessorUpgradeItem.Type.STACK, props));

    public static final ModEntry QUANTUM_UPGRADE =
            addCatalyst("quantum_upgrade", CatalystType.SPEED, QuantumCatalyst::new,
                    props -> new ProcessorUpgradeItem(ProcessorUpgradeItem.Type.QUANTUM, props));

    public static void records() {
        for (String name : NCJukeboxSongs.RECORDS.keySet()) {
            addItem(name, props -> new Item(props.stacksTo(1).jukeboxPlayable(NCJukeboxSongs.key(name)))).build();
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
                props -> new Q36Item(props.stacksTo(1).rarity(Rarity.EPIC)))
                .build();
    }

    public static void tools() {
        add("spaxelhoe_tough")
                .item(props -> new PaxelItem(NCTiers.TOUGH,
                        props.stacksTo(1).fireResistant()))
                .build();
        add("spaxelhoe_thorium")
                .item(props -> new PaxelItem(NCTiers.THORIUM,
                        props.stacksTo(1).fireResistant()))
                .build();
        add("multitool")
                .item(props -> new MultitoolItem(props.stacksTo(1)))
                .build();
        add("qnp")
                .item(props -> new QNPItem(NCTiers.QNP,
                        props.stacksTo(1)))
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
                .repairItem(NCTiers.TOUGH_REPAIRS)
                .build();
        addArmorSet("tough", tough).build();

        ArmorMaterialEntry hev = ArmorMaterialEntry.builder("hev")
                .durabilityMultiplier(37)
                .defense(3, 5, 7, 3)
                .enchantmentValue(25)
                .equipSound(SoundEvents.ARMOR_EQUIP_NETHERITE)
                .toughness(4.0f)
                .knockbackResistance(0.3f)
                .repairItem(NCTiers.HEV_REPAIRS)
                .build();
        addArmorSet("hev", hev)
                .armorFactory((material, type, props) ->
                        new HEVItem(material, type, props.component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                                .component(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.UNBREAKABLE, true))))
                .build();
    }
}
