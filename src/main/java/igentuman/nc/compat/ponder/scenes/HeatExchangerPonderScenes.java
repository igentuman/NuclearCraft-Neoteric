package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import igentuman.nc.setup.ModEntries;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class HeatExchangerPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.HEAT_EXCHANGER.getPath(), "Building a Heat Exchanger");

        BlockState casing = ModEntries.get("heat_exchanger_casing").block().get().defaultBlockState();

        // Reset the special blocks the structure ships with back to plain casing so they can be animated in later.
        scene.world().setBlock(util.grid().at(2, 2, 0), casing, false);
        scene.world().setBlock(util.grid().at(1, 1, 0), casing, false);
        scene.world().setBlock(util.grid().at(3, 1, 0), casing, false);
        scene.world().setBlock(util.grid().at(1, 3, 0), casing, false);
        scene.world().setBlock(util.grid().at(3, 3, 0), casing, false);
        scene.world().setBlock(util.grid().at(1, 4, 2), casing, false);
        scene.world().setBlock(util.grid().at(2, 4, 2), casing, false);
        scene.world().setBlock(util.grid().at(3, 4, 2), casing, false);
        scene.world().setBlock(util.grid().at(1, 4, 3), casing, false);
        scene.world().setBlock(util.grid().at(2, 4, 3), casing, false);
        scene.world().setBlock(util.grid().at(3, 4, 3), casing, false);
        // Drop the stray side port the structure was saved with.
        scene.world().setBlock(util.grid().at(0, 1, 1), casing, false);

        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0, 0, 0, 4, 0, 5), Direction.UP);
        scene.idle(8);
        scene.world().showSection(util.select().fromTo(0, 1, 5, 4, 4, 5), Direction.NORTH);
        scene.idle(8);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 4, 0), Direction.SOUTH);
        scene.idle(8);
        scene.world().showSection(util.select().fromTo(0, 1, 1, 0, 4, 4), Direction.EAST);
        scene.idle(8);
        scene.world().showSection(util.select().fromTo(4, 1, 1, 4, 4, 4), Direction.WEST);
        scene.idle(8);
        scene.world().showSection(util.select().fromTo(1, 4, 1, 3, 4, 4), Direction.DOWN);
        scene.idle(8);
        scene.overlay().showText(60)
                .pointAt(util.vector().blockSurface(util.grid().at(2, 0, 2), Direction.UP))
                .text("A cuboid shell of Heat Exchanger Casing, from 3x3x3 up to 11x11x11. Non-cube shapes are allowed.");
        scene.idle(65);

        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(2, 2, 0), ModEntries.get("heat_exchanger_controller").block().get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH), true);
        scene.overlay().showOutline(PonderPalette.WHITE, new Object(), util.select().fromTo(2, 2, 0, 2, 2, 0), 55);
        scene.overlay().showText(55)
                .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 0), Direction.NORTH))
                .text("Place one Heat Exchanger Controller in the shell. It owns the shared heat buffer.");
        scene.idle(60);

        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(1, 1, 0), ModEntries.get("heat_exchanger_hot_coolant_port").block().get().defaultBlockState(), true);
        scene.world().setBlock(util.grid().at(3, 1, 0), ModEntries.get("heat_exchanger_hot_coolant_port").block().get().defaultBlockState(), true);
        scene.overlay().showOutline(PonderPalette.FAST, new Object(), util.select().fromTo(1, 1, 0, 1, 1, 0), 55);
        scene.overlay().showOutline(PonderPalette.FAST, new Object(), util.select().fromTo(3, 1, 0, 3, 1, 0), 55);
        scene.overlay().showText(55)
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 0), Direction.NORTH))
                .text("Hot Coolant Ports take hot coolant in, return it cooled, and dump the heat into the buffer. Run a matched pair.");
        scene.idle(60);

        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(1, 3, 0), ModEntries.get("heat_exchanger_cold_coolant_port").block().get().defaultBlockState(), true);
        scene.world().setBlock(util.grid().at(3, 3, 0), ModEntries.get("heat_exchanger_cold_coolant_port").block().get().defaultBlockState(), true);
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(), util.select().fromTo(1, 3, 0, 1, 3, 0), 55);
        scene.overlay().showOutline(PonderPalette.BLUE, new Object(), util.select().fromTo(3, 3, 0, 3, 3, 0), 55);
        scene.overlay().showText(55)
                .pointAt(util.vector().blockSurface(util.grid().at(3, 3, 0), Direction.NORTH))
                .text("Cold Coolant Ports condense spent steam back into water, drawing the stored heat to do it. Two of them as well.");
        scene.idle(60);

        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(1, 4, 2), ModEntries.get("heat_exchanger_radiator").block().get().defaultBlockState(), true);
        scene.world().setBlock(util.grid().at(2, 4, 2), ModEntries.get("heat_exchanger_radiator").block().get().defaultBlockState(), true);
        scene.world().setBlock(util.grid().at(3, 4, 2), ModEntries.get("heat_exchanger_radiator").block().get().defaultBlockState(), true);
        scene.world().setBlock(util.grid().at(1, 4, 3), ModEntries.get("heat_exchanger_radiator").block().get().defaultBlockState(), true);
        scene.world().setBlock(util.grid().at(2, 4, 3), ModEntries.get("heat_exchanger_radiator").block().get().defaultBlockState(), true);
        scene.world().setBlock(util.grid().at(3, 4, 3), ModEntries.get("heat_exchanger_radiator").block().get().defaultBlockState(), true);
        scene.overlay().showOutline(PonderPalette.GREEN, new Object(), util.select().fromTo(1, 4, 2, 3, 4, 3), 55);
        scene.overlay().showText(55)
                .pointAt(util.vector().blockSurface(util.grid().at(2, 4, 2), Direction.UP))
                .text("Radiators go on the top face and passively vent surplus heat, so the hot side never jams.");
        scene.idle(60);

        scene.addKeyframe();
        scene.world().hideSection(util.select().fromTo(0, 0, 0, 4, 4, 0), Direction.SOUTH);
        scene.idle(15);
        for (int z = 1; z <= 4; z++) {
            for (int y = 1; y <= 3; y++) {
                for (int x = 1; x <= 3; x++) {
                    scene.world().showSection(util.select().position(x, y, z), Direction.DOWN);
                    scene.idle(1);
                }
            }
        }
        scene.overlay().showText(70)
                .pointAt(util.vector().centerOf(util.grid().at(2, 2, 2)))
                .text("Fill the interior with Heat Exchanger blocks. More blocks mean faster processing and a bigger heat buffer.");
        scene.idle(75);

        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(0, 0, 0, 4, 4, 0), Direction.NORTH);
        scene.idle(15);
        scene.world().setBlock(util.grid().at(2, 2, 0), ModEntries.get("heat_exchanger_controller").block().get().defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH), false);
        scene.overlay().showText(70)
                .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 0), Direction.NORTH))
                .text("Apply a redstone signal: the hot loop banks heat, the cold loop spends it. Both run at once on standby power.");
        scene.idle(70);
    }
}
