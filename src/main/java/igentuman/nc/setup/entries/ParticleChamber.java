package igentuman.nc.setup.entries;

import igentuman.nc.block.particle.DetectorBlock;
import igentuman.nc.block.particle.ParticleChamberBeamPortBlock;
import igentuman.nc.block.particle.ParticleChamberBlock;
import igentuman.nc.block.particle.ParticleChamberCameraBlock;
import igentuman.nc.block.particle.ParticleChamberControllerBlock;
import igentuman.nc.block.particle.ParticleChamberPortBlock;
import igentuman.nc.block_entity.particle.CollisionChamberControllerBE;
import igentuman.nc.block_entity.particle.DecayChamberControllerBE;
import igentuman.nc.block_entity.particle.ParticleChamberBeamPortBE;
import igentuman.nc.block_entity.particle.ParticleChamberPortBE;
import igentuman.nc.block_entity.particle.TargetChamberControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.container.CollisionChamberContainer;
import igentuman.nc.container.DecayChamberContainer;
import igentuman.nc.container.ParticleChamberPortContainer;
import igentuman.nc.container.TargetChamberContainer;
import igentuman.nc.multiblock.MultiblockEntryBuilder;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.discovery.BoundaryDiscoverySpec;
import igentuman.nc.multiblock.discovery.BoundaryGeometryDiscoveryJob;
import igentuman.nc.multiblock.particle_chamber.CollisionChamberValidationContext;
import igentuman.nc.multiblock.particle_chamber.CollisionChamberValidationJob;
import igentuman.nc.multiblock.particle_chamber.DecayChamberValidationContext;
import igentuman.nc.multiblock.particle_chamber.DecayChamberValidationJob;
import igentuman.nc.multiblock.particle_chamber.DetectorDef;
import igentuman.nc.multiblock.particle_chamber.TargetChamberValidationContext;
import igentuman.nc.multiblock.particle_chamber.TargetChamberValidationJob;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.SlotsLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.registration.ModEntryBuilder.add;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockBlock;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockController;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockPart;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockPartWithoutMenu;
import static igentuman.nc.setup.Registers.CREATIVE_MODE_TABS;

/** Declares content for the particle chamber multiblock. */
public class ParticleChamber extends ModEntries {
    private static boolean initialized;
    private static final DetectorDef SILICON_TRACKER = new DetectorDef(rl("silicon_tracker"), 0.15, 20_000, 1);
    private static final DetectorDef BUBBLE_CHAMBER = new DetectorDef(rl("bubble_chamber"), 0.075, 2_000, 2);
    private static final DetectorDef WIRE_CHAMBER = new DetectorDef(rl("wire_chamber"), 0.1, 10_000, 2);
    private static final DetectorDef EM_CALORIMETER = new DetectorDef(rl("em_calorimeter"), 0.05, 2_000, 3);
    private static final DetectorDef HADRON_CALORIMETER = new DetectorDef(rl("hadron_calorimeter"), 0.025, 1_000, 5);
    public static final List<String> STRUCTURAL_BLOCKS = List.of(
            "target_chamber_casing", "target_chamber_casing_glass", "target_chamber_camera"
    );
    public static final List<String> CONTROLLERS = List.of(
            "target_chamber_controller", "decay_chamber_controller", "collision_chamber_controller"
    );
    public static final List<String> PORTS = List.of("target_chamber_port", "target_chamber_beam_port");
    private static final SlotsLayout TARGET_CHAMBER_LAYOUT = SlotsLayout.create()
            .addInput(53, 38)
            .addInput(51, 55)
            .addOutput(111, 38)
            .addOutput(111, 55);
    public static final List<String> DETECTORS = List.of(
            "bubble_chamber", "silicon_tracker", "wire_chamber", "em_calorimeter", "hadron_calorimeter"
    );
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PARTICLE_CHAMBER =
            CREATIVE_MODE_TABS.register("particle_chamber", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nuclearcraft.particle_chamber"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModEntries.get("target_chamber_controller").block().toStack())
                    .displayItems((parameters, output) -> particleChamberTabEntries().forEach(name -> {
                        ModEntry entry = ModEntries.get(name);
                        if (entry != null && entry.hasItem() && entry.isEnabled()) output.accept(entry.item());
                    })).build());

    private static List<String> particleChamberTabEntries() {
        List<String> entries = new ArrayList<>();
        entries.addAll(CONTROLLERS);
        entries.addAll(STRUCTURAL_BLOCKS);
        entries.addAll(PORTS);
        entries.addAll(DETECTORS);
        return entries;
    }

    public static void particleChamber() {
        if (initialized) return;
        initialized = true;

        igentuman.nc.recipe.particle.ParticleRecipes.init();
        addMultiblockBlock("target_chamber_casing", () -> new ParticleChamberBlock(metalProps()));
        addMultiblockBlock("target_chamber_casing_glass", () -> new ParticleChamberBlock(glassProps()));
        addMultiblockBlock("target_chamber_camera", () -> new ParticleChamberCameraBlock(metalProps()));
        add("silicon_tracker").block(() -> new DetectorBlock(metalProps(), SILICON_TRACKER)).build();
        add("bubble_chamber").block(() -> new DetectorBlock(metalProps(), BUBBLE_CHAMBER)).build();
        add("wire_chamber").block(() -> new DetectorBlock(metalProps(), WIRE_CHAMBER)).build();
        add("em_calorimeter").block(() -> new DetectorBlock(metalProps(), EM_CALORIMETER)).build();
        add("hadron_calorimeter").block(() -> new DetectorBlock(metalProps(), HADRON_CALORIMETER)).build();

        ModEntry controller = addMultiblockController("target_chamber_controller", ParticleChamberControllerBlock::new)
                .blockEntity(TargetChamberControllerBE::new)
                .menu(TargetChamberContainer::new)
                .itemCap(1, 1)
                .fluidCap(1, 1, 0)
                .withLayout(TARGET_CHAMBER_LAYOUT)
                .withEnergyInput(1_000_000)
                .withoutRecipes()
                .build();
        ModEntry servicePort = addMultiblockPart("target_chamber_port", metalProps(),
                ParticleChamberPortBlock::new, ParticleChamberPortBE::new, ParticleChamberPortContainer::new);
        ModEntry beamPort = addMultiblockPartWithoutMenu("target_chamber_beam_port", metalProps(),
                ParticleChamberBeamPortBlock::new, ParticleChamberBeamPortBE::new);

        MultiblockEntryBuilder.name("target_chamber")
                .controller(controller)
                .ports(servicePort, beamPort)
                .casing(() -> get("target_chamber_casing").block().get(),
                        () -> get("target_chamber_casing_glass").block().get())
                .interior(() -> get("target_chamber_camera").block().get(),
                        () -> get("particle_beam").block().get(),
                        () -> get("silicon_tracker").block().get(),
                        () -> get("bubble_chamber").block().get(),
                        () -> get("wire_chamber").block().get(),
                        () -> get("em_calorimeter").block().get(),
                        () -> get("hadron_calorimeter").block().get())
                .sizeRange(Multiblocks.targetChamberMinSize, Multiblocks.targetChamberMaxSize,
                        Multiblocks.targetChamberMinSize, Multiblocks.targetChamberMaxSize,
                        Multiblocks.targetChamberMinSize, Multiblocks.targetChamberMaxSize)
                .scheduled((id, pos, facing) -> {
                            int minimum = Math.min(Multiblocks.targetChamberMinSize,
                                    Multiblocks.targetChamberMaxSize);
                            int maximum = Math.max(Multiblocks.targetChamberMinSize,
                                    Multiblocks.targetChamberMaxSize);
                            return new BoundaryGeometryDiscoveryJob(id, pos, facing,
                                    new BoundaryDiscoverySpec(BoundaryDiscoverySpec.Shape.ODD_CUBE,
                                            minimum, maximum, minimum, maximum, minimum, maximum,
                                            0, ParticleChamber::isTargetShell));
                        },
                        (id, record) -> new TargetChamberValidationJob(id, validationContext(record)))
                .build();

        ModEntry decayController = addMultiblockController("decay_chamber_controller", ParticleChamberControllerBlock::new)
                .blockEntity(DecayChamberControllerBE::new)
                .menu(DecayChamberContainer::new)
                .withEnergyInput(1_000_000)
                .withoutRecipes()
                .build();

        MultiblockEntryBuilder.name("decay_chamber")
                .controller(decayController)
                .ports(servicePort, beamPort)
                .casing(() -> get("target_chamber_casing").block().get(),
                        () -> get("target_chamber_casing_glass").block().get())
                .interior(() -> get("target_chamber_camera").block().get(),
                        () -> get("particle_beam").block().get(),
                        () -> get("silicon_tracker").block().get(),
                        () -> get("bubble_chamber").block().get(),
                        () -> get("wire_chamber").block().get(),
                        () -> get("em_calorimeter").block().get(),
                        () -> get("hadron_calorimeter").block().get())
                .sizeRange(Multiblocks.decayChamberMinSize, Multiblocks.decayChamberMaxSize,
                        Multiblocks.decayChamberMinSize, Multiblocks.decayChamberMaxSize,
                        Multiblocks.decayChamberMinSize, Multiblocks.decayChamberMaxSize)
                .scheduled((id, pos, facing) -> {
                            int minimum = Math.min(Multiblocks.decayChamberMinSize,
                                    Multiblocks.decayChamberMaxSize);
                            int maximum = Math.max(Multiblocks.decayChamberMinSize,
                                    Multiblocks.decayChamberMaxSize);
                            return new BoundaryGeometryDiscoveryJob(id, pos, facing,
                                    new BoundaryDiscoverySpec(BoundaryDiscoverySpec.Shape.ODD_CUBE,
                                            minimum, maximum, minimum, maximum, minimum, maximum,
                                            0, ParticleChamber::isDecayShell));
                        },
                        (id, record) -> new DecayChamberValidationJob(id, decayValidationContext(record)))
                .build();

        ModEntry collisionController = addMultiblockController("collision_chamber_controller", ParticleChamberControllerBlock::new)
                .blockEntity(CollisionChamberControllerBE::new)
                .menu(CollisionChamberContainer::new)
                .withEnergyInput(1_000_000)
                .withoutRecipes()
                .build();

        MultiblockEntryBuilder.name("collision_chamber")
                .controller(collisionController)
                .ports(servicePort, beamPort)
                .casing(() -> get("target_chamber_casing").block().get(),
                        () -> get("target_chamber_casing_glass").block().get())
                .interior(() -> get("target_chamber_camera").block().get(),
                        () -> get("particle_beam").block().get(),
                        () -> get("silicon_tracker").block().get(),
                        () -> get("bubble_chamber").block().get(),
                        () -> get("wire_chamber").block().get(),
                        () -> get("em_calorimeter").block().get(),
                        () -> get("hadron_calorimeter").block().get())
                .sizeRange(Multiblocks.collisionChamberMinTransverseSize, Multiblocks.collisionChamberMaxTransverseSize,
                        Multiblocks.collisionChamberMinTransverseSize, Multiblocks.collisionChamberMaxTransverseSize,
                        Multiblocks.collisionChamberMinLength, Multiblocks.collisionChamberMaxLength)
                .scheduled((id, pos, facing) -> {
                            int minTransverse = Math.min(Multiblocks.collisionChamberMinTransverseSize,
                                    Multiblocks.collisionChamberMaxTransverseSize);
                            int maxTransverse = Math.max(Multiblocks.collisionChamberMinTransverseSize,
                                    Multiblocks.collisionChamberMaxTransverseSize);
                            int minLength = Math.min(Multiblocks.collisionChamberMinLength,
                                    Multiblocks.collisionChamberMaxLength);
                            int maxLength = Math.max(Multiblocks.collisionChamberMinLength,
                                    Multiblocks.collisionChamberMaxLength);
                            return new BoundaryGeometryDiscoveryJob(id, pos, facing,
                                    new BoundaryDiscoverySpec(BoundaryDiscoverySpec.Shape.COLLISION_BOX,
                                            minTransverse, maxTransverse, minTransverse, maxTransverse,
                                            minLength, maxLength, 0, ParticleChamber::isCollisionShell));
                        },
                        (id, record) -> new CollisionChamberValidationJob(id, collisionValidationContext(record)))
                .build();
    }

    private static TargetChamberValidationContext validationContext(StructureRecord record) {
        return new TargetChamberValidationContext(record,
                state -> is(state, "target_chamber_casing"),
                ParticleChamber::isTargetShell,
                state -> is(state, "target_chamber_controller"),
                state -> is(state, "target_chamber_port"),
                state -> is(state, "target_chamber_beam_port"),
                state -> is(state, "particle_beam"),
                state -> is(state, "target_chamber_camera"),
                state -> is(state, "particle_beam"),
                state -> state.getBlock() instanceof DetectorBlock detector ? detector.definition() : null);
    }

    private static boolean isTargetShell(BlockState state) {
        return is(state, "target_chamber_casing") || is(state, "target_chamber_casing_glass")
                || is(state, "target_chamber_controller") || is(state, "target_chamber_port")
                || is(state, "target_chamber_beam_port");
    }

    private static DecayChamberValidationContext decayValidationContext(StructureRecord record) {
        return new DecayChamberValidationContext(record,
                state -> is(state, "target_chamber_casing"),
                ParticleChamber::isDecayShell,
                state -> is(state, "decay_chamber_controller"),
                state -> is(state, "target_chamber_port"),
                state -> is(state, "target_chamber_beam_port"),
                state -> is(state, "particle_beam"),
                state -> is(state, "target_chamber_camera"),
                state -> is(state, "particle_beam"),
                state -> state.getBlock() instanceof DetectorBlock detector ? detector.definition() : null);
    }

    private static boolean isDecayShell(BlockState state) {
        return is(state, "target_chamber_casing") || is(state, "target_chamber_casing_glass")
                || is(state, "decay_chamber_controller") || is(state, "target_chamber_port")
                || is(state, "target_chamber_beam_port");
    }

    private static CollisionChamberValidationContext collisionValidationContext(StructureRecord record) {
        return new CollisionChamberValidationContext(record,
                state -> is(state, "target_chamber_casing"),
                ParticleChamber::isCollisionShell,
                state -> is(state, "collision_chamber_controller"),
                state -> is(state, "target_chamber_port"),
                state -> is(state, "target_chamber_beam_port"),
                state -> is(state, "particle_beam"),
                state -> is(state, "target_chamber_camera"),
                state -> is(state, "particle_beam"),
                state -> state.getBlock() instanceof DetectorBlock detector ? detector.definition() : null);
    }

    private static boolean isCollisionShell(BlockState state) {
        return is(state, "target_chamber_casing") || is(state, "target_chamber_casing_glass")
                || is(state, "collision_chamber_controller") || is(state, "target_chamber_port")
                || is(state, "target_chamber_beam_port");
    }

    private static boolean is(BlockState state, String name) {
        ModEntry entry = get(name);
        return entry != null && entry.block() != null && state.is(entry.block().get());
    }

    private static BlockBehaviour.Properties metalProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f, 6.0f)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties glassProps() {
        return metalProps().sound(SoundType.GLASS).noOcclusion();
    }
}
