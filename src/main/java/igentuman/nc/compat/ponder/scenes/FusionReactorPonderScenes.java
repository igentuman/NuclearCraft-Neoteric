package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.BlockPosInstance;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.instruction.RotateSceneInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_BLOCKS;
import static net.minecraft.core.Direction.*;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.world.item.Items.LEVER;

public class FusionReactorPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.FUSION_REACTOR.getPath(), "Building a Fusion Reactor");

        scene.idle(10);
        // Center (5, 1, 5) for a 15x15 structure (3-wide ring + 1 connectors + 3x3 core area)
        int cx = 5;
        int cz = 5;
        BlockPos corePos = util.grid().at(cx, 0, cz);

        scene.world().showSection(util.select().position(corePos), Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(corePos, Direction.UP))
                .text("The Fusion Core is the central part of the reactor.");
        scene.idle(40);

        // Show 3x3x3 volume
        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.GREEN, new Object(), util.select().fromTo(cx - 1, 0, cz - 1, cx + 1, 2, cz + 1), 30);
        scene.overlay().showText(30)
                .text("It automatically occupies a 3x3x3 volume around it.");
        scene.idle(35);

        // 2. Connectors
        scene.addKeyframe();
        scene.world().showSection(util.select().position(corePos.above().north(2)), null);
        scene.world().showSection(util.select().position(corePos.above().south(2)), null);
        scene.world().showSection(util.select().position(corePos.above().east(2)), null);
        scene.world().showSection(util.select().position(corePos.above().west(2)), null);
        scene.idle(20);
        scene.overlay().showText(30)
                .text("Add one Fusion Reactor Connector in each horizontal direction.");
        scene.idle(35);
        scene.overlay().showText(30)
                .text("You can have up to 10 connectors in each horizontal direction.");
        scene.idle(35);
        scene.overlay().showText(30)
                .text("Bigger ring - more energy and heat.");
        scene.idle(35);
        // 3. Ring Chamber
        scene.addKeyframe();
        scene.overlay().showText(30)
                .text("Finally, build the Ring Chamber with a 3x3 cross-section.");
        scene.idle(20);

        BlockPosInstance pos = new BlockPosInstance(corePos.relative(UP));
        for(Direction side: List.of(NORTH, EAST, SOUTH, WEST)) {
            Direction dir = side;
            int steps = 6;
            int shift = 3;
            BlockPosInstance startPosInnerWall = null;
            BlockPosInstance startPosOuterWall = null;
            BlockPosInstance startPosBottomWall = null;
            BlockPosInstance startPosTopWall = null;
            switch (side) {
                case NORTH -> {
                    dir = EAST;
                    startPosInnerWall = new BlockPosInstance(pos.revert().relative(NORTH, shift).relative(WEST, shift));
                    startPosOuterWall = new BlockPosInstance(pos.revert().relative(NORTH, 2+shift).relative(WEST, 1+shift));
                    startPosBottomWall = new BlockPosInstance(pos.revert().relative(NORTH, 1+shift).relative(WEST, 1+shift).relative(DOWN));
                    startPosTopWall = new BlockPosInstance(pos.revert().relative(NORTH, 1+shift).relative(WEST, 1+shift).relative(UP));
                }
                case SOUTH -> {
                    dir = WEST;
                    startPosInnerWall = new BlockPosInstance(pos.revert().relative(SOUTH, shift).relative(EAST, shift));
                    startPosOuterWall = new BlockPosInstance(pos.revert().relative(SOUTH, 2+shift).relative(EAST, 1+shift));
                    startPosBottomWall = new BlockPosInstance(pos.revert().relative(SOUTH, 1+shift).relative(EAST, 1+shift).relative(DOWN));
                    startPosTopWall = new BlockPosInstance(pos.revert().relative(SOUTH, 1+shift).relative(EAST, 1+shift).relative(UP));
                }
                case WEST -> {
                    dir = SOUTH;
                    startPosInnerWall = new BlockPosInstance(pos.revert().relative(WEST, shift).relative(NORTH, shift));
                    startPosOuterWall = new BlockPosInstance(pos.revert().relative(WEST, 2+shift).relative(NORTH, 1+shift));
                    startPosBottomWall = new BlockPosInstance(pos.revert().relative(WEST, 1+shift).relative(NORTH, 1+shift).relative(DOWN));
                    startPosTopWall = new BlockPosInstance(pos.revert().relative(WEST, 1+shift).relative(NORTH, 1+shift).relative(UP));
                }
                case EAST -> {
                    dir = NORTH;
                    startPosInnerWall = new BlockPosInstance(pos.revert().relative(EAST, shift).relative(SOUTH, shift));
                    startPosOuterWall = new BlockPosInstance(pos.revert().relative(EAST, 2+shift).relative(SOUTH, 1+shift));
                    startPosBottomWall = new BlockPosInstance(pos.revert().relative(EAST, 1+shift).relative(SOUTH, 1+shift).relative(DOWN));
                    startPosTopWall = new BlockPosInstance(pos.revert().relative(EAST, 1+shift).relative(SOUTH, 1+shift).relative(UP));
                }
            }
            //inner wall
            for(int i = 0; i < steps; i++) {
                assert startPosInnerWall != null;
                scene.world().showSection(util.select().position(startPosInnerWall.revert().relative(dir, i)), null);
            }
            //outer, bottom, top walls
            for(int i = 0; i < steps+2; i++) {
                scene.world().setBlock(startPosOuterWall.revert().relative(dir, i), FUSION_BLOCKS.get("fusion_reactor_casing").get().defaultBlockState(), false);
                scene.world().showSection(util.select().position(startPosOuterWall.revert().relative(dir, i)), null);
                scene.world().showSection(util.select().position(startPosBottomWall.revert().relative(dir, i)), null);
                scene.world().setBlock(startPosBottomWall.revert().relative(dir, i), FUSION_BLOCKS.get("fusion_reactor_casing").get().defaultBlockState(), false);
                scene.world().showSection(util.select().position(startPosTopWall.revert().relative(dir, i)), null);
            }
        }

        scene.idle(10);
        scene.overlay().showText(30)
                .text("The chamber must be hollow to allow plasma to circulate.");
        scene.idle(35);

        scene.addKeyframe();
        scene.overlay().showText(30)
                .text("Fusion reactor functional blocks.");
        scene.world().restoreBlocks(util.select().fromTo(0,0,0,10, 2, 10));
        scene.idle(35);
        scene.overlay().showText(30)
                .text("Functional blocks must be placed anywhere in the corners of reactor ring.");
        scene.idle(35);
        scene.world().showSection(util.select().fromTo(0,0,0,10, 2, 10), null);
        scene.overlay().showText(50)
                .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.UP))
                .text("RF Amplifiers used to heat the plasma. You don't have enough RF amplification, reaction might not start");
        scene.idle(55);
        scene.overlay().showText(50)
                .pointAt(util.vector().blockSurface(util.grid().at(0, 2, 2), Direction.UP))
                .text("Electromagnets used to increase cross-section. Make reaction more stable");
        scene.idle(55);

        scene.addKeyframe();

        scene.overlay().showText(30)
                .text("When reactor is ready, you need to charge it, pump fuel and coolant.");
        scene.idle(35);
        scene.addInstruction(new RotateSceneInstruction(-35, -45, true));
        scene.overlay().showText(30)
                .text("Start reactor with input redstone signal to Fusion Core. Redstone signal strength directly affects RF amplification.");
        scene.overlay().showControls(util.vector().topOf(3, 3, 6), Pointing.LEFT, 30).withItem(new ItemStack(LEVER)).rightClick();
        scene.idle(35);

        scene.overlay().showText(30)
                .text("On top of this you can adjust RF amplification in reactor GUI.");
        scene.idle(35);
    }
}
