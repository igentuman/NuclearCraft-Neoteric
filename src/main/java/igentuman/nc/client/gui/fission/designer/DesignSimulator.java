package igentuman.nc.client.gui.fission.designer;

import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DesignSimulator {

    public double heatPerTick;
    public double coolingPerTick;
    public double netHeat;
    public double maxHeat;
    public double heatMultiplier;
    public double energyPerTick;
    public int steamPerTick;
    public int irradiation;
    public double meltdownTimeSeconds;
    public final HashMap<String, Integer> coolantPerTick = new HashMap<>();
    public final Set<BlockPos> invalidCells = new HashSet<>();

    private String fuelKey;
    private String fuelVariant = "";
    private double fuelHeat;
    private double fuelEnergy;

    private final Set<BlockPos> fuelCells = new HashSet<>();
    private final Set<BlockPos> moderators = new HashSet<>();
    private final Set<BlockPos> irradiators = new HashSet<>();
    private final Set<BlockPos> validHeatSinks = new HashSet<>();
    private final Set<BlockPos> directFuelCellConnection = new HashSet<>();
    private final Set<BlockPos> secondFuelCellConnection = new HashSet<>();
    private final HashMap<String, List<BlockPos>> heatSinksByKey = new HashMap<>();

    private double cellsHeatMult;
    private double cellsEnergyMult;
    private double moderatorsHeatMult;
    private double moderatorsEnergyMult;
    private int irradiationLines;
    private int effectiveIrradiation;

    private final DesignGrid grid;
    private final HeatsinkRuleEvaluator evaluator;
    private boolean dirty = true;

    public DesignSimulator(DesignGrid grid) {
        this.grid = grid;
        this.evaluator = new HeatsinkRuleEvaluator(
                grid, validHeatSinks, moderators, directFuelCellConnection, secondFuelCellConnection);
    }

    public void markDirty() {
        dirty = true;
    }

    public void setFuel(String key, String variant) {
        String v = variant == null ? "" : variant;
        boolean changed = (key == null ? fuelKey != null : !key.equals(fuelKey)) || !v.equals(fuelVariant);
        if (changed) {
            fuelKey = key;
            fuelVariant = v;
            resolveFuel();
            dirty = true;
        }
    }

    private void resolveFuel() {
        fuelHeat = 0;
        fuelEnergy = 0;
        if (fuelKey == null) {
            return;
        }
        FissionFuelEntry entry = ModEntries.FISSION_FUEL.get(fuelKey);
        if (entry == null) {
            return;
        }
        FuelDef def = entry.variantDef(fuelVariant);
        if (def != null) {
            fuelHeat = def.heat;
            fuelEnergy = def.forgeEnergy;
        }
    }

    public void simulateIfDirty() {
        if (dirty) {
            simulate();
            dirty = false;
        }
    }

    public void simulate() {
        reset();
        indexInnerBlocks();
        indexFuelCellAttachments();
        indexIrradiators();
        indexHeatSinks();
        computeStats();
        publishInvalidCells();
    }

    private void reset() {
        fuelCells.clear();
        moderators.clear();
        irradiators.clear();
        validHeatSinks.clear();
        directFuelCellConnection.clear();
        secondFuelCellConnection.clear();
        heatSinksByKey.clear();
        invalidCells.clear();
        cellsHeatMult = 0;
        cellsEnergyMult = 0;
        moderatorsHeatMult = 0;
        moderatorsEnergyMult = 0;
        irradiationLines = 0;
        effectiveIrradiation = 0;
    }

    private void indexInnerBlocks() {
        for (var entry : grid.cells.entrySet()) {
            BlockPos pos = entry.getKey();
            Block b = entry.getValue();
            if (DesignBlocks.isFuelCell(b)) {
                fuelCells.add(pos);
                addDirectConnection(pos);
            } else if (DesignBlocks.isIrradiator(b)) {
                irradiators.add(pos);
            } else if (b instanceof HeatSinkBlock hs) {
                heatSinksByKey.computeIfAbsent(hs.getDef().name, k -> new ArrayList<>()).add(pos);
            }
        }
    }

    private void indexFuelCellAttachments() {
        for (BlockPos pos : fuelCells) {
            int extra = countAdjacentFuelCells(pos);
            cellsHeatMult += (extra + 1D) * (extra + 2D) / 2D;
            cellsEnergyMult += extra + 1D;
            int mods = countFuelCellModerators(pos);
            moderatorsHeatMult += mods * (extra + 1D) * (Multiblocks.fissionModeratorHeatMultiplier / 100D);
            moderatorsEnergyMult += mods * (extra + 1D) * (Multiblocks.fissionModeratorFeMultiplier / 100D);
        }
    }

    private int countAdjacentFuelCells(BlockPos pos) {
        int count = 0;
        for (Direction d : Direction.values()) {
            for (int l = 1; l < 5; l++) {
                Block b = blockAt(pos.relative(d, l));
                if (DesignBlocks.isFuelCell(b)) {
                    count++;
                    break;
                }
                if (isModerator(b)) {
                    if (DesignBlocks.isFuelCell(blockAt(pos.relative(d, l + 1)))) {
                        count++;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return count;
    }

    private int countFuelCellModerators(BlockPos fuelCellPos) {
        int count = 0;
        for (Direction d : Direction.values()) {
            BlockPos toCheck = fuelCellPos.relative(d);
            if (isModerator(blockAt(toCheck))) {
                moderators.add(toCheck);
                addDirectConnection(toCheck);
                count++;
            }
        }
        return count;
    }

    private void indexIrradiators() {
        Block pileDriver = DesignBlocks.pileDriver();
        for (BlockPos pos : irradiators) {
            boolean valid = false;
            for (Direction d : Direction.values()) {
                if (isModerator(blockAt(pos.relative(d))) && DesignBlocks.isFuelCell(blockAt(pos.relative(d, 2)))) {
                    irradiationLines++;
                    effectiveIrradiation++;
                    if (pileDriver != null && grid.cells.get(pos) == pileDriver) {
                        effectiveIrradiation += 4;
                    }
                    valid = true;
                }
            }
            if (!valid) {
                invalidCells.add(pos);
            }
        }
    }

    private void indexHeatSinks() {
        coolantPerTick.clear();
        for (String key : ModEntries.HS_SCHEDULE) {
            List<BlockPos> positions = heatSinksByKey.get(key);
            if (positions == null) {
                continue;
            }
            for (BlockPos pos : positions) {
                Block b = grid.cells.get(pos);
                if (b instanceof HeatSinkBlock block && evaluator.evaluate(block.getDef(), pos)) {
                    validHeatSinks.add(pos);
                    addSecondConnection(pos);
                    if (block.getDef().isActive()) {
                        String coolant = block.getDef().name.replace("active_", "");
                        coolantPerTick.merge(coolant, Multiblocks.fissionActiveCoolantPerTick, Integer::sum);
                    }
                } else {
                    invalidCells.add(pos);
                }
            }
        }
        for (var e : heatSinksByKey.entrySet()) {
            for (BlockPos pos : e.getValue()) {
                if (!validHeatSinks.contains(pos)) {
                    invalidCells.add(pos);
                }
            }
        }
    }

    private void computeStats() {
        double heatSinkCooling = 0;
        for (BlockPos pos : validHeatSinks) {
            Block b = grid.cells.get(pos);
            if (b instanceof HeatSinkBlock hs) {
                heatSinkCooling += hs.getDef().heat;
            }
        }

        int irradiationHeat = irradiationLines * 15;
        heatPerTick = fuelHeat * (cellsHeatMult + moderatorsHeat()) + irradiationHeat;
        coolingPerTick = heatSinkCooling;
        netHeat = heatPerTick - coolingPerTick;

        double volume = Math.max(1, (double) grid.sizeX * grid.sizeY * grid.sizeZ);
        double multiplier = Math.max(1, ((double) Math.round(Math.log(volume) * 10) / 10) - 1);
        maxHeat = Multiblocks.fissionHeatCapacity * multiplier;

        heatMultiplier = computeHeatMultiplier();
        double collected = collectedHeatMultiplier();
        energyPerTick = fuelEnergy * (cellsEnergyMult + moderatorsFE())
                * (heatMultiplier + collected - 1)
                * Multiblocks.fissionFeGenerationMultiplier / 10D;
        energyPerTick = Math.max(0, energyPerTick);

        steamPerTick = computeSteamPerTick();

        irradiation = effectiveIrradiation;
        meltdownTimeSeconds = netHeat > 0 ? maxHeat / netHeat / 20D : Double.POSITIVE_INFINITY;
    }

    private double moderatorsHeat() {
        return moderatorsHeatMult;
    }

    private double moderatorsFE() {
        return moderatorsEnergyMult;
    }

    private double computeHeatMultiplier() {
        if (heatPerTick <= 0) {
            return 0;
        }
        double h = heatPerTick;
        double c = Math.max(1, coolingPerTick);
        double m = Math.log10(h / c) / (1 + Math.exp(h / c * Multiblocks.fissionHeatMultiplier)) + 1;
        return Math.round(m * 100.0) / 100.0;
    }

    private double collectedHeatMultiplier() {
        return Math.min(Multiblocks.fissionHeatMultiplierCap,
                Math.pow((maxHeat / 8) / maxHeat, 5) + 0.9999694824);
    }

    private int computeSteamPerTick() {
        if (netHeat >= 0) {
            return 0;
        }
        double heatEff = coolingPerTick * Multiblocks.fissionBoilingMult / 100D * heatMultiplier;
        return (int) Math.max(0, heatEff);
    }

    private void publishInvalidCells() {
        grid.invalidCells.clear();
        grid.invalidCells.addAll(invalidCells);
    }

    private Block blockAt(BlockPos pos) {
        if (!grid.inBounds(pos.getX(), pos.getY(), pos.getZ())) {
            return null;
        }
        return grid.cells.get(pos);
    }

    private boolean isModerator(Block block) {
        return DesignBlocks.isModerator(block);
    }

    private void addDirectConnection(BlockPos pos) {
        directFuelCellConnection.add(pos);
        for (Direction d : Direction.values()) {
            directFuelCellConnection.add(pos.relative(d));
        }
    }

    private void addSecondConnection(BlockPos pos) {
        secondFuelCellConnection.add(pos);
        for (Direction d : Direction.values()) {
            secondFuelCellConnection.add(pos.relative(d));
        }
    }
}
