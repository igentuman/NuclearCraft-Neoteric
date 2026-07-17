package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.block.MultiblockControllerBlock.HORIZONTAL_FACING;
import static igentuman.nc.block.MultiblockControllerBlock.POWERED;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static net.minecraft.world.item.Items.LEVER;

public class RingAcceleratorPonderScenes {

    // Structure NBT (assets/nuclearcraft/ponder/ring_accelerator.nbt) is 20x5x20.
    private static final int SIDE = 20;
    private static final int HEIGHT = 5;
    private static final int MAX = SIDE - 1;
    private static final int MID_Y = 2;
    // Outer ring is 5 wall blocks thick; hole sits at x,z in (5..14).
    private static final int INNER_LO = 5;
    private static final int INNER_HI = 14;
    // Beam ring runs through the middle of the wall, offset 2 from outer edge.
    private static final int BEAM_LO = 2;
    private static final int BEAM_HI = SIDE - 1 - 2;
    // Controller column (from NBT capture).
    private static final int CTRL_X = MAX;
    private static final int CTRL_Y = 1;
    private static final int CTRL_Z = 7;

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.RING_ACCELERATOR.getPath(), "Creating a Synchrotron Accelerator");
        scene.configureBasePlate(0, 0, SIDE);
        scene.scaleSceneView(0.45f);

        // Bottom layer (floor of donut)
        scene.world().showSection(util.select().fromTo(0, 0, 0, MAX, 0, MAX), Direction.UP);
        scene.idle(10);

        // Walls + top — reveal full casing torus
        scene.world().showSection(util.select().fromTo(0, 1, 0, MAX, HEIGHT - 1, MAX), Direction.DOWN);
        scene.overlay().showText(70)
                .pointAt(util.vector().centerOf(0, MID_Y, MID_Y))
                .text("Synchrotron casing forms a square torus, 5 blocks wide on each side.");
        scene.idle(75);

        // Beam ring at mid Y
        scene.addKeyframe();
        scene.world().showSection(util.select()
                .fromTo(BEAM_LO, MID_Y, BEAM_LO, BEAM_HI, MID_Y, BEAM_HI), Direction.UP);
        scene.overlay().showText(75)
                .pointAt(util.vector().centerOf(BEAM_LO, MID_Y, BEAM_LO))
                .text("A continuous ring of Particle Beam blocks runs through the middle of all four sides.");
        scene.idle(80);

        // Corner dipoles (electromagnet pillars + yokes near each corner of beam ring)
        scene.addKeyframe();
        int[][] corners = {
                {BEAM_LO, BEAM_LO}, {BEAM_HI, BEAM_LO},
                {BEAM_LO, BEAM_HI}, {BEAM_HI, BEAM_HI}
        };
        for (int[] c : corners) {
            scene.world().showSection(util.select()
                    .fromTo(c[0] - 1, MID_Y - 1, c[1] - 1, c[0] + 1, MID_Y + 1, c[1] + 1), Direction.DOWN);
            scene.idle(4);
        }
        scene.overlay().showText(80)
                .pointAt(util.vector().centerOf(BEAM_LO, MID_Y, BEAM_LO))
                .text("Place a dipole magnet at every corner: electromagnet above and below the beam, yokes around.");
        scene.idle(85);

        // Inside-corner coolers (interior corner columns of the donut hole)
        scene.addKeyframe();
        scene.world().showSection(util.select()
                .fromTo(INNER_LO, 1, INNER_LO, INNER_LO, HEIGHT - 2, INNER_LO), Direction.DOWN);
        scene.world().showSection(util.select()
                .fromTo(INNER_HI, 1, INNER_LO, INNER_HI, HEIGHT - 2, INNER_LO), Direction.DOWN);
        scene.world().showSection(util.select()
                .fromTo(INNER_LO, 1, INNER_HI, INNER_LO, HEIGHT - 2, INNER_HI), Direction.DOWN);
        scene.world().showSection(util.select()
                .fromTo(INNER_HI, 1, INNER_HI, INNER_HI, HEIGHT - 2, INNER_HI), Direction.DOWN);
        scene.overlay().showText(75)
                .pointAt(util.vector().centerOf(INNER_LO, MID_Y, INNER_LO))
                .text("Inside corners of the ring may host coolers to dump heat.");
        scene.idle(80);

        // Ports + controller on east outer wall
        scene.addKeyframe();
        scene.world().showSection(util.select()
                .fromTo(CTRL_X, 1, 7, CTRL_X, 2, 13), Direction.WEST);
        scene.world().showSection(util.select()
                .fromTo(17, 2, 19, 19, 2, 19), Direction.NORTH);
        scene.world().showSection(util.select()
                .fromTo(CTRL_X, 2, 2, CTRL_X, 2, 2), Direction.WEST);
        scene.world().showSection(util.select()
                .fromTo(CTRL_X, 2, 17, CTRL_X, 2, 17), Direction.WEST);
        scene.overlay().showText(85)
                .pointAt(util.vector().centerOf(CTRL_X, CTRL_Y, CTRL_Z))
                .text("Use Ring Accelerator Ports for energy, fluids and redstone, and Beam Ports for particle input/output. The generic Accelerator Port and Ion Source Port do not fit on a ring.");
        scene.idle(90);

        // Power on
        scene.world().setBlock(util.grid().at(CTRL_X, CTRL_Y, CTRL_Z),
                ACCELERATOR_BLOCKS.get("ring_accelerator_controller").get().defaultBlockState()
                        .setValue(HORIZONTAL_FACING, Direction.EAST)
                        .setValue(POWERED, true),
                false);
        scene.overlay().showControls(util.vector().topOf(CTRL_X, CTRL_Y, CTRL_Z), Pointing.LEFT, 55)
                .withItem(new ItemStack(LEVER)).rightClick();
        scene.overlay().showText(75)
                .text("Pipe in an existing beam at 5 MeV or higher, then power the controller. Redstone strength scales output energy.");
        scene.idle(80);
    }
}
