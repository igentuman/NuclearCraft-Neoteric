package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.block.particle.ParticleChamberBeamPortBlock;
import igentuman.nc.compat.ponder.PonderScenes;
import igentuman.nc.setup.ModEntries;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class CollisionChamberPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.COLLISION_CHAMBER.getPath(), "Creating a Collision Chamber");
        scene.configureBasePlate(0, 0, 17);
        scene.scaleSceneView(0.55f);

        BlockState casing = ModEntries.get("target_chamber_casing").block().get().defaultBlockState();
        BlockState beam = ModEntries.get("particle_beam").block().get().defaultBlockState();
        BlockState beamPort = ModEntries.get("target_chamber_beam_port").block().get().defaultBlockState();
        for (int x = 0; x < 17; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = 0; z < 5; z++) {
                    if (x == 0 || x == 16 || y == 0 || y == 4 || z == 0 || z == 4) {
                        scene.world().setBlock(util.grid().at(x, y, z), casing, false);
                    }
                }
            }
        }
        for (int x = 1; x < 16; x++) {
            scene.world().setBlock(util.grid().at(x, 2, 2),
                    x == 2 || x == 14 ? ModEntries.get("target_chamber_camera").block().get().defaultBlockState() : beam,
                    false);
        }
        scene.world().setBlock(util.grid().at(0, 2, 2), beamPort.setValue(HORIZONTAL_FACING, Direction.WEST)
                .setValue(ParticleChamberBeamPortBlock.PORT_MODE, BeamPortMode.INPUT), false);
        scene.world().setBlock(util.grid().at(16, 2, 2), beamPort.setValue(HORIZONTAL_FACING, Direction.EAST)
                .setValue(ParticleChamberBeamPortBlock.PORT_MODE, BeamPortMode.INPUT), false);
        for (int x : new int[]{2, 14}) {
            scene.world().setBlock(util.grid().at(x, 2, 1), beam, false);
            scene.world().setBlock(util.grid().at(x, 2, 3), beam, false);
            scene.world().setBlock(util.grid().at(x, 2, 0), beamPort.setValue(HORIZONTAL_FACING, Direction.NORTH)
                    .setValue(ParticleChamberBeamPortBlock.PORT_MODE, BeamPortMode.OUTPUT), false);
            scene.world().setBlock(util.grid().at(x, 2, 4), beamPort.setValue(HORIZONTAL_FACING, Direction.SOUTH)
                    .setValue(ParticleChamberBeamPortBlock.PORT_MODE, BeamPortMode.OUTPUT), false);
        }
        scene.world().setBlock(util.grid().at(8, 1, 2),
                ModEntries.get("silicon_tracker").block().get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(8, 1, 4),
                ModEntries.get("collision_chamber_controller").block().get().defaultBlockState()
                        .setValue(HORIZONTAL_FACING, Direction.SOUTH), false);
        scene.world().setBlock(util.grid().at(9, 1, 4),
                ModEntries.get("target_chamber_port").block().get().defaultBlockState()
                        .setValue(HORIZONTAL_FACING, Direction.SOUTH), false);

        scene.world().showSection(util.select().fromTo(0, 0, 0, 16, 0, 4), Direction.UP);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 16, 4, 0), Direction.SOUTH);
        scene.world().showSection(util.select().fromTo(0, 1, 4, 16, 4, 4), Direction.NORTH);
        scene.world().showSection(util.select().fromTo(0, 1, 1, 0, 4, 3), Direction.EAST);
        scene.world().showSection(util.select().fromTo(16, 1, 1, 16, 4, 3), Direction.WEST);
        scene.world().showSection(util.select().fromTo(1, 4, 1, 15, 4, 3), Direction.DOWN);
        scene.overlay().showText(70).text("Build a long chamber: 5 to 11 blocks across and 13 to 21 blocks long.");
        scene.idle(75);

        scene.addKeyframe();
        scene.world().hideSection(util.select().fromTo(1, 4, 1, 15, 4, 3), Direction.UP);
        scene.world().showSection(util.select().fromTo(1, 2, 2, 15, 2, 2), Direction.UP);
        scene.overlay().showOutline(PonderPalette.GREEN, new Object(), util.select().position(2, 2, 2), 65);
        scene.overlay().showOutline(PonderPalette.GREEN, new Object(), util.select().position(14, 2, 2), 65);
        scene.overlay().showText(65).pointAt(util.vector().topOf(8, 2, 2))
                .text("Two or more cameras sit in a Particle Beam line along the chamber.");
        scene.idle(70);

        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.RED, new Object(),
                util.select().position(0, 2, 2).add(util.select().position(16, 2, 2)), 65);
        scene.overlay().showText(65).pointAt(util.vector().centerOf(0, 2, 2))
                .text("Opposite Beam Ports feed two incoming beams into the chamber.");
        scene.idle(70);

        scene.addKeyframe();
        for (int x : new int[]{2, 14}) {
            scene.world().showSection(util.select().fromTo(x, 2, 0, x, 2, 4), Direction.UP);
        }
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(),
                util.select().position(2, 2, 0).add(util.select().position(14, 2, 4)), 70);
        scene.overlay().showText(70).pointAt(util.vector().topOf(2, 2, 0))
                .text("Each side wall needs two output Beam Ports, aligned with cameras by Particle Beam blocks.");
        scene.idle(75);

        scene.addKeyframe();
        scene.world().showSection(util.select().position(8, 1, 2), Direction.UP);
        scene.overlay().showText(60).pointAt(util.vector().topOf(8, 1, 2))
                .text("Detectors inside the chamber collect data and consume power.");
        scene.idle(65);

        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(),
                util.select().position(8, 1, 4).add(util.select().position(9, 1, 4)), 65);
        scene.overlay().showText(65).pointAt(util.vector().centerOf(8, 1, 4))
                .text("Add a Collision Chamber Controller and Particle Chamber Ports for energy and items.");
        scene.idle(70);
    }
}
