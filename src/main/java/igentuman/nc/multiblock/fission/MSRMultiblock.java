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
import static igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration.HX_BLOCKS;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class MSRMultiblock extends AbstractMultiblock {

    protected int fuelCellCount = 0;

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

        updateControllerStats();
        
        debugLog("=== MSR MULTIBLOCK VALIDATION COMPLETE ===");
    }

    @Override
    public void validateInner(boolean force) {
        if (!outerValid && !force) {
            debugLog("VALIDATION FAILED - Outer structure invalid, clearing stats");
            clearStats();
            return;
        }
        fuelCellCount = 0;
        indexInnerBlocks();
        bsCache.values().stream().filter(bs -> bs.getBlock() instanceof FissionFuelCellBlock).forEach(bs -> fuelCellCount++);
    }

    private void indexInnerBlocks() {
        BlockPos thePos = initialPos().copy();
        debugLog("height="+height+" width="+width+" depth="+depth);
        for(int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                for (int z = 1; z < depth - 1; z++) {
                    switch (getControllerDirection().ordinal()) {
                        case 3 -> thePos = initialPos().copy().east(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z);
                        case 5 -> thePos = initialPos().copy().north(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z);
                        case 2 -> thePos = initialPos().copy().west(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z);
                        case 4 -> thePos = initialPos().copy().south(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z);
                    }
                    if(!processInnerBlock(thePos)) {
                        validationResult = ValidationResult.WRONG_INNER;
                        errorBlockPos = new BlockPos(thePos);
                        return;
                    }
                }
            }
        }
        validationResult = ValidationResult.VALID;
        errorBlockPos = null;
    }

    protected boolean processInnerBlock(BlockPos toCheck) {
        if(!isValidForInner(toCheck)) {
            return false;
        }
        addIfNotExists(toCheck, allBlocks);
        attachMultiblock(toCheck);
        return true;
    }

    private void updateControllerStats() {
        if(!(beCache.getOrDefault(controllerPos.asLong(), null) instanceof MSRControllerBE controller)) {
            return;
        }
        
        // Calculate stats based on fuel cell count and chamber size
        int chamberVolume = (width - 2) * (height - 2) * (depth - 2);
        
        // Update controller
        controller.connectedPorts = connectedPorts;
        controller.fuelCellsCount = fuelCellCount;
        controller.maxHeat = chamberVolume * 1000.0;
        controller.minPebblesForCriticality = Math.max(20, chamberVolume * 5);
        controller.minSaltForCriticality = Math.max(500, chamberVolume * 100);
        
        debugLog("  FuelCells: " + fuelCellCount);
        debugLog("  ChamberVolume: " + chamberVolume);
        
        controller.markDirty();
    }

    @Override
    public void clearStats() {
        fuelCellCount = 0;

        BlockEntity be = getLevel().getBlockEntity((BlockPos) controllerPos);
        if(be instanceof MSRControllerBE controller) {
            controller.isCasingValid = false;
            controller.isInternalValid = false;
        }
        
        debugLog("MSR STATS CLEARED");
    }
}
