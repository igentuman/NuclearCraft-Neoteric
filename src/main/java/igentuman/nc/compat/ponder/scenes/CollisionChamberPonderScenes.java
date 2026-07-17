package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.block.MultiblockControllerBlock.POWERED;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.MULTITOOL;
import static net.minecraft.world.item.Items.LEVER;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class CollisionChamberPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.COLLISION_CHAMBER.getPath(), "Creating a Collision Chamber");

        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0, 0, 0, 16, 0, 4), Direction.UP);
        scene.idle(8);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 16, 3, 0), Direction.SOUTH);
        scene.world().showSection(util.select().fromTo(0, 1, 4, 16, 3, 4), Direction.NORTH);
        scene.world().showSection(util.select().fromTo(0, 1, 1, 0, 3, 3), Direction.EAST);
        scene.world().showSection(util.select().fromTo(16, 1, 1, 16, 3, 3), Direction.WEST);
        scene.idle(10);
        scene.overlay().showText(70)
                .pointAt(util.vector().blockSurface(util.grid().at(8, 3, 2), Direction.UP))
                .text("Collision Chambers are long boxes: 5 to 11 wide and tall, and 13 to 21 deep (17 by default).");

        scene.idle(75);
        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(1, 2, 2, 15, 2, 2), Direction.DOWN);
        scene.overlay().showOutline(PonderPalette.GREEN, new Object(), util.select().fromTo(1, 2, 2, 15, 2, 2), 80);
        scene.overlay().showText(70)
                .pointAt(util.vector().topOf(8, 2, 2))
                .text("A line of Particle Chamber Cameras runs the full length of the chamber, linked by Particle Beam blocks.");

        scene.idle(75);
        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.RED, new Object(), util.select().position(0, 2, 2), 80);
        scene.overlay().showOutline(PonderPalette.RED, new Object(), util.select().position(16, 2, 2), 80);
        scene.overlay().showText(70)
                .pointAt(util.vector().blockSurface(util.grid().at(0, 2, 2), Direction.WEST))
                .text("Both ends of that axis are beam ports in INPUT mode. Two opposing beams enter here and collide.");

        scene.idle(75);
        scene.addKeyframe();
        scene.world().showSection(util.select().position(2, 2, 1), null);
        scene.world().showSection(util.select().position(2, 2, 3), null);
        scene.world().showSection(util.select().position(14, 2, 1), null);
        scene.world().showSection(util.select().position(14, 2, 3), null);
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(), util.select().position(2, 2, 0), 80);
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(), util.select().position(14, 2, 0), 80);
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(), util.select().position(2, 2, 4), 80);
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(), util.select().position(14, 2, 4), 80);
        scene.overlay().showText(70)
                .pointAt(util.vector().topOf(2, 2, 0))
                .text("Four beam ports in OUTPUT mode sit on the side walls, two per wall. Collision products leave through them.");

        scene.idle(75);
        scene.addKeyframe();
        scene.overlay().showText(60)
                .pointAt(util.vector().topOf(2, 2, 1))
                .text("Each output port reaches a camera along a straight line of Particle Beam blocks.");

        scene.idle(65);
        scene.addKeyframe();
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(0, 2, 2), Direction.WEST), Pointing.RIGHT, 50).withItem(new ItemStack(MULTITOOL.get())).rightClick();
        scene.idle(10);
        scene.overlay().showText(50).text("Use a Multitool to switch a port between input and output mode.");

        scene.idle(50);
        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(1, 1, 1, 15, 3, 3), null);
        scene.overlay().showText(60)
                .pointAt(util.vector().topOf(8, 3, 2))
                .text("Detectors fill the interior to raise efficiency at the cost of power.");

        scene.idle(65);
        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().position(8, 1, 4), 70);
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(util.grid().at(8, 1, 4), Direction.SOUTH))
                .text("Place the Collision Chamber Controller on the casing and add Particle Chamber Ports for energy and items.");

        scene.idle(65);
        scene.world().setBlock(util.grid().at(8, 1, 4), PARTICLE_CHAMBER_BLOCKS.get("collision_chamber_controller").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.SOUTH).setValue(POWERED, true), false);
        scene.overlay().showControls(util.vector().topOf(8, 1, 5), Pointing.DOWN, 50).withItem(new ItemStack(LEVER)).rightClick();
        scene.overlay().showText(60).text("Feed the controller a redstone signal to start the collision.");

        scene.idle(70);
    }
}
