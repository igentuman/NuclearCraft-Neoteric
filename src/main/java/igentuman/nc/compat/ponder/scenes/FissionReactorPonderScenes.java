package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.block.MultiblockControllerBlock.POWERED;
import static igentuman.nc.block.fission.FissionPortBlock.HORIZONTAL_FACING;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.setup.registration.NCProcessors.PROCESSORS;
import static net.minecraft.world.item.Items.LEVER;

public class FissionReactorPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.FISSION_REACTOR.getPath(), "Creating a Fission Reactor");
        scene.world().setBlock(util.grid().at(2, 4, 0), FISSION_BLOCKS.get("fission_reactor_casing").get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(0, 1, 1), FISSION_BLOCKS.get("fission_reactor_glass").get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(4, 1, 1), FISSION_BLOCKS.get("fission_reactor_glass").get().defaultBlockState(), false);

        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0,0, 0, 5, 0, 5), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0,1, 4, 5, 5, 5), Direction.NORTH);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(4,1, 0, 5, 5, 3), Direction.WEST);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0,1, 0, 0, 5, 3), Direction.EAST);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1,1, 0, 3, 5, 0), Direction.SOUTH);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1,4, 1, 3, 5, 3), Direction.DOWN);
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 1), Direction.UP))
                .text("Walls are mainly made of Fission Reactor Casing or Reactor Glass.");

        scene.idle(55);
        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().fromTo(2, 1, 0, 2, 1, 0), 55);
        scene.overlay().showText(55)
                .pointAt(util.vector().blockSurface(util.grid().at(1, 2, -2), Direction.UP))
                .text("Place the Fission Reactor Controller anywhere you like to form the structure.");

        scene.idle(55);
        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(0, 1, 1), FISSION_BLOCKS.get("fission_reactor_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.EAST), true);

        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().fromTo(0, 1, 1, 0, 1, 1), 55);
        scene.overlay().showText(50)
                .pointAt(util.vector().blockSurface(util.grid().at(1, -1, 3), Direction.UP))
                .text("The Reactor Port is a universal block allowing you to load/unload fuel and liquids, read or send redstone signals, and attach computers.");
        scene.idle(50);
        scene.world().setBlock(util.grid().at(4, 1, 1), FISSION_BLOCKS.get("fission_reactor_port").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.WEST), true);
        scene.overlay().showText(55)
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 1), Direction.UP))
                .text("Use as many ports as you like.");
        scene.idle(5);
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().fromTo(4, 1, 1, 4, 1, 1), 15);
        scene.idle(55);
        scene.overlay().showText(55).text("Start the reactor with a redstone signal to the controller or port (make sure to select redstone mode in the port GUI).");
        scene.overlay().showControls(util.vector().topOf(2, 1, 0), Pointing.LEFT, 55).withItem(new ItemStack(LEVER)).rightClick();

        scene.idle(55);
        scene.addKeyframe();
        scene.world().hideSection(util.select().fromTo(1,4, 1, 3, 5, 3), Direction.UP);
        scene.world().hideSection(util.select().fromTo(1,1, 0, 3, 5, 0), Direction.NORTH);
        scene.world().hideSection(util.select().fromTo(0,1, 0, 0, 5, 3), Direction.WEST);
        scene.idle(15);
        scene.overlay().showText(40).text("There are no strict requirements for how internal reactor blocks must be placed.");
        scene.idle(40);
        scene.world().showSection(util.select().position(2,1, 2), Direction.DOWN);
        scene.overlay().showText(55).pointAt(util.vector().topOf(2,1, 2)).text("The Fuel Cell block is used for energy and heat generation.");
        scene.idle(55);
        scene.overlay().showText(55).text("Place as many fuel cells as you like anywhere inside the reactor.");
        scene.world().showSection(util.select().position(1,1, 1), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(3,1, 3), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(3,1, 1), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(1,1, 3), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(50).text("The resulting energy and heat generation is multiplied by the number of fuel cells.");
        scene.idle(50);
        scene.overlay().showText(50).text("It affects the fuel depletion speed at the same rate.");

        scene.idle(50);
        scene.addKeyframe();
        scene.world().showSection(util.select().position(2,2, 2), Direction.DOWN);
        scene.overlay().showText(50).pointAt(util.vector().topOf(2,2, 2)).text("Another way to get more energy and heat is to attach moderator blocks to fuel cells.");
        scene.idle(50);
        scene.overlay().showText(50).text("Each moderator block face connected to a fuel cell increases FE generation by 17% and the heat rate by 33%.");
        scene.idle(20);
        scene.world().showSection(util.select().position(2,1, 1), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(3,1, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(2,1, 3), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(1,1, 2), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(50).pointAt(util.vector().topOf(2,1, 1)).text("Moderators between two fuel cells give an additional bonus.");
        scene.idle(50);
        scene.addKeyframe();
        scene.overlay().showText(50).text("The reactor will meltdown if you don't use heatsinks.");
        scene.idle(25);
        scene.world().showSection(util.select().fromTo(1,2, 1, 3, 2, 3), Direction.DOWN);
        scene.idle(25);
        scene.overlay().showText(50).text("Each heatsink has specific placement rules to be active.");
        scene.idle(50);
        scene.overlay().showText(50).text("You are free to design your reactor as you like. Just make sure you place heatsinks according to their placement rules.");
        scene.idle(55);
        scene.world().showSection(util.select().fromTo(1,3, 1, 3, 3, 3), Direction.DOWN);

        scene.idle(55);
        scene.addKeyframe();
        scene.overlay().showText(55).text("Fission Reactor irradiation feature.");
        scene.world().hideSection(util.select().fromTo(1,1, 1, 3, 3, 3), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(2,1, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(2,2, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(2,3, 2), Direction.DOWN);
        scene.overlay().showText(55).pointAt(util.vector().topOf(2,3, 2)).text("An irradiation line is a set of three blocks in a row: Fuel Cell -> Moderator -> Irradiation Chamber.");
        scene.idle(55);
        scene.overlay().showText(55).text("Up to six irradiation lines for each Irradiation Chamber block.");
        scene.idle(15);
        scene.world().showSection(util.select().fromTo(1,1, 0, 3, 5, 0), Direction.SOUTH);
        scene.idle(15);
        scene.world().setBlock(util.grid().at(2, 4, 0), PROCESSORS.get("irradiator").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH), true);
        scene.overlay().showText(55).pointAt(util.vector().topOf(2, 4, 0)).text("Place the Irradiator anywhere in the reactor wall.");
        scene.idle(55);
        scene.overlay().showText(55).text("When the reactor is up and running, the Irradiator will use all irradiation lines to produce recipes.");
        scene.world().showSection(util.select().fromTo(0, 0, 0, 4, 4, 4), null);
        scene.world().setBlock(util.grid().at(2, 4, 0), PROCESSORS.get("irradiator").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(POWERED, true), false);
        scene.world().setBlock(util.grid().at(2, 1, 0), FISSION_BLOCKS.get("fission_reactor_controller").get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(POWERED, true), false);

        scene.idle(55);
        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(2, 3, 2), FISSION_BLOCKS.get("fission_reactor_pile-driver_irradiation_chamber").get().defaultBlockState(), false);
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().fromTo(2, 3, 2, 2, 3, 2), 55);
        scene.overlay().showText(55).pointAt(util.vector().topOf(2, 3, 2)).text("Swap in a Pile-Driver Irradiation Chamber for 5x irradiation speed.");
    }
}
