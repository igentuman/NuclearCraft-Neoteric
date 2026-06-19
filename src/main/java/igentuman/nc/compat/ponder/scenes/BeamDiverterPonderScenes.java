package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import igentuman.nc.util.PortMode;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.block.MultiblockControllerBlock.HORIZONTAL_FACING;
import static igentuman.nc.block.MultiblockControllerBlock.POWERED;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.NC_ELECTROMAGNETS;
import static igentuman.nc.util.PortMode.PORT_MODE;
import static net.minecraft.world.item.Items.LEVER;

public class BeamDiverterPonderScenes {

    // Fixed 5x5x5; center at (2,2,2). Beam ports sit at the center of each vertical wall.
    private static final int MAX = 4;
    private static final int MID = 2;

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.BEAM_DIVERTER.getPath(), "Creating a Beam Diverter");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.95f);

        BlockState casing = ACCELERATOR_BLOCKS.get("accelerator_casing").get().defaultBlockState();
        BlockState glass = ACCELERATOR_BLOCKS.get("accelerator_casing_glass").get().defaultBlockState();
        BlockState beam = ACCELERATOR_BLOCKS.get("particle_beam").get().defaultBlockState();
        BlockState yoke = ACCELERATOR_BLOCKS.get("electromagnet_yoke").get().defaultBlockState();
        BlockState magnet = NC_ELECTROMAGNETS.get("basic_electromagnet").get().defaultBlockState();
        BlockState port = ACCELERATOR_BLOCKS.get("accelerator_beam_port").get().defaultBlockState();

        // Outer shell: corners/edges casing, faces glass.
        for (int y = 0; y <= MAX; y++) {
            for (int x = 0; x <= MAX; x++) {
                for (int z = 0; z <= MAX; z++) {
                    boolean shell = x == 0 || x == MAX || y == 0 || y == MAX || z == 0 || z == MAX;
                    if (!shell) continue;
                    int extremes = (x == 0 || x == MAX ? 1 : 0)
                            + (y == 0 || y == MAX ? 1 : 0)
                            + (z == 0 || z == MAX ? 1 : 0);
                    scene.world().setBlock(util.grid().at(x, y, z), extremes >= 2 ? casing : glass, false);
                }
            }
        }

        // Inner 3x3x3 optics: beam cross at mid height, dipole magnet above/below center, yokes elsewhere.
        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                for (int z = 1; z <= 3; z++) {
                    int dx = x - MID, dy = y - MID, dz = z - MID;
                    BlockState bs;
                    if (dy == 0 && (dx == 0 || dz == 0)) {
                        bs = beam;
                    } else if (dx == 0 && dz == 0 && Math.abs(dy) == 1) {
                        bs = magnet;
                    } else {
                        bs = yoke;
                    }
                    scene.world().setBlock(util.grid().at(x, y, z), bs, false);
                }
            }
        }

        // Four beam ports at the wall centers (default disabled), and the controller on the north wall.
        scene.world().setBlock(util.grid().at(MID, MID, 0), port, false);
        scene.world().setBlock(util.grid().at(MID, MID, MAX), port, false);
        scene.world().setBlock(util.grid().at(0, MID, MID), port, false);
        scene.world().setBlock(util.grid().at(MAX, MID, MID), port, false);
        scene.world().setBlock(util.grid().at(MID, MID + 1, 0),
                ACCELERATOR_BLOCKS.get("beam_diverter_controller").get().defaultBlockState()
                        .setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(POWERED, false), false);

        // Floor
        scene.world().showSection(util.select().fromTo(0, 0, 0, MAX, 0, MAX), Direction.UP);
        scene.idle(10);

        // Walls + top (shell only - the interior stays hidden)
        scene.world().showSection(util.select().fromTo(0, 1, 0, MAX, MAX, 0), Direction.SOUTH);
        scene.world().showSection(util.select().fromTo(0, 1, MAX, MAX, MAX, MAX), Direction.NORTH);
        scene.world().showSection(util.select().fromTo(0, 1, 1, 0, MAX, 3), Direction.EAST);
        scene.world().showSection(util.select().fromTo(MAX, 1, 1, MAX, MAX, 3), Direction.WEST);
        scene.world().showSection(util.select().fromTo(0, MAX, 0, MAX, MAX, MAX), Direction.DOWN);
        scene.overlay().showText(70)
                .pointAt(util.vector().centerOf(MID, MID, 0))
                .text("The Beam Diverter is a fixed 5x5x5 cube of Accelerator Casing and Glass.");
        scene.idle(75);

        // Beam ports
        scene.addKeyframe();
        scene.overlay().showText(75)
                .pointAt(util.vector().centerOf(0, MID, MID))
                .text("An Accelerator Beam Port sits at the center of each of the four walls.");
        scene.idle(80);

        // Beam cross
        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(1, MID, 1, 3, MID, 3), Direction.UP);
        scene.overlay().showText(75)
                .pointAt(util.vector().centerOf(MID, MID, MID))
                .text("Inside, a cross of Particle Beam blocks links the four ports through the center.");
        scene.idle(80);

        // Dipole magnet
        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(1, 1, 1, 3, 1, 3), Direction.UP);
        scene.world().showSection(util.select().fromTo(1, 3, 1, 3, 3, 3), Direction.DOWN);
        scene.overlay().showText(80)
                .pointAt(util.vector().centerOf(MID, MID + 1, MID))
                .text("A dipole magnet - an electromagnet above and below the center, with yokes filling the rest - bends the beam.");
        scene.idle(85);

        // Port modes
        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(MID, MID, 0), port.setValue(PORT_MODE, PortMode.Mode.INPUT), false);
        scene.world().setBlock(util.grid().at(MID, MID, MAX), port.setValue(PORT_MODE, PortMode.Mode.OUTPUT), false);
        scene.world().setBlock(util.grid().at(0, MID, MID), port.setValue(PORT_MODE, PortMode.Mode.OUTPUT), false);
        scene.world().setBlock(util.grid().at(MAX, MID, MID), port.setValue(PORT_MODE, PortMode.Mode.OUTPUT), false);
        scene.overlay().showText(80)
                .pointAt(util.vector().centerOf(MID, MID, 0))
                .text("Set exactly one port to Input and at least one to Output - with the Multitool, redstone, or a computer.");
        scene.idle(85);

        // Routing behaviour
        scene.addKeyframe();
        scene.overlay().showText(90)
                .pointAt(util.vector().centerOf(MID, MID, MID))
                .text("The diverter reroutes the incoming beam to the active output. A 90 degree turn costs energy that scales with dipole strength; a straight pass-through only loses focus.");
        scene.idle(95);

        // Power on
        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(MID, MID + 1, 0),
                ACCELERATOR_BLOCKS.get("beam_diverter_controller").get().defaultBlockState()
                        .setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(POWERED, true), false);
        scene.overlay().showControls(util.vector().topOf(MID, MID + 1, 0), Pointing.DOWN, 60)
                .withItem(new ItemStack(LEVER)).rightClick();
        scene.overlay().showText(80)
                .pointAt(util.vector().centerOf(MID, MID + 1, 0))
                .text("Mount the Beam Diverter Controller in the casing and power it to start routing.");
        scene.idle(85);
    }
}
