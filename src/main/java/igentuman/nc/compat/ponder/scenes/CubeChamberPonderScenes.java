package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.block.particle.ParticleChamberBeamPortBlock;
import igentuman.nc.setup.ModEntries;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

final class CubeChamberPonderScenes {

    private CubeChamberPonderScenes() {
    }

    static void create(SceneBuilder scene, SceneBuildingUtil util, ResourceLocation id, String name,
                       String controllerName) {
        scene.title(id.getPath(), "Creating a " + name);
        scene.configureBasePlate(0, 0, 5);

        BlockState casing = block("target_chamber_casing");
        BlockState beam = block("particle_beam");
        BlockState port = block("target_chamber_beam_port");
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = 0; z < 5; z++) {
                    if (x == 0 || x == 4 || y == 0 || y == 4 || z == 0 || z == 4) {
                        scene.world().setBlock(util.grid().at(x, y, z), casing, false);
                    }
                }
            }
        }
        scene.world().setBlock(util.grid().at(2, 2, 2), block("target_chamber_camera"), false);
        for (int offset = 1; offset <= 3; offset += 2) {
            scene.world().setBlock(util.grid().at(offset, 2, 2), beam, false);
            scene.world().setBlock(util.grid().at(2, 2, offset), beam, false);
        }
        scene.world().setBlock(util.grid().at(0, 2, 2), port.setValue(HORIZONTAL_FACING, Direction.WEST)
                .setValue(ParticleChamberBeamPortBlock.PORT_MODE, BeamPortMode.INPUT), false);
        scene.world().setBlock(util.grid().at(4, 2, 2), port.setValue(HORIZONTAL_FACING, Direction.EAST)
                .setValue(ParticleChamberBeamPortBlock.PORT_MODE, BeamPortMode.OUTPUT), false);
        scene.world().setBlock(util.grid().at(2, 2, 0), port.setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(ParticleChamberBeamPortBlock.PORT_MODE, BeamPortMode.OUTPUT), false);
        scene.world().setBlock(util.grid().at(2, 2, 4), port.setValue(HORIZONTAL_FACING, Direction.SOUTH)
                .setValue(ParticleChamberBeamPortBlock.PORT_MODE, BeamPortMode.OUTPUT), false);
        scene.world().setBlock(util.grid().at(2, 1, 2), block("silicon_tracker"), false);
        scene.world().setBlock(util.grid().at(2, 3, 2), block("silicon_tracker"), false);
        scene.world().setBlock(util.grid().at(1, 1, 4), block("target_chamber_port")
                .setValue(HORIZONTAL_FACING, Direction.SOUTH), false);
        scene.world().setBlock(util.grid().at(2, 3, 4), block(controllerName)
                .setValue(HORIZONTAL_FACING, Direction.SOUTH), false);

        scene.world().showSection(util.select().fromTo(0, 0, 0, 4, 0, 4), Direction.UP);
        scene.world().showSection(util.select().fromTo(0, 1, 4, 4, 4, 4), Direction.NORTH);
        scene.world().showSection(util.select().fromTo(4, 1, 0, 4, 4, 3), Direction.WEST);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 0, 4, 3), Direction.EAST);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 4, 0), Direction.SOUTH);
        scene.world().showSection(util.select().fromTo(1, 4, 1, 3, 4, 3), Direction.DOWN);
        scene.overlay().showText(65).text(name + "s are odd cubes, from 5x5x5 to 11x11x11.");
        scene.idle(70);

        scene.addKeyframe();
        scene.world().hideSection(util.select().fromTo(0, 1, 0, 4, 4, 0), Direction.NORTH);
        scene.world().hideSection(util.select().fromTo(0, 1, 0, 0, 4, 3), Direction.WEST);
        scene.world().hideSection(util.select().fromTo(1, 4, 1, 3, 4, 3), Direction.UP);
        scene.idle(20);
        scene.world().showSection(util.select().position(2, 2, 2), Direction.DOWN);
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().position(2, 2, 2), 55);
        scene.overlay().showText(55).pointAt(util.vector().topOf(2, 2, 2))
                .text("Place a Particle Chamber Camera at the exact center.");
        scene.idle(60);

        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(1, 2, 2, 3, 2, 2), Direction.UP);
        scene.world().showSection(util.select().fromTo(2, 2, 1, 2, 2, 3), Direction.UP);
        scene.overlay().showText(60).pointAt(util.vector().topOf(2, 2, 1))
                .text("Particle Beam blocks connect the camera to four horizontal Beam Ports.");
        scene.idle(65);

        scene.addKeyframe();
        scene.world().showSection(util.select().position(0, 2, 2), Direction.EAST);
        scene.world().showSection(util.select().position(4, 2, 2), Direction.WEST);
        scene.world().showSection(util.select().position(2, 2, 0), Direction.SOUTH);
        scene.world().showSection(util.select().position(2, 2, 4), Direction.NORTH);
        scene.overlay().showOutline(PonderPalette.GREEN, new Object(), util.select().position(0, 2, 2), 60);
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(), util.select().position(2, 2, 4), 60);
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(0, 2, 2), Direction.WEST),
                Pointing.RIGHT, 60).withItem(new ItemStack(ModEntries.get("multitool").item().get())).rightClick();
        scene.overlay().showText(60).text("Use the Multitool to set one Beam Port to Input and the others to Output.");
        scene.idle(65);

        scene.addKeyframe();
        scene.world().showSection(util.select().position(2, 1, 2), Direction.UP);
        scene.world().showSection(util.select().position(2, 3, 2), Direction.DOWN);
        scene.overlay().showText(60).pointAt(util.vector().topOf(2, 1, 2))
                .text("Place detectors around the camera to collect particle data.");
        scene.idle(65);

        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().position(1, 1, 4), 55);
        scene.overlay().showText(55).pointAt(util.vector().centerOf(1, 1, 4))
                .text("Particle Chamber Ports carry energy, items and fluids.");
        scene.idle(60);

        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().position(2, 3, 4), 60);
        scene.overlay().showText(60).pointAt(util.vector().centerOf(2, 3, 4))
                .text("Place the " + name + " Controller in the casing and power it with redstone.");
        scene.idle(65);
    }

    private static BlockState block(String name) {
        return ModEntries.get(name).block().get().defaultBlockState();
    }
}
