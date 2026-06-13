package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import igentuman.nc.util.PortMode;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.block.MultiblockControllerBlock.POWERED;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.MULTITOOL;
import static igentuman.nc.util.PortMode.PORT_MODE;
import static net.minecraft.world.item.Items.LEVER;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class TargetChamberPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.TARGET_CHAMBER.getPath(), "Creating a Target Chamber");
        
        for(int x = 0; x <= 4; x++) {
            for(int y = 0; y <= 4; y++) {
                for(int z = 0; z <= 4; z++) {
                    if(x == 0 || x == 4 || y == 0 || y == 4 || z == 0 || z == 4) {
                        scene.world().setBlock(util.grid().at(x, y, z), PARTICLE_CHAMBER_BLOCKS.get("target_chamber_casing").get().defaultBlockState(), false);
                    }
                }
            }
        }
        
        scene.world().setBlock(util.grid().at(2, 2, 2), PARTICLE_CHAMBER_BLOCKS.get("target_chamber_camera").get().defaultBlockState(), false);
        
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0,0, 0, 4, 0, 4), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0,1, 4, 4, 4, 4), Direction.NORTH);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(4,1, 0, 4, 4, 3), Direction.WEST);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0,1, 0, 0, 4, 3), Direction.EAST);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1,1, 0, 3, 4, 0), Direction.SOUTH);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1,4, 1, 3, 4, 3), Direction.DOWN);
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(util.grid().at(0, 1, 0), Direction.UP))
                .text("Target Chamber size can be from 5x5x5 up to 11x11x11.");

        scene.idle(55);
        scene.world().hideSection(util.select().fromTo(1,4, 1, 3, 5, 3), Direction.UP);
        scene.world().hideSection(util.select().fromTo(1,1, 0, 3, 5, 0), Direction.NORTH);
        scene.world().hideSection(util.select().fromTo(0,1, 0, 0, 5, 3), Direction.WEST);
        scene.idle(25);
        scene.world().restoreBlocks(util.select().fromTo(0,0,0,3,4,3));
        scene.addKeyframe();
        scene.world().showSection(util.select().position(2, 2, 2), Direction.DOWN);
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().position(2, 2, 2), 55);
        scene.overlay().showText(55)
                .pointAt(util.vector().topOf(2, 2, 2))
                .text("The center of the structure must be a Target Chamber Camera.");

        scene.idle(55);
        scene.addKeyframe();

        scene.world().showSection(util.select().position(1, 2, 2), null);

        scene.world().showSection(util.select().position(3, 2, 2), null);

        scene.world().showSection(util.select().position(2, 2, 1), null);

        scene.world().showSection(util.select().position(2, 2, 3), null);

        scene.overlay().showText(50)
                .pointAt(util.vector().topOf(1, 2, 2))
                .text("Beam blocks must connect the camera to the beam ports in all 4 horizontal directions.");

        scene.idle(55);
        scene.addKeyframe();
        scene.world().showSection(util.select().position(2, 2, 4), null);
        scene.world().showSection(util.select().position(2, 2, 0), null);
        scene.world().showSection(util.select().position(4, 2, 2), null);
        scene.world().showSection(util.select().position(0, 2, 2), null);


        scene.overlay().showText(40)
                .pointAt(util.vector().topOf(0, 2, 2))
                .text("Structure needs at least 1 input beam port and 3 output beam ports.");
        
        scene.idle(45);
        scene.overlay().showControls(util.vector().topOf(-1, 2, 2), Pointing.LEFT, 55).withItem(new ItemStack(MULTITOOL.get())).rightClick();
        scene.idle(10);
        scene.overlay().showText(40).text("Use a Multitool to change the port mode.");
        scene.world().setBlock(util.grid().at(0, 2, 2), PARTICLE_CHAMBER_BLOCKS.get("target_chamber_beam_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.WEST).setValue(PORT_MODE, PortMode.Mode.INPUT), false);
        scene.world().setBlock(util.grid().at(2, 2, 4), PARTICLE_CHAMBER_BLOCKS.get("target_chamber_beam_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.SOUTH).setValue(PORT_MODE, PortMode.Mode.OUTPUT), false);

        scene.idle(45);
        scene.addKeyframe();
        scene.world().showSection(util.select().position(2, 1, 2), null);
        scene.world().showSection(util.select().position(2, 3, 2), null);
        scene.idle(20);
        scene.world().showSection(util.select().fromTo(1,1, 1, 3, 3, 3), null);

        scene.overlay().showText(40)
                .pointAt(util.vector().topOf(2, 1, 2))
                .text("Detectors must be placed around the camera to collect data.");
        
        scene.idle(45);
        scene.addKeyframe();
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0,1, 0, 0, 4, 3), Direction.EAST);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1,1, 0, 3, 4, 0), Direction.SOUTH);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1,4, 1, 3, 4, 3), Direction.DOWN);
        scene.rotateCameraY(-90);
        scene.idle(10);
        scene.world().setBlock(util.grid().at(1, 1, 4), PARTICLE_CHAMBER_BLOCKS.get("target_chamber_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.SOUTH), true);
        scene.overlay().showText(55)
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 4), Direction.SOUTH))
                .text("Add Target Chamber Ports for energy and item/fluid transport.");
        scene.idle(10);
        scene.world().setBlock(util.grid().at(3, 1, 4), PARTICLE_CHAMBER_BLOCKS.get("target_chamber_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.SOUTH), true);

        scene.idle(55);
        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(2, 3, 4), PARTICLE_CHAMBER_BLOCKS.get("target_chamber_controller").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.SOUTH), true);
        scene.overlay().showText(40)
                .pointAt(util.vector().blockSurface(util.grid().at(2, 3, 4), Direction.NORTH))
                .text("Place the Target Chamber Controller on the casing.");
        
        scene.idle(45);
        scene.world().restoreBlocks(util.select().fromTo(0,0,0,4,4,4));
        scene.world().setBlock(util.grid().at(2, 3, 4), PARTICLE_CHAMBER_BLOCKS.get("target_chamber_controller").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.SOUTH).setValue(POWERED, true), false);
        scene.overlay().showText(60).text("When the structure is valid, start it with redstone signal to controller block.");
        scene.overlay().showControls(util.vector().topOf(2, 3, 5), Pointing.LEFT, 55).withItem(new ItemStack(LEVER)).rightClick();

        scene.idle(70);
    }
}

