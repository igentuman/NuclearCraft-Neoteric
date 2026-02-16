package igentuman.nc.multiblock.fission;

import igentuman.nc.block.fission.FissionFuelCellBlock;
import igentuman.nc.block.fission.entity.MSRControllerBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class MSRMultiblock extends AbstractMultiblock {

    protected int fuelCellCount = 0;
    protected int heatExchangerCount = 0;
    protected final HashSet<Long> heatExchangers = new HashSet<>();

    public MSRMultiblock(MSRControllerBE msrControllerBE) {
        super(
                getBlocksByTagKey(FissionReactorRegistration.CASING_BLOCKS.location().toString()),
                getInnerBlocks(),
                new MSRController(msrControllerBE)
        );
        id = "msr_" + msrControllerBE.getBlockPos().toShortString();
        controllerBe = msrControllerBE;
    }

    private static HashSet<Block> getInnerBlocks() {
        HashSet<Block> innerBlocks = new HashSet<>();
        innerBlocks.add(FissionReactorRegistration.FISSION_BLOCKS.get("msr_fuel_cell").get());
        innerBlocks.add(FissionReactorRegistration.FISSION_BLOCKS.get("heat_exchanger").get());
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
        return FISSION_CONFIG.MIN_SIZE.get() + 2;
    }

    @Override
    public int minWidth() {
        return FISSION_CONFIG.MIN_SIZE.get() + 2;
    }

    @Override
    public int minDepth() {
        return FISSION_CONFIG.MIN_SIZE.get() + 2;
    }

    @Override
    protected MSRControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (MSRControllerBE) controllerBe;
    }
    
    @Override
    protected Direction getControllerDirection() {
        return controllerBE().getFacing();
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
        
        // Stage 2: Index fuel cells and heat exchangers
        debugLog("Stage 2: Indexing fuel cells and heat exchangers");
        indexInnerBlocks();
        if(validationResult != ValidationResult.VALID) {
            debugLog("Stage 2 FAILED - Invalid interior: " + validationResult);
            return;
        }
        debugLog("Stage 2 complete - Fuel cells found: " + fuelCellCount + ", Heat exchangers found: " + heatExchangerCount);
        
        // Stage 3: Update controller with calculated stats
        debugLog("Stage 3: Updating controller stats");
        updateControllerStats();
        
        debugLog("=== MSR MULTIBLOCK VALIDATION COMPLETE ===");
    }

    private void indexInnerBlocks() {
        fuelCellCount = 0;
        heatExchangerCount = 0;
        heatExchangers.clear();
        BlockPos thePos = initialPos().copy();
        
        Block heatExchangerBlock = FissionReactorRegistration.FISSION_BLOCKS.get("heat_exchanger").get();
        
        // Iterate through interior blocks (excluding outer casing layer)
        for(int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                for (int z = 1; z < depth - 1; z++) {
                    BlockPos checkPos = new BlockPos(thePos.getX() + x, thePos.getY() + y, thePos.getZ() + z);
                    
                    BlockState state = getBlockState(checkPos);
                    Block block = state.getBlock();
                    
                    // MSR interior can contain fuel cells and heat exchangers
                    if(block instanceof FissionFuelCellBlock) {
                        fuelCellCount++;
                    } else if(block == heatExchangerBlock) {
                        heatExchangerCount++;
                        heatExchangers.add(checkPos.asLong());
                    } else if(!state.isAir()) {
                        // Invalid block in interior
                        validationResult = ValidationResult.WRONG_INNER;
                        errorBlockPos = checkPos;
                        outerValid = false;
                        debugLog("INVALID INTERIOR at " + checkPos + ": Found " + block.getName().getString() + ", expected fuel cells or heat exchangers only");
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
        
        // Update controller
        controller.connectedPorts = connectedPorts;
        controller.fuelCellsCount = fuelCellCount;
        controller.heatExchangerCount = heatExchangerCount;
        controller.maxHeat = chamberVolume * 1000.0;
        
        debugLog("  FuelCells: " + fuelCellCount);
        debugLog("  HeatExchangers: " + heatExchangerCount);
        debugLog("  ChamberVolume: " + chamberVolume);
        
        controller.setChanged();
    }

    @Override
    public void clearStats() {
        fuelCellCount = 0;
        heatExchangerCount = 0;
        heatExchangers.clear();
        
        BlockEntity be = getLevel().getBlockEntity((BlockPos) controllerPos);
        if(be instanceof MSRControllerBE controller) {
            controller.isCasingValid = false;
            controller.isInternalValid = false;
        }
        
        debugLog("MSR STATS CLEARED");
    }
}
