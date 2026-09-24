package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import igentuman.nc.setup.ModEntries;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class RingAcceleratorPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.RING_ACCELERATOR.getPath(), "Creating a Synchrotron Accelerator");
        scene.configureBasePlate(0, 0, 20);
        scene.scaleSceneView(0.45f);

        scene.world().showSection(util.select().fromTo(0, 0, 0, 19, 0, 19), Direction.UP);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 19, 4, 19), Direction.DOWN);
        scene.overlay().showText(70).text("Build a square ring of Accelerator Casing, five blocks wide and high.");
        scene.idle(75);

        scene.addKeyframe();
        scene.world().hideSection(util.select().fromTo(0, 4, 0, 19, 4, 19), Direction.UP);
        scene.idle(15);
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(),
                util.select().fromTo(2, 2, 2, 17, 2, 2).add(util.select().fromTo(2, 2, 17, 17, 2, 17))
                        .add(util.select().fromTo(2, 2, 2, 2, 2, 17))
                        .add(util.select().fromTo(17, 2, 2, 17, 2, 17)), 75);
        scene.overlay().showText(75).pointAt(util.vector().centerOf(2, 2, 2))
                .text("A continuous Particle Beam loop runs through the center of the ring walls.");
        scene.idle(80);

        scene.addKeyframe();
        for (int x : new int[]{2, 17}) {
            for (int z : new int[]{2, 17}) {
                scene.overlay().showOutline(PonderPalette.GREEN, new Object(),
                        util.select().fromTo(x - 1, 1, z - 1, x + 1, 3, z + 1), 70);
            }
        }
        scene.overlay().showText(70).pointAt(util.vector().centerOf(2, 2, 2))
                .text("Each corner needs a dipole: matching Electromagnets above and below the beam, surrounded by yokes.");
        scene.idle(75);

        scene.addKeyframe();
        for (int x : new int[]{5, 14}) {
            for (int z : new int[]{5, 14}) {
                scene.overlay().showOutline(PonderPalette.FAST, new Object(),
                        util.select().fromTo(x, 1, z, x, 3, z), 65);
            }
        }
        scene.overlay().showText(65).text("Coolers at the inside corners can remove heat from the ring.");
        scene.idle(70);

        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(0, 4, 0, 19, 4, 19), Direction.DOWN);
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().position(19, 1, 7), 65);
        scene.overlay().showText(65).pointAt(util.vector().centerOf(19, 1, 7))
                .text("Put the Ring Accelerator Controller on the outside wall. Add Accelerator Ports for services.");
        scene.idle(70);

        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(),
                util.select().position(19, 2, 2).add(util.select().position(19, 2, 17)), 65);
        scene.overlay().showText(65).text("Use at least two Beam Ports: one supplies particles and the others send them onward.");
        scene.idle(70);

        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(19, 1, 7),
                ModEntries.get("ring_accelerator_controller").block().get().defaultBlockState()
                        .setValue(HORIZONTAL_FACING, Direction.EAST), false);
        scene.overlay().showText(70).text("Supply an existing high-energy beam, then use redstone strength to set output energy.");
        scene.idle(75);
    }
}
