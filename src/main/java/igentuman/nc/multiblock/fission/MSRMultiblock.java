package igentuman.nc.multiblock.fission;

import igentuman.nc.block.fission.FissionCasingBlock;
import igentuman.nc.block.fission.FissionFuelCellBlock;
import igentuman.nc.block.fission.entity.MSRControllerBE;
import igentuman.nc.handler.event.server.WorldEvents;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class MSRMultiblock extends AbstractMultiblock {

    protected int fuelCellCount = 0;
    protected double heatPerTick = 0;
    protected int energyPerTick = 0;
    protected double efficiency = 0;

    public MSRMultiblock(MSRControllerBE msrControllerBE) {
        super(
                getBlocksByTagKey(FissionReactorRegistration.CASING_BLOCKS.location().toString()),
                getInnerBlocks(),
                new MSRController(msrControllerBE)
        );
        id = "msr_" + msrControllerBE.getBlockPos().toShortString();
        controllerBe = msrControllerBE;
        
        // Track casing blocks for updates
        for(Block b: validOuterBlocks()) {
            if(b instanceof FissionCasingBlock) {
                continue;
            }
            if(!WorldEvents.trackingBlocks.contains(b)) {
                WorldEvents.trackingBlocks.add(b);
            }
        }
    }

    private static HashSet<Block> getInnerBlocks() {
        HashSet<Block> innerBlocks = new HashSet<>();
        innerBlocks.add(FissionReactorRegistration.FISSION_BLOCKS.get("msr_fuel_cell").get());
        return innerBlocks;
    }

    @Override
    public int maxHeight() {
        return FISSION_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int maxWidth() {
        return FISSION_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int maxDepth() {
        return FISSION_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int minHeight() {
        return FISSION_CONFIG.MIN_SIZE.get();
    }

    @Override
    public int minWidth() {
        return FISSION_CONFIG.MIN_SIZE.get();
    }

    @Override
    public int minDepth() {
        return FISSION_CONFIG.MIN_SIZE.get();
    }

    @Override
    protected Direction getControllerDirection() {
        return Direction.NORTH;
    }

    @Override
    public void validate() {
        debugLog("=== MSR MULTIBLOCK VALIDATION START ===");
        
        // Stage 1: Validate structure bounds
        debugLog("Stage 1: Validating structure bounds");
        super.validate();
        if(validationResult != ValidationResult.VALID) {
            debugLog("Stage 1 FAILED - Invalid bounds: " + validationResult);
            return;
        }
        debugLog("Stage 1 complete - Height: " + height + ", Width: " + width + ", Depth: " + depth);
        
        // Stage 2: Index fuel cells
        debugLog("Stage 2: Indexing fuel cells");
        indexFuelCells();
        if(validationResult != ValidationResult.VALID) {
            debugLog("Stage 2 FAILED - Invalid interior: " + validationResult);
            return;
        }
        debugLog("Stage 2 complete - Fuel cells found: " + fuelCellCount);
        
        // Stage 3: Update controller with calculated stats
        debugLog("Stage 3: Updating controller stats");
        updateControllerStats();
        
        debugLog("=== MSR MULTIBLOCK VALIDATION COMPLETE ===");
    }

    private void indexFuelCells() {
        fuelCellCount = 0;
        BlockPos thePos = initialPos().copy();
        
        // Iterate through interior blocks (excluding outer casing layer)
        for(int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                for (int z = 1; z < depth - 1; z++) {
                    BlockPos checkPos = new BlockPos(thePos.getX() + x, thePos.getY() + y, thePos.getZ() + z);
                    
                    BlockState state = getBlockState(checkPos);
                    Block block = state.getBlock();
                    
                    // MSR interior must be ONLY fuel cells
                    if(block instanceof FissionFuelCellBlock) {
                        fuelCellCount++;
                    } else {
                        // Invalid block in interior - MSR requires only fuel cells
                        validationResult = ValidationResult.WRONG_INNER;
                        errorBlockPos = checkPos;
                        outerValid = false;
                        debugLog("INVALID INTERIOR at " + checkPos + ": Found " + block.getName().getString() + ", expected fuel cells only");
                        return;
                    }
                }
            }
        }
    }

    private void updateControllerStats() {
        BlockEntity be = getLevel().getBlockEntity((BlockPos) controllerPos);
        if(!(be instanceof MSRControllerBE controller)) {
            return;
        }
        
        // Calculate stats based on fuel cell count and chamber size
        int chamberVolume = (width - 2) * (height - 2) * (depth - 2);
        
        // Energy and heat generation scales with fuel cell count
        double baseEnergyPerTick = fuelCellCount * 10.0;
        double baseHeatPerTick = fuelCellCount * 5.0;
        
        // Efficiency improves with larger chambers (better thermodynamics)
        efficiency = 0.7 + (chamberVolume / (double)(chamberVolume + 100)) * 0.25;
        efficiency = Math.min(efficiency, 0.95);
        
        energyPerTick = (int)(baseEnergyPerTick * efficiency);
        heatPerTick = baseHeatPerTick * efficiency;

        // Update controller
        controller.connectedPorts = connectedPorts;
        controller.energyPerTick = energyPerTick;
        controller.heatPerTick = heatPerTick;
        controller.efficiency = efficiency;
        controller.maxHeat = chamberVolume * 10.0;
        
        debugLog("  FuelCells: " + fuelCellCount);
        debugLog("  ChamberVolume: " + chamberVolume);
        debugLog("  EnergyPerTick: " + energyPerTick);
        debugLog("  HeatPerTick: " + heatPerTick);
        debugLog("  Efficiency: " + String.format("%.2f%%", efficiency * 100));
        
        controller.setChanged();
    }

    @Override
    public void clearStats() {
        fuelCellCount = 0;
        energyPerTick = 0;
        heatPerTick = 0;
        efficiency = 0;
        
        BlockEntity be = getLevel().getBlockEntity((BlockPos) controllerPos);
        if(be instanceof MSRControllerBE controller) {
            controller.isCasingValid = false;
            controller.isInternalValid = false;
        }
        
        debugLog("MSR STATS CLEARED");
    }
}