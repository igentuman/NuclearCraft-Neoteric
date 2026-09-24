package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.block.accelerator.AcceleratorBeamPortBlock;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.compat.ponder.PonderScenes;
import igentuman.nc.setup.ModEntries;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class BeamDiverterPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.BEAM_DIVERTER.getPath(), "Creating a Beam Diverter");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.95f);

        BlockState casing = ModEntries.get("accelerator_casing").block().get().defaultBlockState();
        BlockState glass = ModEntries.get("accelerator_casing_glass").block().get().defaultBlockState();
        BlockState beam = ModEntries.get("particle_beam").block().get().defaultBlockState();
        BlockState yoke = ModEntries.get("electromagnet_yoke").block().get().defaultBlockState();
        BlockState magnet = ModEntries.get("basic_electromagnet").block().get().defaultBlockState();
        BlockState port = ModEntries.get("accelerator_beam_port").block().get().defaultBlockState();

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = 0; z < 5; z++) {
                    int edges = (x == 0 || x == 4 ? 1 : 0) + (y == 0 || y == 4 ? 1 : 0)
                            + (z == 0 || z == 4 ? 1 : 0);
                    if (edges > 0) {
                        scene.world().setBlock(util.grid().at(x, y, z), edges > 1 ? casing : glass, false);
                    } else {
                        BlockState state = y == 2 && (x == 2 || z == 2) ? beam
                                : x == 2 && z == 2 ? magnet : yoke;
                        scene.world().setBlock(util.grid().at(x, y, z), state, false);
                    }
                }
            }
        }
        scene.world().setBlock(util.grid().at(2, 2, 0), port.setValue(HORIZONTAL_FACING, Direction.NORTH), false);
        scene.world().setBlock(util.grid().at(2, 2, 4), port.setValue(HORIZONTAL_FACING, Direction.SOUTH)
                .setValue(AcceleratorBeamPortBlock.PORT_MODE, BeamPortMode.OUTPUT), false);
        scene.world().setBlock(util.grid().at(0, 2, 2), port.setValue(HORIZONTAL_FACING, Direction.WEST)
                .setValue(AcceleratorBeamPortBlock.PORT_MODE, BeamPortMode.INPUT), false);
        scene.world().setBlock(util.grid().at(4, 2, 2), port.setValue(HORIZONTAL_FACING, Direction.EAST), false);
        scene.world().setBlock(util.grid().at(0, 1, 3),
                ModEntries.get("beam_diverter_controller").block().get().defaultBlockState()
                        .setValue(HORIZONTAL_FACING, Direction.WEST), false);
        scene.world().setBlock(util.grid().at(0, 1, 1),
                ModEntries.get("accelerator_port").block().get().defaultBlockState()
                        .setValue(HORIZONTAL_FACING, Direction.WEST), false);

        scene.world().showSection(util.select().fromTo(0, 0, 0, 4, 0, 4), Direction.UP);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 4, 0), Direction.SOUTH);
        scene.world().showSection(util.select().fromTo(0, 1, 4, 4, 4, 4), Direction.NORTH);
        scene.world().showSection(util.select().fromTo(0, 1, 1, 0, 4, 3), Direction.EAST);
        scene.world().showSection(util.select().fromTo(4, 1, 1, 4, 4, 3), Direction.WEST);
        scene.world().showSection(util.select().fromTo(1, 4, 1, 3, 4, 3), Direction.DOWN);
        scene.overlay().showText(65).text("The Beam Diverter is a fixed 5x5x5 shell of Accelerator Casing and Glass.");
        scene.idle(70);

        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(),
                util.select().position(2, 2, 0).add(util.select().position(2, 2, 4))
                        .add(util.select().position(0, 2, 2)).add(util.select().position(4, 2, 2)), 65);
        scene.overlay().showText(65).text("Put a Beam Port at the center of each horizontal wall.");
        scene.idle(70);

        scene.addKeyframe();
        scene.world().hideSection(util.select().fromTo(1, 4, 1, 3, 4, 3), Direction.UP);
        scene.world().showSection(util.select().fromTo(1, 2, 1, 3, 2, 3), Direction.UP);
        scene.overlay().showText(65).pointAt(util.vector().centerOf(2, 2, 2))
                .text("A cross of Particle Beam blocks joins the four ports through the center.");
        scene.idle(70);

        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(1, 1, 1, 3, 1, 3), Direction.UP);
        scene.world().showSection(util.select().fromTo(1, 3, 1, 3, 3, 3), Direction.DOWN);
        scene.overlay().showOutline(PonderPalette.GREEN, new Object(),
                util.select().position(2, 1, 2).add(util.select().position(2, 3, 2)), 70);
        scene.overlay().showText(70).text("Matching Electromagnets above and below the center bend the beam; yokes fill the gaps.");
        scene.idle(75);

        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(),
                util.select().position(0, 1, 3).add(util.select().position(0, 1, 1)), 65);
        scene.overlay().showText(65).text("Put the controller and an Accelerator Port in the casing; supply power and redstone.");
        scene.idle(70);

        scene.addKeyframe();
        scene.overlay().showText(70).text("The controller accepts one incoming beam and routes it to an output; turns cost energy.");
        scene.idle(75);
    }
}
