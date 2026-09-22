package igentuman.nc.setup.entries;

import igentuman.nc.block.accelerator.AcceleratorBeamPortBlock;
import igentuman.nc.block.accelerator.AcceleratorBlock;
import igentuman.nc.block.accelerator.AcceleratorControllerBlock;
import igentuman.nc.block.accelerator.AcceleratorIonSourcePortBlock;
import igentuman.nc.block.accelerator.AcceleratorPortBlock;
import igentuman.nc.block.accelerator.CoolerBlock;
import igentuman.nc.block.fusion.ElectromagnetBlock;
import igentuman.nc.block.fusion.ElectromagnetSlopeBlock;
import igentuman.nc.block.fusion.RFAmplifierBlock;
import igentuman.nc.block_entity.accelerator.AcceleratorBeamPortBE;
import igentuman.nc.block_entity.accelerator.AcceleratorIonSourcePortBE;
import igentuman.nc.block_entity.accelerator.AcceleratorPortBE;
import igentuman.nc.block_entity.accelerator.BeamDiverterControllerBE;
import igentuman.nc.block_entity.accelerator.LinearAcceleratorControllerBE;
import igentuman.nc.block_entity.accelerator.RingAcceleratorControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.multiblock.accelerator.CoolerDef;
import igentuman.nc.container.AcceleratorIonSourcePortContainer;
import igentuman.nc.container.AcceleratorPortContainer;
import igentuman.nc.container.BeamDiverterContainer;
import igentuman.nc.container.LinearAcceleratorContainer;
import igentuman.nc.container.RingAcceleratorContainer;
import igentuman.nc.multiblock.MultiblockEntryBuilder;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.accelerator.BeamDiverterValidationContext;
import igentuman.nc.multiblock.accelerator.BeamDiverterValidationJob;
import igentuman.nc.multiblock.accelerator.LinearAcceleratorValidationContext;
import igentuman.nc.multiblock.accelerator.LinearAcceleratorValidationJob;
import igentuman.nc.multiblock.accelerator.RingAcceleratorValidationContext;
import igentuman.nc.multiblock.accelerator.RingAcceleratorValidationJob;
import igentuman.nc.multiblock.discovery.BoundaryDiscoverySpec;
import igentuman.nc.multiblock.discovery.BoundaryGeometryDiscoveryJob;
import igentuman.nc.multiblock.fusion.ElectromagnetDef;
import igentuman.nc.multiblock.fusion.RFAmplifierDef;
import igentuman.nc.item.ParticleSourceItem;
import igentuman.nc.particle.ParticleSourceCatalog;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.Registers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.registration.ModEntryBuilder.addMultiblockBlock;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockController;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockPart;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockPartWithoutMenu;
import static igentuman.nc.registration.ModEntryBuilder.addItem;
import static igentuman.nc.setup.Registers.CREATIVE_MODE_TABS;

/** Declares content for the accelerator multiblock. */
public class Accelerator extends ModEntries {
    private static boolean initialized;
    public static final List<String> STRUCTURAL_BLOCKS = List.of(
            "accelerator_casing", "accelerator_casing_glass", "electromagnet_yoke", "particle_beam"
    );
    public static final List<String> CONTROLLERS = List.of(
            "linear_accelerator_controller", "ring_accelerator_controller", "beam_diverter_controller"
    );
    public static final List<String> PORTS = List.of(
            "accelerator_port", "accelerator_beam_port", "accelerator_ion_source_port"
    );
    public static final List<String> COOLERS = List.of(
            "aluminum_cooler", "arsenic_cooler", "boron_cooler", "carobbiite_cooler", "copper_cooler",
            "cryotheum_cooler", "diamond_cooler", "emerald_cooler", "empty_cooler", "end_stone_cooler",
            "enderium_cooler", "fluorite_cooler", "glowstone_cooler", "gold_cooler", "iron_cooler",
            "lapis_cooler", "lead_cooler", "liquid_helium_cooler", "liquid_nitrogen_cooler", "lithium_cooler",
            "magnesium_cooler", "manganese_cooler", "nether_brick_cooler", "obsidian_cooler",
            "prismarine_cooler", "purpur_cooler", "quartz_cooler", "redstone_cooler", "silver_cooler",
            "slime_cooler", "tin_cooler", "villiaumite_cooler", "water_cooler"
    );
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ACCELERATOR =
            CREATIVE_MODE_TABS.register("accelerator", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nuclearcraft.accelerator"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModEntries.get("linear_accelerator_controller").block().toStack())
                    .displayItems((parameters, output) -> acceleratorTabEntries().forEach(name -> {
                        ModEntry entry = ModEntries.get(name);
                        if (entry != null && entry.hasItem() && entry.isEnabled()) output.accept(entry.item());
                    })).build());

    public static void accelerator() {
        if (initialized) return;
        initialized = true;

        HighEnergyComponents.highEnergyComponents();
        igentuman.nc.recipe.particle.ParticleRecipes.init();
        registerParticleSources();
        addMultiblockBlock("accelerator_casing", () -> new AcceleratorBlock(metalProps()));
        addMultiblockBlock("accelerator_casing_glass", () -> new AcceleratorBlock(glassProps()));
        addMultiblockBlock("electromagnet_yoke", () -> new AcceleratorBlock(metalProps()));
        addMultiblockBlock("particle_beam", () -> new AcceleratorBlock(glassProps()));
        for (String cooler : COOLERS) {
            addMultiblockBlock(cooler, () -> new CoolerBlock(metalProps(), CoolerDef.get(cooler)));
        }

        ModEntry controller = addMultiblockController("linear_accelerator_controller", AcceleratorControllerBlock::new)
                .blockEntity(LinearAcceleratorControllerBE::new)
                .menu(LinearAcceleratorContainer::new)
                .itemCap(1, 0)
                .fluidCap(2, 1, 0)
                .withEnergyInput(100_000_000)
                .withoutRecipes()
                .build();
        ModEntry servicePort = addMultiblockPart("accelerator_port", metalProps(), AcceleratorPortBlock::new,
                AcceleratorPortBE::new, AcceleratorPortContainer::new);
        ModEntry beamPort = addMultiblockPartWithoutMenu("accelerator_beam_port", metalProps(),
                AcceleratorBeamPortBlock::new, AcceleratorBeamPortBE::new);
        ModEntry ionSourcePort = addMultiblockPart("accelerator_ion_source_port", metalProps(),
                AcceleratorIonSourcePortBlock::new, AcceleratorIonSourcePortBE::new,
                AcceleratorIonSourcePortContainer::new);

        MultiblockEntryBuilder.name("linear_accelerator")
                .controller(controller)
                .ports(servicePort, beamPort, ionSourcePort)
                .casing(() -> get("accelerator_casing").block().get(),
                        () -> get("accelerator_casing_glass").block().get())
                .interior(() -> get("particle_beam").block().get(),
                        () -> get("electromagnet_yoke").block().get())
                .sizeRange(5, 10_000, 5, 5, 5, 10_000)
                .scheduled((id, pos, facing) -> {
                            int[] range = linearSizeRange();
                            return new BoundaryGeometryDiscoveryJob(id, pos, facing,
                                    new BoundaryDiscoverySpec(BoundaryDiscoverySpec.Shape.LINEAR_TUBE,
                                            5, range[1], 5, 5, 5, range[1], 0, range[0],
                                            Accelerator::isLinearShell));
                        },
                        (id, record) -> new LinearAcceleratorValidationJob(id, validationContext(record)))
                .build();

        ModEntry ringController = addMultiblockController("ring_accelerator_controller", AcceleratorControllerBlock::new)
                .blockEntity(RingAcceleratorControllerBE::new)
                .menu(RingAcceleratorContainer::new)
                .fluidCap(1, 1, 0)
                .withEnergyInput(100_000_000)
                .withoutRecipes()
                .build();

        MultiblockEntryBuilder.name("ring_accelerator")
                .controller(ringController)
                .ports(servicePort, beamPort)
                .casing(() -> get("accelerator_casing").block().get(),
                        () -> get("accelerator_casing_glass").block().get())
                .interior(() -> get("particle_beam").block().get(),
                        () -> get("electromagnet_yoke").block().get())
                .sizeRange(RingAcceleratorValidationJob.MINIMUM_OUTER_SIDE, 10_000, 5, 5,
                        RingAcceleratorValidationJob.MINIMUM_OUTER_SIDE, 10_000)
                .scheduled((id, pos, facing) -> {
                            int[] range = ringSizeRange();
                            return new BoundaryGeometryDiscoveryJob(id, pos, facing,
                                    new BoundaryDiscoverySpec(BoundaryDiscoverySpec.Shape.SQUARE_RING,
                                            range[0], range[1], 5, 5, range[0], range[1],
                                            RingAcceleratorValidationJob.WALL_OFFSET,
                                            Accelerator::isRingOrDiverterShell));
                        },
                        (id, record) -> new RingAcceleratorValidationJob(id, ringValidationContext(record)))
                .build();

        ModEntry diverterController = addMultiblockController("beam_diverter_controller", AcceleratorControllerBlock::new)
                .blockEntity(BeamDiverterControllerBE::new)
                .menu(BeamDiverterContainer::new)
                .withEnergyInput(100_000_000)
                .withoutRecipes()
                .build();

        MultiblockEntryBuilder.name("beam_diverter")
                .controller(diverterController)
                .ports(servicePort, beamPort)
                .casing(() -> get("accelerator_casing").block().get(),
                        () -> get("accelerator_casing_glass").block().get())
                .interior(() -> get("particle_beam").block().get(),
                        () -> get("electromagnet_yoke").block().get())
                .size(5, 5, 5)
                .scheduled((id, pos, facing) -> new BoundaryGeometryDiscoveryJob(id, pos, facing,
                                new BoundaryDiscoverySpec(BoundaryDiscoverySpec.Shape.BOX,
                                        5, 5, 5, 5, 5, 5, 0, Accelerator::isRingOrDiverterShell)),
                        (id, record) -> new BeamDiverterValidationJob(id, diverterValidationContext(record)))
                .build();
    }

    private static int[] ringSizeRange() {
        int[] range = linearSizeRange();
        return new int[]{Math.max(range[0], RingAcceleratorValidationJob.MINIMUM_OUTER_SIDE), range[1]};
    }

    private static RingAcceleratorValidationContext ringValidationContext(StructureRecord record) {
        return new RingAcceleratorValidationContext(record,
                state -> is(state, "accelerator_casing"),
                Accelerator::isRingOrDiverterShell,
                state -> is(state, "ring_accelerator_controller"),
                state -> is(state, "accelerator_port"),
                state -> is(state, "accelerator_beam_port"),
                state -> is(state, "particle_beam"),
                state -> is(state, "electromagnet_yoke"),
                Accelerator::electromagnet,
                Accelerator::rfAmplifier,
                state -> state.getBlock() instanceof CoolerBlock cooler ? cooler.definition() : null,
                Multiblocks.acceleratorHeatCapacityPerBlock);
    }

    private static BeamDiverterValidationContext diverterValidationContext(StructureRecord record) {
        return new BeamDiverterValidationContext(record,
                state -> is(state, "accelerator_casing"),
                Accelerator::isRingOrDiverterShell,
                state -> is(state, "beam_diverter_controller"),
                state -> is(state, "accelerator_port"),
                state -> is(state, "accelerator_beam_port"),
                state -> is(state, "particle_beam"),
                state -> is(state, "electromagnet_yoke"),
                Accelerator::electromagnet,
                Multiblocks.acceleratorHeatCapacityPerBlock);
    }

    private static boolean isRingOrDiverterShell(BlockState state) {
        return is(state, "accelerator_casing") || is(state, "accelerator_casing_glass")
                || is(state, "ring_accelerator_controller") || is(state, "beam_diverter_controller")
                || is(state, "accelerator_port") || is(state, "accelerator_beam_port");
    }

    private static void registerParticleSources() {
        ParticleSourceCatalog catalog = ParticleSourceCatalog.builtin();
        for (String name : Particles.ITEM_SOURCES) {
            ResourceLocation id = igentuman.nc.NuclearCraft.rl(name);
            addItem(name, () -> new ParticleSourceItem(new Item.Properties().stacksTo(1)
                    .component(Registers.PARTICLE_SOURCE.get(), catalog.itemSources().get(id))))
                    .withoutRecipes()
                    .build();
        }
    }

    private static List<String> acceleratorTabEntries() {
        List<String> entries = new ArrayList<>();
        entries.add("linear_accelerator_controller");
        entries.add("ring_accelerator_controller");
        entries.add("beam_diverter_controller");
        entries.addAll(STRUCTURAL_BLOCKS);
        entries.addAll(PORTS);
        entries.addAll(COOLERS);
        for (String tier : HighEnergyComponents.TIERS) {
            entries.add(tier + "_electromagnet");
            entries.add(tier + "_rf_amplifier");
        }
        entries.addAll(Particles.ITEM_SOURCES);
        return entries;
    }

    private static LinearAcceleratorValidationContext validationContext(StructureRecord record) {
        return new LinearAcceleratorValidationContext(record,
                state -> is(state, "accelerator_casing"),
                Accelerator::isLinearShell,
                state -> is(state, "linear_accelerator_controller"),
                state -> is(state, "accelerator_port"),
                state -> is(state, "accelerator_beam_port"),
                state -> is(state, "accelerator_ion_source_port"),
                state -> is(state, "particle_beam"),
                state -> is(state, "electromagnet_yoke"),
                Accelerator::electromagnet,
                Accelerator::rfAmplifier,
                state -> state.getBlock() instanceof CoolerBlock cooler ? cooler.definition() : null,
                Multiblocks.acceleratorHeatCapacityPerBlock);
    }

    private static boolean isLinearShell(BlockState state) {
        return is(state, "accelerator_casing") || is(state, "accelerator_casing_glass")
                || is(state, "linear_accelerator_controller") || is(state, "accelerator_port")
                || is(state, "accelerator_beam_port") || is(state, "accelerator_ion_source_port");
    }

    private static boolean is(BlockState state, String name) {
        ModEntry entry = get(name);
        return entry != null && entry.block() != null && state.is(entry.block().get());
    }

    private static ElectromagnetDef electromagnet(BlockState state) {
        if (!(state.getBlock() instanceof ElectromagnetBlock)
                || state.getBlock() instanceof ElectromagnetSlopeBlock) return null;
        return ElectromagnetDef.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
    }

    private static RFAmplifierDef rfAmplifier(BlockState state) {
        if (!(state.getBlock() instanceof RFAmplifierBlock)) return null;
        return RFAmplifierDef.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
    }

    private static int[] linearSizeRange() {
        return switch (Multiblocks.acceleratorScalePreset) {
            case 2 -> new int[]{60, 1_000};
            case 3 -> new int[]{600, 10_000};
            default -> new int[]{6, 100};
        };
    }

    private static BlockBehaviour.Properties metalProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f, 6.0f)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties glassProps() {
        return metalProps().sound(SoundType.GLASS).noOcclusion();
    }
}
