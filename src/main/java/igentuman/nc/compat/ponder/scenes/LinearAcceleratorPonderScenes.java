package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import igentuman.nc.util.PortMode;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.block.MultiblockControllerBlock.HORIZONTAL_FACING;
import static igentuman.nc.block.MultiblockControllerBlock.POWERED;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.NC_ELECTROMAGNETS;
import static igentuman.nc.setup.registration.NCBlocks.NC_RF_AMPLIFIERS;
import static igentuman.nc.setup.registration.NCItems.MULTITOOL;
import static igentuman.nc.util.PortMode.PORT_MODE;
import static net.minecraft.world.item.Items.LEVER;

public class LinearAcceleratorPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.LINEAR_ACCELERATOR.getPath(), "Creating a Linear Accelerator");
        scene.configureBasePlate(0, -12, 25);
        scene.scaleSceneView(0.5f);
        int length = 25;
        int height = 5;
        int width = 5;

        // Base Casing
        scene.world().showSection(util.select().fromTo(0, 0, 0, length - 1, 0, width - 1), Direction.UP);
        scene.idle(10);

        // Particle Beam line
        for (int x = 1; x < length - 1; x++) {
            scene.world().setBlock(util.grid().at(x, 2, 2), ACCELERATOR_BLOCKS.get("particle_beam").get().defaultBlockState(), false);
        }
        scene.world().showSection(util.select().fromTo(1, 2, 2, length - 2, 2, 2), Direction.NORTH);
        scene.idle(10);

        // Ion Source Port (Input)
        scene.world().setBlock(util.grid().at(0, 2, 2), ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.WEST), false);
        scene.world().showSection(util.select().position(0, 2, 2), Direction.EAST);
        scene.overlay().showText(40).pointAt(util.vector().centerOf(0, 2, 2)).text("One end needs an Ion Source Port or Particle Beam Port (Input).");
        scene.idle(45);

        // Beam Port (Output)
        scene.world().setBlock(util.grid().at(length - 1, 2, 2), ACCELERATOR_BLOCKS.get("accelerator_beam_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.EAST).setValue(PORT_MODE, PortMode.Mode.OUTPUT), false);
        scene.world().showSection(util.select().position(length - 1, 2, 2), Direction.WEST);
        scene.overlay().showText(40).pointAt(util.vector().centerOf(length - 1, 2, 2)).text("The opposite end needs a Beam Port (Output).");
        scene.overlay().showControls(util.vector().topOf(length - 1, 2, 2), Pointing.RIGHT, 55).withItem(new ItemStack(MULTITOOL.get())).rightClick();
        scene.idle(45);

        // RF Amplifiers (around beam)
        scene.addKeyframe();
        int[] ampPositions = {3, 7, 11, 15, 19};
        for (int ampPos : ampPositions) {
            for (int y = 1; y <= 3; y++) {
                for (int z = 1; z <= 3; z++) {
                    if (y == 2 && z == 2) continue;
                    scene.world().setBlock(util.grid().at(ampPos, y, z), NC_RF_AMPLIFIERS.get("basic_rf_amplifier").get().defaultBlockState(), false);
                }
            }
            scene.world().showSection(util.select().fromTo(ampPos, 1, 1, ampPos, 3, 3), Direction.DOWN);
            scene.idle(5);
        }
        scene.overlay().showText(60).pointAt(util.vector().centerOf(ampPositions[2], 2, 2)).text("RF Amplifiers increase particle energy. Place 8 blocks around a beam block.");
        scene.idle(65);

        // Electromagnets (around beam)
        scene.addKeyframe();
        int[] magnetPositions = {5, 9, 13, 17, 21};
        for (int magnetPos : magnetPositions) {
            scene.world().setBlock(util.grid().at(magnetPos, 1, 2), NC_ELECTROMAGNETS.get("basic_electromagnet").get().defaultBlockState(), false);
            scene.world().setBlock(util.grid().at(magnetPos, 3, 2), NC_ELECTROMAGNETS.get("basic_electromagnet").get().defaultBlockState(), false);
            scene.world().setBlock(util.grid().at(magnetPos, 2, 1), NC_ELECTROMAGNETS.get("basic_electromagnet").get().defaultBlockState(), false);
            scene.world().setBlock(util.grid().at(magnetPos, 2, 3), NC_ELECTROMAGNETS.get("basic_electromagnet").get().defaultBlockState(), false);
            scene.world().showSection(util.select().position(magnetPos, 1, 2), null);
            scene.world().showSection(util.select().position(magnetPos, 3, 2), null);
            scene.world().showSection(util.select().position(magnetPos, 2, 1), null);
            scene.world().showSection(util.select().position(magnetPos, 2, 3), null);
            scene.idle(5);
        }
        scene.overlay().showText(60).pointAt(util.vector().centerOf(magnetPositions[2], 2, 2)).text("Electromagnets increase beam focus. Place 4 blocks around a beam block.");
        scene.idle(65);

        // Coolers
        scene.addKeyframe();
        int[] coolerPositions = {4, 6, 8, 10, 12, 14, 16, 18, 20};
        for (int coolerPos : coolerPositions) {
            scene.world().setBlock(util.grid().at(coolerPos, 1, 1), ACCELERATOR_BLOCKS.get("water_cooler").get().defaultBlockState(), false);
            scene.world().setBlock(util.grid().at(coolerPos, 1, 3), ACCELERATOR_BLOCKS.get("water_cooler").get().defaultBlockState(), false);
            scene.world().setBlock(util.grid().at(coolerPos, 2, 1), ACCELERATOR_BLOCKS.get("water_cooler").get().defaultBlockState(), false);
            scene.world().setBlock(util.grid().at(coolerPos, 2, 3), ACCELERATOR_BLOCKS.get("water_cooler").get().defaultBlockState(), false);
            scene.world().setBlock(util.grid().at(coolerPos, 3, 1), ACCELERATOR_BLOCKS.get("water_cooler").get().defaultBlockState(), false);
            scene.world().setBlock(util.grid().at(coolerPos, 3, 3), ACCELERATOR_BLOCKS.get("water_cooler").get().defaultBlockState(), false);
            scene.world().showSection(util.select().position(coolerPos, 1, 1), Direction.DOWN);
            scene.world().showSection(util.select().position(coolerPos, 1, 3), Direction.DOWN);
            scene.world().showSection(util.select().position(coolerPos, 2, 1), Direction.DOWN);
            scene.world().showSection(util.select().position(coolerPos, 2, 3), Direction.DOWN);
            scene.world().showSection(util.select().position(coolerPos, 3, 1), Direction.DOWN);
            scene.world().showSection(util.select().position(coolerPos, 3, 3), Direction.DOWN);
        }
        scene.overlay().showText(60).pointAt(util.vector().centerOf(coolerPositions[4], 1, 1)).text("Accelerator Coolers must be placed inside to regulate temperature.");
        scene.idle(65);

        // Finish structure
        scene.addKeyframe();

        scene.world().showSection(util.select().fromTo(0, 1, 0, length - 1, height - 1, 0), Direction.SOUTH);
        scene.world().showSection(util.select().fromTo(0, 1, width - 1, length - 1, height - 1, width - 1), Direction.NORTH);
        scene.world().showSection(util.select().fromTo(0, 1, 1, 0, height - 1, width - 2), Direction.EAST);
        scene.world().showSection(util.select().fromTo(length - 1, 1, 1, length - 1, height - 1, width - 2), Direction.WEST);
        scene.world().showSection(util.select().fromTo(0, height - 1, 0, length - 1, height - 1, width - 1), Direction.DOWN);
        
        // Ports and Controller
        scene.world().setBlock(util.grid().at(length / 2, 2, 0), ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH), false);
        scene.world().setBlock(util.grid().at(length / 2 + 2, 2, 0), ACCELERATOR_BLOCKS.get("accelerator_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH), false);
        scene.world().setBlock(util.grid().at(length / 2 + 4, 2, 0), ACCELERATOR_BLOCKS.get("accelerator_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH), false);
        scene.world().setBlock(util.grid().at(length / 2 - 2, 2, 0), ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH), false);
        
        scene.world().showSection(util.select().position(length / 2, 2, 0), Direction.SOUTH);
        scene.world().showSection(util.select().position(length / 2 + 2, 2, 0), Direction.SOUTH);
        scene.world().showSection(util.select().position(length / 2 - 2, 2, 0), Direction.SOUTH);
        
        scene.world().setBlock(util.grid().at(length / 2, 2, 0), ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(POWERED, true), false);
        
        scene.overlay().showText(60).text("Finalize with Casing, Glass, Ports and a Controller.");
        scene.rotateCameraY(90);
        scene.idle(55);
        scene.overlay().showText(55).pointAt(util.vector().centerOf(length, 2, 2)).text("Connect beamline from beam output port to other structure.");
        scene.idle(55);
        scene.overlay().showControls(util.vector().topOf(length / 2, 2, 0), Pointing.LEFT, 55).withItem(new ItemStack(LEVER)).rightClick();
        scene.overlay().showText(60).text("Provide redstone signal to controller block. Signal strength affects acceleration energy.");
        scene.idle(60);

    }
}
