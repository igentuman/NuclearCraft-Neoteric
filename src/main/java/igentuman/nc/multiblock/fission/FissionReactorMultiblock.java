package igentuman.nc.multiblock.fission;

import igentuman.nc.block.entity.fission.*;
import igentuman.nc.block.fission.FissionCasingBlock;
import igentuman.nc.block.fission.FissionFuelCellBlock;
import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.block.fission.IrradiationChamberBlock;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.handler.event.server.WorldEvents;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class FissionReactorMultiblock extends AbstractMultiblock {

    public int irradiationConnections = 0;
    private final List<Block> validModerators;
    public final HashMap<Long, HeatSinkBlock> validHeatSinks = new HashMap<>();
    public final List<Long> moderators = new ArrayList<>();
    public final List<Long> irradiators = new ArrayList<>();
    public final List<Long> heatSinks = new ArrayList<>();
    public final List<Long> fuelCells = new ArrayList<>();
    public final List<Long> allModerators = new ArrayList<>();
    public final List<Long> validIrradiators = new ArrayList<>();
    public final List<Long> activeCoolers = new ArrayList<>();
    private double heatSinkCooling = 0;
    public double activeCooling = 0;
    public final List<Long> allHeatSinks = new ArrayList<>();
    private FissionControllerBE controllerBe;
    private final List<Long> directFuelCellConnectionPos = new ArrayList<>();
    private final List<Long> secondFuelCellConnectionPos = new ArrayList<>();
    public final HashMap<String, Integer> coolantPerTick = new HashMap<>();
    private final List<Long> delayedValidation = new ArrayList<>();
    private boolean delayedValidationFlag = false;
    private int fuelCellMultiplier = 0;
    private int moderatorCellMultiplier = 0;
    public int moderatorAttachments = 0;

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
                getBlocksByTagKey(FissionReactorRegistration.CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(FissionReactorRegistration.INNER_REACTOR_BLOCKS.location().toString()),
                new FissionReactorController(fissionControllerBE)
        );
        id = "fission_reactor_"+fissionControllerBE.getBlockPos().toShortString();
        validModerators = getBlocksByTagKey(FissionReactorRegistration.MODERATORS_BLOCKS.location().toString());
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
        MultiblockHandler.get(getLevel().dimension()).addMultiblock(this);
    }

    public Map<Long, HeatSinkBlock> validHeatSinks() {
        if(validHeatSinks.isEmpty()) {
            for(long packedPos: allHeatSinks) {
                BlockPos hpos = BlockPos.of(packedPos);
                Block block = getBlockState(hpos).getBlock();
                if(block instanceof HeatSinkBlock hs) {
                    if(hs.isValid(getLevel(), hpos, this)) {
                        validHeatSinks.put(packedPos, hs);
                        if(hs.isActive()) {
                            activeCoolers.add(packedPos);
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
        if(!FissionReactorRegistration.heatsinks.containsKey("active_"+coolant)) {
            return 0;
        }
        int mbPerTick = FISSION_CONFIG.ACTIVE_HEATSINK_COOLANT_PER_TICK.get();
        FissionReactorRegistration.heatsinks.get("active_"+coolant);
        return ((double)amount /(double)mbPerTick)*FissionReactorRegistration.heatsinks.get("active_"+coolant).heat;
    }

    public boolean isModerator(BlockPos pos) {
        return  validModerators.contains(getBlockState(pos).getBlock());
    }

    public boolean isIrradiator(BlockPos pos) {
        BlockState bs = getBlockState(pos);
        if (bs == null) {
            return false;
        }
        return  bs.getBlock() instanceof IrradiationChamberBlock;
    }

    protected boolean isHeatSink(BlockPos pos) {
        BlockState bs = getBlockState(pos);
        if (bs == null) {
            return false;
        }
        return bs.getBlock() instanceof HeatSinkBlock;
    }

    protected boolean isFuelCell(BlockPos pos) {
        BlockState bs = getBlockState(pos);
        if (bs == null) {
            return false;
        }
        return bs.getBlock() instanceof FissionFuelCellBlock;
    }

    private boolean isAttachedToFuelCell(BlockPos toCheck) {
        if (directFuelCellConnectionPos.contains(toCheck.asLong())) {
            return true;
        }
        if (secondFuelCellConnectionPos.contains(toCheck.asLong())) {
            return true;
        }

        for(Direction d : Direction.values()) {
            if(isFuelCell(toCheck.relative(d))) {
                addDirectFuelCellConnection((toCheck.relative(d)));
                return true;
            }
            if (directFuelCellConnectionPos.contains(toCheck.asLong())) {
                return true;
            }
            if (secondFuelCellConnectionPos.contains(toCheck.asLong())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validate() {
        heatSinkCooling = 0;
        moderatorAttachments = 0;
        fuelCellMultiplier = 0;
        moderatorCellMultiplier = 0;
        irradiationConnections = 0;
        validHeatSinks.clear();
        coolantPerTick.clear();
        allHeatSinks.clear();
        allModerators.clear();
        validIrradiators.clear();
        activeCoolers.clear();
        super.validate();
    }

    @Override
    public void validateInner()
    {
        if(!outerValid) {
            invalidateStats();
            return;
        }
        resolveDimensions();
        moderatorCellMultiplier = 0;
        fuelCellMultiplier = 0;
        moderatorAttachments = 0;
        irradiationConnections = 0;
        collectFuelCells();
        controllerBE().moderatorCellMultiplier = moderatorCellMultiplier;
        controllerBE().fuelCellMultiplier = fuelCellMultiplier;
        controllerBE().moderatorAttachments = moderatorAttachments;
        for(int y = 1; y < height-1; y++) {
            for(int x = 1; x < width-1; x++) {
                for (int z = 1; z < depth-1; z++) {
                    NCBlockPos toCheck = new NCBlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                    if (!isValidForInner(toCheck)) {
                        validationResult = ValidationResult.WRONG_INNER;
                        errorBlockPos = new BlockPos(toCheck);
                        return;
                    }
                    processInnerBlock(new BlockPos(toCheck));
                }
            }
        }
        delayedValidationFlag = true;
        for(long packedPos: delayedValidation) {
            BlockPos pos = BlockPos.of(packedPos);
            processInnerBlock(pos);
        }


        validationResult =  ValidationResult.VALID;
        heatSinkCooling = countCooling(true);
    }

    private void collectFuelCells() {
        for(int y = 1; y < height-1; y++) {
            for(int x = 1; x < width-1; x++) {
                for (int z = 1; z < depth-1; z++) {
                    NCBlockPos toCheck = new NCBlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                    if (isFuelCell(toCheck)) {
                        addDirectFuelCellConnection(new BlockPos(toCheck));
                        addIfNotExists(new BlockPos(toCheck), fuelCells);
                        int modAttachments = countAttachedModeratorsToFuelCell(new BlockPos(toCheck));
                        fuelCellMultiplier += countAdjacentFuelCells(NCBlockPos.of(toCheck), 2);
                        moderatorCellMultiplier += (countAdjacentFuelCells(NCBlockPos.of(toCheck), 1)+1)*modAttachments;
                        moderatorAttachments += modAttachments;
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
                addIfNotExists(toCheck.relative(d), allHeatSinks);
                addIfNotExists(toCheck.relative(d), heatSinks);
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
        addIfNotExists(toCheck, allBlocks);
        if(isFuelCell(toCheck)) {
            addDirectFuelCellConnection(new BlockPos(toCheck));
            return true;
        }
        if(isModerator(toCheck)) {
            addIfNotExists(toCheck, allModerators);
            return true;
        }
        if(isHeatSink(toCheck)) {
            addIfNotExists(toCheck, allHeatSinks);
            if(isAttachedToFuelCell(toCheck)) {
                addIfNotExists(toCheck, heatSinks);
                addSecondConnectionsToFuelCell(new BlockPos(toCheck));
                return true;
            } else {
                if(!delayedValidationFlag) {
                    delayedValidation.add(toCheck.asLong());
                }
            }
        }
        if(isIrradiator(toCheck)) {
            addIfNotExists(toCheck, irradiators);
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
                && !allBlocks.contains(pos.asLong())
        ) {
            return false;
        }

        if (toCheck.equals(FissionFuelCellBlock.class)) {
            return directFuelCellConnectionPos.contains(pos.asLong()) || secondFuelCellConnectionPos.contains(pos.asLong());
        }
        return false;
    }

    private int countAttachedModeratorsToFuelCell(BlockPos toCheck) {
        int count = 0;
        for(Direction d : Direction.values()) {
            if(isModerator(toCheck.relative(d))) {
                addIfNotExists(toCheck.relative(d), allModerators);
                addIfNotExists(toCheck.relative(d), moderators);
                addSecondConnectionsToFuelCell(toCheck.relative(d));
                count++;
            }
        }
        return count;
    }

    private void countIrradiationConnections(BlockPos toCheck) {
        for(Direction d: Direction.values()) {
            if(isModerator(toCheck.relative(d))) {
                addIfNotExists(toCheck.relative(d), allModerators);
                Block bs = getBlockState(toCheck.relative(d, 2)).getBlock();
                if(bs instanceof FissionFuelCellBlock) {
                    addIfNotExists(toCheck ,validIrradiators);
                    irradiationConnections++;
                }
            }
        }
    }

    private int countAdjacentFuelCells(NCBlockPos toCheck, int step) {
        int count = 0;
        for (Direction d : Direction.values()) {
            for(int l = 1; l < 6; l++) {
                if (isFuelCell(toCheck.revert().relative(d, l))) {
                    count += step;
                    break;
                }

                if(isModerator(toCheck.revert().relative(d, l))) {
                    addIfNotExists(toCheck.revert().relative(d), allModerators);
                    if(isFuelCell(toCheck.revert().relative(d, l + 1))) {
                        count += step;
                        break;
                    }
                }
            }
        }
        if(count == 0) {
            count = 1;
        }
        return count;
    }

    @Override
    public void removeFromCacheIfChanged(BlockPos pos) {
        long packedPos = pos.asLong();
        if (beCache.containsKey(packedPos)) {
            BlockEntity be = getLevel().getExistingBlockEntity(pos);
            if(be != beCache.get(packedPos) || (be != null && be.isRemoved())) {
                beCache.remove(packedPos);
                allBlocks.remove(packedPos);
            }
        }
        if (bsCache.containsKey(packedPos)) {
            BlockState bs = getLevel().getBlockState(pos);
            BlockState cachedState = bsCache.get(packedPos);
            if(cachedState == null || !bs.is(bsCache.get(packedPos).getBlock())) {
                bsCache.remove(packedPos);
                bsCache.put(packedPos, bs);
                allBlocks.remove(packedPos);
                moderators.remove(packedPos);
                heatSinks.remove(packedPos);
                fuelCells.remove(packedPos);
                irradiators.remove(packedPos);
                directFuelCellConnectionPos.remove(packedPos);
                secondFuelCellConnectionPos.remove(packedPos);
            }
        }
    }

    public void invalidateStats()
    {
        controller().clearStats();
        coolantPerTick.clear();
        delayedValidation.clear();
        irradiationConnections = 0;
        delayedValidationFlag = false;
        controllerBE().moderatorCellMultiplier = 0;
        controllerBE().fuelCellMultiplier = 0;
        controllerBE().moderatorAttachments = 0;
    }

    protected Direction getControllerDirection() {
        return controllerBE().getFacing();
    }

    public double countCooling(boolean forceCheck) {
        if(forceCheck) {
            //validHeatSinks();
            heatSinkCooling = 0;
            for (HeatSinkBlock hs : validHeatSinks().values()) {
                heatSinkCooling += hs.heat;
            }
        }
        return heatSinkCooling;
    }

}
