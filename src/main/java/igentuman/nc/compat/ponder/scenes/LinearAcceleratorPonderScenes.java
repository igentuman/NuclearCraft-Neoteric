package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import igentuman.nc.block.accelerator.AcceleratorBeamPortBlock;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.setup.ModEntries;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class LinearAcceleratorPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.LINEAR_ACCELERATOR.getPath(), "Creating a Linear Accelerator");
        scene.configureBasePlate(0, -12, 25);
        scene.scaleSceneView(0.5f);

        BlockState beam = ModEntries.get("particle_beam").block().get().defaultBlockState();
        for (int x = 1; x < 24; x++) {
            scene.world().setBlock(util.grid().at(x, 2, 2), beam, false);
        }
        scene.world().setBlock(util.grid().at(0, 2, 2),
                ModEntries.get("accelerator_beam_port").block().get().defaultBlockState()
                        .setValue(HORIZONTAL_FACING, Direction.WEST)
                        .setValue(AcceleratorBeamPortBlock.PORT_MODE, BeamPortMode.OUTPUT), false);
        scene.world().setBlock(util.grid().at(24, 2, 2),
                ModEntries.get("accelerator_ion_source_port").block().get().defaultBlockState()
                        .setValue(HORIZONTAL_FACING, Direction.EAST), false);
        scene.world().setBlock(util.grid().at(12, 1, 4),
                ModEntries.get("linear_accelerator_controller").block().get().defaultBlockState()
                        .setValue(HORIZONTAL_FACING, Direction.SOUTH), false);
        scene.world().setBlock(util.grid().at(9, 1, 4),
                ModEntries.get("accelerator_port").block().get().defaultBlockState()
                        .setValue(HORIZONTAL_FACING, Direction.SOUTH), false);

        scene.world().showSection(util.select().fromTo(0, 0, 0, 24, 0, 4), Direction.UP);
        scene.world().showSection(util.select().fromTo(1, 2, 2, 23, 2, 2), Direction.DOWN);
        scene.overlay().showText(65).pointAt(util.vector().centerOf(12, 2, 2))
                .text("A straight Particle Beam line runs through a 5x5 Accelerator shell.");
        scene.idle(70);

        scene.addKeyframe();
        scene.world().showSection(util.select().position(0, 2, 2), Direction.EAST);
        scene.world().showSection(util.select().position(24, 2, 2), Direction.WEST);
        scene.overlay().showOutline(PonderPalette.GREEN, new Object(), util.select().position(24, 2, 2), 65);
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(), util.select().position(0, 2, 2), 65);
        scene.overlay().showText(65).text("Feed particles through an Ion Source or input Beam Port; take them out at the far end.");
        scene.idle(70);

        scene.addKeyframe();
        for (int x : new int[]{3, 7, 11, 15, 19}) {
            scene.world().showSection(util.select().fromTo(x, 1, 1, x, 3, 3), Direction.DOWN);
            scene.idle(4);
        }
        scene.overlay().showText(65).pointAt(util.vector().centerOf(11, 2, 2))
                .text("RF Amplifiers around a beam block raise particle energy.");
        scene.idle(70);

        scene.addKeyframe();
        for (int x : new int[]{5, 9, 13, 17, 21}) {
            scene.world().showSection(util.select().fromTo(x, 1, 1, x, 3, 3), Direction.DOWN);
            scene.idle(4);
        }
        scene.overlay().showText(65).pointAt(util.vector().centerOf(13, 2, 2))
                .text("Four Electromagnets around the beam improve its focus.");
        scene.idle(70);

        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(4, 1, 1, 20, 3, 3), Direction.DOWN);
        scene.overlay().showText(60).pointAt(util.vector().centerOf(12, 1, 1))
                .text("Add Accelerator Coolers to carry away the heat from beam components.");
        scene.idle(65);

        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(0, 1, 0, 24, 4, 0), Direction.SOUTH);
        scene.world().showSection(util.select().fromTo(0, 1, 4, 24, 4, 4), Direction.NORTH);
        scene.world().showSection(util.select().fromTo(0, 1, 1, 0, 4, 3), Direction.EAST);
        scene.world().showSection(util.select().fromTo(24, 1, 1, 24, 4, 3), Direction.WEST);
        scene.world().showSection(util.select().fromTo(0, 4, 0, 24, 4, 4), Direction.DOWN);
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(),
                util.select().position(12, 1, 4).add(util.select().position(9, 1, 4)), 65);
        scene.overlay().showText(65).text("Finish the casing, connect power at an Accelerator Port, and signal the controller.");
        scene.idle(70);
    }
}
