package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.block.MultiblockControllerBlock.POWERED;
import static igentuman.nc.block.fission.MSRPortBlock.HORIZONTAL_FACING;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;

public class MoltenSaltReactorPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.MOLTEN_SALT_REACTOR.getPath(), "Building a Molten Salt Reactor");

        BlockState glass = FISSION_BLOCKS.get("fission_reactor_glass").get().defaultBlockState();

        // Reset the wall blocks the structure ships with so they can be animated in later.
        scene.world().setBlock(util.grid().at(3, 1, 0), glass, false); // controller spot
        scene.world().setBlock(util.grid().at(0, 1, 2), glass, false);
        scene.world().setBlock(util.grid().at(0, 1, 4), glass, false);
        scene.world().setBlock(util.grid().at(6, 1, 2), glass, false);
        scene.world().setBlock(util.grid().at(6, 1, 4), glass, false);

        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0, 0, 0, 6, 0, 6), Direction.UP);
        scene.idle(6);
        scene.world().showSection(util.select().fromTo(0, 6, 0, 6, 6, 6), Direction.DOWN);
        scene.idle(6);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 6, 5, 0), Direction.SOUTH);
        scene.idle(6);
        scene.world().showSection(util.select().fromTo(0, 1, 6, 6, 5, 6), Direction.NORTH);
        scene.idle(6);
        scene.world().showSection(util.select().fromTo(0, 1, 1, 0, 5, 5), Direction.EAST);
        scene.idle(6);
        scene.world().showSection(util.select().fromTo(6, 1, 1, 6, 5, 5), Direction.WEST);
        scene.idle(8);
        scene.overlay().showText(70)
                .pointAt(util.vector().blockSurface(util.grid().at(3, 3, 0), Direction.NORTH))
                .text("A cuboid shell of Reactor Casing edges and Reactor Glass walls, from 5x5x5 up to 26x26x26.");
        scene.idle(75);

        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(3, 1, 0), FISSION_BLOCKS.get("msr_controller").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH), true);
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().fromTo(3, 1, 0, 3, 1, 0), 55);
        scene.overlay().showText(55)
                .pointAt(util.vector().blockSurface(util.grid().at(3, 1, 0), Direction.NORTH))
                .text("Place one MSR Controller. It runs the reaction and owns the salt tanks and pebble slots.");
        scene.idle(60);

        scene.addKeyframe();
        BlockState portWest = FISSION_BLOCKS.get("msr_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.WEST);
        BlockState portEast = FISSION_BLOCKS.get("msr_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.EAST);
        scene.world().setBlock(util.grid().at(0, 1, 2), portWest, true);
        scene.world().setBlock(util.grid().at(0, 1, 4), portWest, true);
        scene.world().setBlock(util.grid().at(6, 1, 2), portEast, true);
        scene.world().setBlock(util.grid().at(6, 1, 4), portEast, true);
        scene.overlay().showOutline(PonderPalette.FAST, new Object(), util.select().fromTo(0, 1, 2, 0, 1, 2), 55);
        scene.overlay().showOutline(PonderPalette.FAST, new Object(), util.select().fromTo(6, 1, 4, 6, 1, 4), 55);
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(util.grid().at(0, 1, 2), Direction.WEST))
                .text("Ports move salt and pebbles in and out: cold salt in, hot salt out, pebbles in, depleted out.");
        scene.idle(65);

        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(), util.select().fromTo(3, 5, 0, 3, 5, 0), 50);
        scene.overlay().showText(55)
                .pointAt(util.vector().blockSurface(util.grid().at(3, 5, 0), Direction.NORTH))
                .text("An Irradiator can sit in the wall and add irradiation to the chamber.");
        scene.idle(60);

        scene.addKeyframe();
        scene.world().hideSection(util.select().fromTo(0, 0, 0, 6, 6, 0), Direction.SOUTH);
        scene.idle(15);
        for (int y = 1; y <= 5; y++) {
            for (int z = 1; z <= 5; z++) {
                for (int x = 1; x <= 5; x++) {
                    scene.world().showSection(util.select().position(x, y, z), Direction.DOWN);
                    scene.idle(1);
                }
            }
        }
        scene.overlay().showText(75)
                .pointAt(util.vector().centerOf(util.grid().at(3, 3, 3)))
                .text("Fill the entire interior with MSR Fuel Cells. More cells mean more salt volume and a bigger heat budget.");
        scene.idle(80);

        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(0, 0, 0, 6, 6, 0), Direction.NORTH);
        scene.idle(15);
        scene.world().setBlock(util.grid().at(3, 1, 0), FISSION_BLOCKS.get("msr_controller").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(POWERED, true), false);
        scene.overlay().showText(80)
                .pointAt(util.vector().blockSurface(util.grid().at(3, 1, 0), Direction.NORTH))
                .text("Pipe cold FLiBe salt in, load TRISO pebbles, apply redstone. The core heats up and turns cold salt into hot salt - send it to a Heat Exchanger.");
        scene.idle(80);
    }
}
