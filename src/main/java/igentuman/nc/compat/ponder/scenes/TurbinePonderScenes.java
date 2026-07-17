package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.compat.ponder.PonderScenes;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;

import static igentuman.nc.block.MultiblockControllerBlock.POWERED;
import static igentuman.nc.block.turbine.TurbinePortBlock.HORIZONTAL_FACING;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;

public class TurbinePonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.TURBINE.getPath(), "Creating a Turbine");
        scene.world().setBlock(util.grid().at(4, 3, 0), TURBINE_BLOCKS.get("turbine_controller").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH), false);
        scene.world().setBlock(util.grid().at(0, 3, 3), TURBINE_BLOCKS.get("turbine_bearing").get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(8, 3, 3), TURBINE_BLOCKS.get("turbine_bearing").get().defaultBlockState(), false);

        scene.idle(10);
        // Show base
        scene.world().showSection(util.select().fromTo(0, 0, 0, 8, 0, 6), Direction.UP);
        scene.idle(10);
        // Show back wall
        scene.world().showSection(util.select().fromTo(0, 1, 6, 8, 6, 6), Direction.NORTH);
        scene.idle(10);
        // Show front wall
        scene.world().showSection(util.select().fromTo(0, 1, 0, 8, 6, 0), Direction.SOUTH);
        scene.idle(10);
        // Show left end
        scene.world().showSection(util.select().fromTo(0, 1, 1, 0, 6, 5), Direction.EAST);
        scene.idle(10);
        // Show right end
        scene.world().showSection(util.select().fromTo(8, 1, 1, 8, 6, 5), Direction.WEST);
        scene.idle(10);
        // Show top
        scene.world().showSection(util.select().fromTo(1, 6, 1, 7, 6, 5), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(55)
                .pointAt(util.vector().blockSurface(util.grid().at(4, 0, 3), Direction.UP))
                .text("Walls are mainly made of Turbine Casing or Turbine Glass.");

        scene.idle(55);
        scene.overlay().showText(55)
                .pointAt(util.vector().blockSurface(util.grid().at(4, 0, 3), Direction.UP))
                .text("Turbine can have vertical or horizontal orientation.");

        scene.idle(55);
        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().fromTo(4, 3, 0, 4, 3, 0), 55);
        scene.overlay().showText(55)
                .pointAt(util.vector().blockSurface(util.grid().at(4, 3, 0), Direction.UP))
                .text("Place the Turbine Controller anywhere in the casing to form the structure.");

        scene.idle(55);
        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(4, 1, 0), TURBINE_BLOCKS.get("turbine_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH), true);
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().fromTo(4, 1, 0, 4, 1, 0), 55);
        scene.overlay().showText(50)
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 0), Direction.UP))
                .text("Turbine Ports allow fluid and energy transfer.");
        scene.idle(55);

        scene.addKeyframe();
        // Hide some casing to show internals
        scene.world().hideSection(util.select().fromTo(0, 1, 0, 8, 6, 0), Direction.UP);
        scene.world().hideSection(util.select().fromTo(1, 6, 1, 7, 6, 5), Direction.UP);

        scene.idle(15);

        // Show Bearings
        scene.overlay().showText(40).pointAt(util.vector().centerOf(0, 3, 3)).text("Bearings are placed at the center of the casing ends.");
        scene.idle(40);

        // Show Rotor Shaft
        for (int x = 1; x <= 7; x++) {
            scene.world().setBlock(util.grid().at(x, 3, 3), TURBINE_BLOCKS.get("turbine_rotor_shaft").get().defaultBlockState().setValue(TurbineRotorBlock.FACING, Direction.EAST), true);
            scene.world().showSection(util.select().position(x, 3, 3), Direction.DOWN);
            scene.idle(2);
        }
        scene.rotateCameraY(45);
        scene.overlay().showText(40).pointAt(util.vector().centerOf(4, 3, 3)).text("The Rotor Shaft connects the two bearings.");
        scene.idle(40);

        // Show Blades
        scene.addKeyframe();
        for (int x = 1; x <= 7; x++) {
            scene.world().showSection(util.select().fromTo(x, 1, 1, x, 5, 5), null);
            scene.idle(5);
        }
        scene.overlay().showText(40).text("Attach Turbine Blades to the Rotor Shaft.");
        scene.rotateCameraY(-45);
        scene.idle(40);

        // Show Coils
        scene.addKeyframe();
        scene.overlay().showText(40).text("Coils must be placed next to a bearing or another active coil.");
        scene.idle(10);
        int[][] coilOffsets = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] offset : coilOffsets) {
            scene.world().setBlock(util.grid().at(0, 3 + offset[0], 3 + offset[1]), TURBINE_BLOCKS.get("turbine_magnesium_coil").get().defaultBlockState(), true);
            scene.world().showSection(util.select().position(0, 3 + offset[0], 3 + offset[1]), Direction.EAST);
            scene.idle(5);
        }
        scene.idle(55);

        scene.world().showSection(util.select().fromTo(0, 1, 0, 8, 6, 0), Direction.SOUTH);
        scene.world().showSection(util.select().fromTo(1, 6, 1, 7, 6, 5), Direction.DOWN);
        scene.idle(10);
        scene.world().setBlock(util.grid().at(4, 3, 0), TURBINE_BLOCKS.get("turbine_controller").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(POWERED, true), false);

        scene.addKeyframe();
        scene.overlay().showText(40).text("When turbine is ready, you can start it with redstone signal to controller.");
        scene.idle(55);
    }
}
