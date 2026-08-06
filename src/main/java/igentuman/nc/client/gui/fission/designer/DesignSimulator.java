package igentuman.nc.client.gui.fission.designer;

import igentuman.nc.block.fission.FissionFuelCellBlock;
import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.block.fission.IrradiationChamberBlock;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static igentuman.nc.handler.config.CommonConfig.ENERGY_GENERATION;
import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;

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

    private List<String> fuelKey;
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

    public void setFuelKey(List<String> key) {
        if (key == null ? fuelKey != null : !key.equals(fuelKey)) {
            fuelKey = key;
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
        RegistryObject<net.minecraft.world.item.Item> ro =
                igentuman.nc.setup.registration.FissionFuel.NC_FUEL.get(fuelKey);
        if (ro == null || !ro.isPresent()) {
            return;
        }
        if (ro.get() instanceof ItemFuel fuel) {
            try {
                fuel.initDefinition();
                fuelHeat = fuel.heat;
                fuelEnergy = fuel.forge_energy;
            } catch (Exception ignored) {
                // fuel definition may be unavailable client-side for some custom fuels
            }
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
            if (b instanceof FissionFuelCellBlock) {
                fuelCells.add(pos);
                addDirectConnection(pos);
            } else if (b instanceof IrradiationChamberBlock) {
                irradiators.add(pos);
            } else if (b instanceof HeatSinkBlock) {
                ResourceLocation id = ForgeRegistries.BLOCKS.getKey(b);
                if (id != null) {
                    heatSinksByKey.computeIfAbsent(id.toString(), k -> new ArrayList<>()).add(pos);
                }
            }
        }
    }

    private void indexFuelCellAttachments() {
        for (BlockPos pos : fuelCells) {
            int extra = countAdjacentFuelCells(pos);
            cellsHeatMult += (extra + 1D) * (extra + 2D) / 2D;
            cellsEnergyMult += extra + 1D;
            int mods = countFuelCellModerators(pos);
            moderatorsHeatMult += mods * (extra + 1D) * (FISSION_CONFIG.MODERATOR_HEAT_MULTIPLIER.get() / 100D);
            moderatorsEnergyMult += mods * (extra + 1D) * (FISSION_CONFIG.MODERATOR_FE_MULTIPLIER.get() / 100D);
        }
    }

    private int countAdjacentFuelCells(BlockPos pos) {
        int count = 0;
        for (Direction d : Direction.values()) {
            for (int l = 1; l < 5; l++) {
                Block b = blockAt(pos.relative(d, l));
                if (b instanceof FissionFuelCellBlock) {
                    count++;
                    break;
                }
                if (isModerator(b)) {
                    if (blockAt(pos.relative(d, l + 1)) instanceof FissionFuelCellBlock) {
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
        RegistryObject<Block> pileDriver =
                FissionReactorRegistration.FISSION_BLOCKS.get("fission_reactor_pile-driver_irradiation_chamber");
        for (BlockPos pos : irradiators) {
            boolean valid = false;
            for (Direction d : Direction.values()) {
                if (isModerator(blockAt(pos.relative(d))) && blockAt(pos.relative(d, 2)) instanceof FissionFuelCellBlock) {
                    irradiationLines++;
                    effectiveIrradiation++;
                    if (pileDriver != null && pileDriver.isPresent() && grid.cells.get(pos) == pileDriver.get()) {
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
        for (String key : FissionReactorRegistration.hsSchedule) {
            List<BlockPos> positions = heatSinksByKey.get(key);
            if (positions == null) {
                continue;
            }
            for (BlockPos pos : positions) {
                HeatSinkBlock block = (HeatSinkBlock) grid.cells.get(pos);
                if (block != null && evaluator.evaluate(block.def, pos)) {
                    validHeatSinks.add(pos);
                    addSecondConnection(pos);
                    if (block.isActive()) {
                        String coolant = block.def.name.replace("active_", "");
                        coolantPerTick.merge(coolant, FISSION_CONFIG.ACTIVE_HEATSINK_COOLANT_PER_TICK.get(), Integer::sum);
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
                heatSinkCooling += hs.heat;
            }
        }

        int irradiationHeat = irradiationLines * 15;
        heatPerTick = fuelHeat * (cellsHeatMult + moderatorsHeat()) + irradiationHeat;
        coolingPerTick = heatSinkCooling;
        netHeat = heatPerTick - coolingPerTick;

        double volume = Math.max(1, (double) grid.sizeX * grid.sizeY * grid.sizeZ);
        double multiplier = Math.max(1, ((double) Math.round(Math.log(volume) * 10) / 10) - 1);
        maxHeat = FISSION_CONFIG.HEAT_CAPACITY.get() * multiplier;

        heatMultiplier = computeHeatMultiplier();
        double collected = collectedHeatMultiplier();
        energyPerTick = fuelEnergy * (cellsEnergyMult + moderatorsFE())
                * (heatMultiplier + collected - 1)
                * FISSION_CONFIG.FE_GENERATION_MULTIPLIER.get() / 10D
                * ENERGY_GENERATION.GENERATION_MULTIPLIER.get();
        energyPerTick = Math.max(0, energyPerTick);

        steamPerTick = computeSteamPerTick();

        irradiation = effectiveIrradiation;
        meltdownTimeSeconds = netHeat > 0 ? maxHeat / netHeat / 20D : Double.POSITIVE_INFINITY;
    }

    private double moderatorsHeat() {
        return Math.max(0.1, 1D) * moderatorsHeatMult;
    }

    private double moderatorsFE() {
        return 1D * moderatorsEnergyMult;
    }

    private double computeHeatMultiplier() {
        if (heatPerTick <= 0) {
            return 0;
        }
        double h = heatPerTick;
        double c = Math.max(1, coolingPerTick);
        double m = Math.log10(h / c) / (1 + Math.exp(h / c * FISSION_CONFIG.HEAT_MULTIPLIER.get())) + 1;
        return Math.round(m * 100.0) / 100.0;
    }

    private double collectedHeatMultiplier() {
        return Math.min(FISSION_CONFIG.HEAT_MULTIPLIER_CAP.get(),
                Math.pow((maxHeat / 8) / maxHeat, 5) + 0.9999694824);
    }

    private int computeSteamPerTick() {
        if (netHeat >= 0) {
            return 0;
        }
        double heatEff = coolingPerTick * FISSION_CONFIG.BOILING_MULTIPLIER.get() / 100D * heatMultiplier;
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
        return block != null && FissionReactorRegistration.moderatorBlocks().contains(block);
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
