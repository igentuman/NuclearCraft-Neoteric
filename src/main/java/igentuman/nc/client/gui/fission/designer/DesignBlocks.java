package igentuman.nc.client.gui.fission.designer;

import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.TagUtil;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public final class DesignBlocks {

    private static Set<Block> moderators;

    private DesignBlocks() {}

    public static Set<Block> moderators() {
        if (moderators == null) {
            moderators = new HashSet<>(TagUtil.getBlocksByTagKey("nuclearcraft:moderators"));
        }
        return moderators;
    }

    public static Block block(String name) {
        ModEntry e = ModEntries.get(name);
        return e != null && e.hasBlock() ? e.block().get() : null;
    }

    public static Block fuelCell() {
        return block("fission_reactor_solid_fuel_cell");
    }

    public static Block irradiationChamber() {
        return block("fission_reactor_irradiation_chamber");
    }

    public static Block pileDriver() {
        return block("fission_reactor_pile-driver_irradiation_chamber");
    }

    public static Block casing() {
        return block("fission_reactor_casing");
    }

    public static boolean isFuelCell(Block b) {
        return b != null && b == fuelCell();
    }

    public static boolean isIrradiator(Block b) {
        return b != null && (b == irradiationChamber() || b == pileDriver());
    }

    public static boolean isModerator(Block b) {
        return b != null && moderators().contains(b);
    }
}
