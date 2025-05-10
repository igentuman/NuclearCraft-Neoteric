package igentuman.nc.multiblock.fission;

import igentuman.nc.block.entity.fission.*;
import igentuman.nc.block.fission.FissionCasingBlock;
import igentuman.nc.block.fission.FissionFuelCellBlock;
import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.block.fission.IrradiationChamberBlock;
import igentuman.nc.handler.event.server.WorldEvents;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.*;

import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class FissionReactorMultiblock extends AbstractNCMultiblock {

    private int irradiationConnections = 0;
    private final List<Block> validModerators;
    public final HashMap<BlockPos, HeatSinkBlock> validHeatSinks = new HashMap<>();
    private final List<BlockPos> moderators = new ArrayList<>();
    private final List<BlockPos> irradiators = new ArrayList<>();
    public final List<BlockPos> heatSinks = new ArrayList<>();
    public final List<BlockPos> fuelCells = new ArrayList<>();
    private double heatSinkCooling = 0;
    public double activeCooling = 0;
    private FissionControllerBE controllerBe;
    private final List<BlockPos> directFuelCellConnectionPos = new ArrayList<>();
    private final List<BlockPos> secondFuelCellConnectionPos = new ArrayList<>();
    public final HashMap<String, Integer> coolantPerTick = new HashMap<>();
    private final List<BlockPos> delayedValidation = new ArrayList<>();
    private boolean delayedValidationFlag = false;

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
    public int minWidth() {return FISSION_CONFIG.MIN_SIZE.get(); }

    @Override
    public int minDepth() { return FISSION_CONFIG.MIN_SIZE.get(); }

    public FissionReactorMultiblock(FissionControllerBE fissionControllerBE) {
        super(
                getBlocksByTagKey(FissionBlocks.CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(FissionBlocks.INNER_REACTOR_BLOCKS.location().toString()),
                new FissionReactorController(fissionControllerBE)
        );
        id = "fission_reactor_"+fissionControllerBE.getBlockPos().toShortString();
        validModerators = getBlocksByTagKey(FissionBlocks.MODERATORS_BLOCKS.location().toString());
        for(Block b: validModerators) {
            if(!WorldEvents.trackingBlocks.contains(b)) {
                WorldEvents.trackingBlocks.add(b);
            }
        }
        for(Block b: validOuterBlocks()) {
            if(b instanceof FissionCasingBlock) {
                continue;
            }
            if(!WorldEvents.trackingBlocks.contains(b)) {
                WorldEvents.trackingBlocks.add(b);
            }
        }
        controllerBe = fissionControllerBE;
        MultiblockHandler.instance.addMultiblock(this);
    }

    public Map<BlockPos, HeatSinkBlock> validHeatSinks() {
        if(validHeatSinks.isEmpty()) {
            for(BlockPos hpos: heatSinks) {
                Block block = getBlockState(hpos).getBlock();
                if(block instanceof HeatSinkBlock hs) {
                    if(hs.isValid(getLevel(), hpos)) {
                        validHeatSinks.put(hpos, hs);
                        if(hs.isActive()) {
                            addActiveCoolant(hs.def.name.replace("active_", ""));
                        }
                    }
                }
            }
        }
        controllerBE().heatSinksCount = validHeatSinks.size();
        return validHeatSinks;
    }

    private void addActiveCoolant(String name) {
        if(!coolantPerTick.containsKey(name)) {
            coolantPerTick.put(name, 0);
        }
        coolantPerTick.replace(name, coolantPerTick.get(name)+FISSION_CONFIG.ACTIVE_HEATSINK_COOLANT_PER_TICK.get());
    }

    @Override
    public void tick() {
        super.tick();
        tickActiveHeatSinks();
    }

    private void tickActiveHeatSinks() {
        activeCooling = 0;
        for(String coolant: coolantPerTick.keySet()) {
            int amount = coolantPerTick.get(coolant);
            if(amount == 0) {
                continue;
            }
            if(!controllerBE().hasEnoughCoolant(coolant, amount)) {
                activeCooling -= getCoolingByCoolant(coolant, amount);
                continue;
            }
            if (controllerBE().heat > 0 || controllerBE().isProcessing()) {
                controllerBE().drainCoolant(coolant, amount);
            }
        }
        if (controllerBE().activeCooling != activeCooling) {
            controllerBE().activeCooling = activeCooling;
            controllerBE().setChanged();
        }
    }

    private double getCoolingByCoolant(String coolant, int amount) {
        if(!FissionBlocks.heatsinks.containsKey("active_"+coolant)) {
            return 0;
        }
        int mbPerTick = FISSION_CONFIG.ACTIVE_HEATSINK_COOLANT_PER_TICK.get();
        FissionBlocks.heatsinks.get("active_"+coolant);
        return ((double)amount /(double)mbPerTick)*FissionBlocks.heatsinks.get("active_"+coolant).heat;
    }

    public boolean isModerator(BlockPos pos, Level level) {
        return  validModerators.contains(level.getBlockState(pos).getBlock());
    }

    public boolean isModerator(BlockPos pos) {
        return  validModerators.contains(getBlockState(pos).getBlock());
    }

    public boolean isIrradiator(BlockPos pos) {
        return  getBlockState(pos).getBlock() instanceof IrradiationChamberBlock;
    }

    protected boolean isHeatSink(BlockPos pos) {
        return getBlockState(pos).getBlock() instanceof HeatSinkBlock;
    }

    protected boolean isFuelCell(BlockPos pos) {
        return getBlockState(pos).getBlock() instanceof FissionFuelCellBlock;
    }

    private void addIfNotExists(BlockPos pos, List<BlockPos> list) {
        if(!list.contains(pos)) {
            list.add(pos);
        }
    }

    private boolean isAttachedToFuelCell(BlockPos toCheck) {
        if (directFuelCellConnectionPos.contains(toCheck)) {
            return true;
        }
        if (secondFuelCellConnectionPos.contains(toCheck)) {
            return true;
        }

        for(Direction d : Direction.values()) {
            if(isFuelCell(toCheck.relative(d))) {
                addDirectFuelCellConnection((toCheck.relative(d)));
                return true;
            }
            if (directFuelCellConnectionPos.contains(toCheck)) {
                return true;
            }
            if (secondFuelCellConnectionPos.contains(toCheck)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validateInner()
    {
        if(!outerValid) {
            invalidateStats();
            return;
        }
        resolveDimensions();
        collectFuelCells();
        for(int y = 1; y < height-1; y++) {
            for(int x = 1; x < width-1; x++) {
                for (int z = 1; z < depth-1; z++) {
                    NCBlockPos toCheck = new NCBlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z));
                    if (!isValidForInner(toCheck)) {
                        validationResult = ValidationResult.WRONG_INNER;
                        controller().setErroredBlock(new BlockPos(toCheck));
                        return;
                    }
                    processInnerBlock(new BlockPos(toCheck));
                }
            }
        }
        delayedValidationFlag = true;
        for(BlockPos pos: delayedValidation) {
            processInnerBlock(pos);
        }

        validationResult =  ValidationResult.VALID;
        heatSinkCooling = countCooling(true);
        controllerBE().moderatorsCount = moderators.size();
        controllerBE().irradiationConnections = irradiationConnections;
    }

    private void collectFuelCells() {
        for(int y = 1; y < height-1; y++) {
            for(int x = 1; x < width-1; x++) {
                for (int z = 1; z < depth-1; z++) {
                    NCBlockPos toCheck = new NCBlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z));
                    if (isFuelCell(toCheck)) {
                        addDirectFuelCellConnection(new BlockPos(toCheck));
                        addIfNotExists(new BlockPos(toCheck), fuelCells);
                        int moderatorAttachments = countAttachedModeratorsToFuelCell(new BlockPos(toCheck));
                        controllerBE().fuelCellMultiplier += countAdjacentFuelCells(NCBlockPos.of(toCheck), 3);
                        controllerBE().moderatorCellMultiplier += (countAdjacentFuelCells(NCBlockPos.of(toCheck), 1)+1)*moderatorAttachments;
                        controllerBE().moderatorAttachments += moderatorAttachments;
                        indexDirectHeatSinks(new BlockPos(toCheck));
                    }
                }
            }
        }
        controllerBE().fuelCellsCount = fuelCells.size();
    }

    private void indexDirectHeatSinks(BlockPos toCheck) {
        for(Direction d : Direction.values()) {
            if(isHeatSink(toCheck.relative(d))) {
                addSecondConnectionsToFuelCell(toCheck.relative(d));
            }
        }
    }

    private FissionControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = (FissionControllerBE) controller().controllerBE();
        }
        return controllerBe;
    }


    @Override
    protected boolean processInnerBlock(BlockPos toCheck) {
        if(isFuelCell(toCheck)) {
            return true;
        }
        if(isModerator(toCheck)) {
            return true;
        }
        if(isHeatSink(toCheck)) {
            if(isAttachedToFuelCell(toCheck)) {
                addIfNotExists(new BlockPos(toCheck), heatSinks);
                addSecondConnectionsToFuelCell(new BlockPos(toCheck));
                return true;
            } else {
                if(!delayedValidationFlag) {
                    delayedValidation.add(new BlockPos(toCheck));
                }
            }
        }
        if(isIrradiator(toCheck)) {
            addIfNotExists(new BlockPos(toCheck), irradiators);
            countIrradiationConnections(toCheck);
            return true;
        }
        return true;
    }

    private void addSecondConnectionsToFuelCell(BlockPos toCheck) {
        addIfNotExists(toCheck, secondFuelCellConnectionPos);
        for(Direction d : Direction.values()) {
            addIfNotExists(toCheck.relative(d), secondFuelCellConnectionPos);
        }
    }

    private void addDirectFuelCellConnection(BlockPos toCheck) {
        addIfNotExists(toCheck, directFuelCellConnectionPos);
        for(Direction d : Direction.values()) {
            addIfNotExists(toCheck.relative(d), directFuelCellConnectionPos);
        }
    }

    public boolean checkAttachmentToBlock(Class<?> toCheck, Level level, BlockPos pos, Direction dir) {
        if (
                getBottomLeftBlock().getX() >= pos.getX()
                && getBottomLeftBlock().getY() >= pos.getX()
                && getBottomLeftBlock().getZ() >= pos.getZ()
                && getTopRightBlock().getX() <= pos.getX()
                && getTopRightBlock().getY() <= pos.getY()
                && getTopRightBlock().getZ() <= pos.getZ()
                && !allBlocks.contains(pos)
        ) {
            return false;
        }

        if (toCheck.equals(FissionFuelCellBlock.class)) {
            return directFuelCellConnectionPos.contains(pos) || secondFuelCellConnectionPos.contains(pos);
        }
        return false;
    }

    private int countAttachedModeratorsToFuelCell(BlockPos toCheck) {
        int count = 0;
        for(Direction d : Direction.values()) {
            if(isModerator(toCheck.relative(d))) {
                addIfNotExists(new BlockPos(toCheck.relative(d)), moderators);
                addSecondConnectionsToFuelCell(new BlockPos(toCheck.relative(d)));
                count++;
            }
        }
        return count;
    }

    private void countIrradiationConnections(BlockPos toCheck) {
        for(Direction d: Direction.values()) {
            if(isModerator(toCheck.relative(d))) {
                Block bs = getBlockState(toCheck.relative(d, 2)).getBlock();
                if(bs instanceof FissionFuelCellBlock) {
                    irradiationConnections++;
                }
            }
        }
    }

    private int countAdjacentFuelCells(NCBlockPos toCheck, int step) {
        int count = 0;
        for (Direction d : Direction.values()) {
            if (isFuelCell(toCheck.revert().relative(d))) {
                count+=step;
                continue;
            }
            if(isModerator(toCheck.revert().relative(d))) {
                if(isFuelCell(toCheck.revert().relative(d, 2))) {
                    count += step;
                }
            }
        }
        return count;
    }

    public void invalidateStats()
    {
        controller().clearStats();
        moderators.clear();
        irradiators.clear();
        fuelCells.clear();
        heatSinks.clear();
        validHeatSinks.clear();
        coolantPerTick.clear();
        directFuelCellConnectionPos.clear();
        secondFuelCellConnectionPos.clear();
        delayedValidation.clear();
        irradiationConnections = 0;
        delayedValidationFlag = false;
    }

    protected Direction getFacing() {
        return controllerBE().getFacing();
    }

    public double countCooling(boolean forceCheck) {
        if(refreshInnerCacheFlag || forceCheck) {
            heatSinkCooling = 0;
            for (HeatSinkBlock hs : validHeatSinks().values()) {
                heatSinkCooling += hs.heat;
            }
        }
        return heatSinkCooling;
    }

}
