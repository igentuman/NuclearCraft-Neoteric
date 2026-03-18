package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.instruction.RotateSceneInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import static igentuman.nc.block.MultiblockControllerBlock.HORIZONTAL_FACING;
import static igentuman.nc.block.MultiblockControllerBlock.POWERED;
import static igentuman.nc.block.kugelblitz.BlackHoleBlock.ACTIVE;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;

public class KugelblitzChamberPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.KUGELBLITZ_CHAMBER.getPath(), "Creating a Kugelblitz Chamber");
        
        // Chamber is 11x11x11 in center of 21x21x21
        // Structure bounds: 0,0,0 to 20,20,20
        // Chamber bounds: 5,5,5 to 15,15,15
        scene.scaleSceneView(0.5f);
        // Show walls
        scene.world().showSection(util.select().fromTo(5, 5, 5, 15, 15, 15), Direction.DOWN);
        scene.idle(10);
        
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(util.grid().at(10, 5, 10), Direction.UP))
                .text("Kugelblitz Chamber size is 11x11x11.");
        
        scene.idle(35);
        scene.rotateCameraY(90);
        scene.idle(20);
        
        scene.overlay().showText(60)
                .text("All 6 walls must be perfectly symmetric.");
        
        scene.idle(40);
        scene.addKeyframe();
        
        // Terminal: [14, 7, 10]
        BlockPos terminalPos = util.grid().at(14, 7, 10);
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().position(terminalPos), 60);
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(terminalPos, Direction.WEST))
                .text("The Kugelblitz Chamber Terminal is the main control block.");
        
        scene.idle(65);
        scene.addKeyframe();
        
        // Ports: [14, 7, 8] and [14, 7, 12]
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().position(14, 7, 8).add(util.select().position(14, 7, 12)), 60);
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(util.grid().at(14, 7, 8), Direction.WEST))
                .text("Chamber Ports are used for energy and item transport. Redstone input/output and computers.");
        
        scene.idle(65);
        scene.addKeyframe();
        
        // Photon Concentrators: [10, 5, 10], [5, 10, 10], [10, 10, 5], [10, 10, 15], [15, 10, 10], [10, 15, 10]
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(util.grid().at(15, 10, 10), Direction.WEST))
                .text("Photon Concentrators must be placed at the center of all 6 walls.");
        
        scene.idle(65);
        scene.addKeyframe();
        
        // Quantum Flux Regulator
        BlockPos qfrPos = util.grid().at(8, 15, 8);
        scene.overlay().showOutline(PonderPalette.GREEN, new Object(), util.select().position(qfrPos), 60);
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(qfrPos, Direction.UP))
                .text("Quantum Flux Regulators affect the Forge Energy output rate.");
        
        scene.idle(65);
        scene.addKeyframe();
        
        // Event Horizon Stabilizer
        BlockPos ehsPos = util.grid().at(8, 5, 10);
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(), util.select().position(ehsPos), 60);
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(ehsPos, Direction.UP))
                .text("Event Horizon Stabilizers help maintain black hole stability.");
        
        scene.idle(65);
        scene.addKeyframe();
        
        // Quantum Transformer
        BlockPos qtPos = util.grid().at(8, 15, 9);
        scene.overlay().showOutline(PonderPalette.RED, new Object(), util.select().position(qtPos), 60);
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(qtPos, Direction.UP))
                .text("Quantum Transformers improve the efficiency of transformation processes.");
        
        scene.idle(65);
        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(0, 0, 0, 20, 20, 20), null);

        // [10, 0, 10], [0, 10, 10], [10, 10, 0], [10, 10, 20], [20, 10, 10], [10, 20, 10]
        scene.overlay().showText(40)
                .pointAt(util.vector().blockSurface(util.grid().at(20, 10, 10), Direction.WEST))
                .text("Finally, all 6 Excited Photon Lasers (EXPL) must be burst at the same time.");

        scene.idle(35);
        scene.overlay().showText(30)
                .text("They all need to be fully charged and then activated with redstone or in their GUI.");
        scene.addInstruction(new RotateSceneInstruction(35, 15, true));
        scene.idle(35);
        // Activate terminal state to show success
        scene.world().setBlock(terminalPos, KUGELBLITZ_BLOCKS.get("chamber_terminal").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.EAST).setValue(POWERED, true), false);
        scene.overlay().showText(60)
                .text("When all 6 lasers burst simultaneously, this will create a Blackhole inside the chamber.");
        scene.world().setBlock(new BlockPos(10, 10, 10), KUGELBLITZ_BLOCKS.get("black_hole").get().defaultBlockState().setValue(ACTIVE, true), false);
        scene.idle(70);
    }
}
