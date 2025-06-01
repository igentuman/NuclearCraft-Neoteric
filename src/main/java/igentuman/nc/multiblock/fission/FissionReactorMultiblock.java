package igentuman.nc.multiblock.fission;

import igentuman.nc.block.entity.fission.*;
import igentuman.nc.block.fission.FissionCasingBlock;
import igentuman.nc.block.fission.FissionFuelCellBlock;
import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.block.fission.IrradiationChamberBlock;
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

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class FissionReactorMultiblock extends AbstractMultiblock {

    protected int irradiationLines = 0;
    protected int extraFuelCells = 0;
    protected int moderatorAttachments = 0;
    protected double heatSinkCooling = 0;
    protected double activeCooling = 0;
    protected final List<Block> validModerators;
    protected final HashMap<Long, HeatSinkBlock> validHeatSinks = new HashMap<>();
    protected final List<Long> moderators = new ArrayList<>();
    protected final List<Long> irradiators = new ArrayList<>();
    protected final List<Long> heatSinks = new ArrayList<>();
    public final List<Long> fuelCells = new ArrayList<>();
    protected final List<Long> allModerators = new ArrayList<>();
    protected final List<Long> validIrradiators = new ArrayList<>();
    protected final List<Long> activeCoolers = new ArrayList<>();
    protected final List<Long> allHeatSinks = new ArrayList<>();
    protected double cellsHeatMult = 0.0D;
    protected double moderatorsHeatMult = 0.0D;
    protected double cellsEnergyMult = 0.0D;
    protected double moderatorsEnergyMult = 0.0D;
    protected final List<Long> directFuelCellConnectionPos = new ArrayList<>();
    protected final List<Long> secondFuelCellConnectionPos = new ArrayList<>();
    public final HashMap<String, Integer> coolantPerTick = new HashMap<>();
    protected final List<Long> delayedValidation = new ArrayList<>();


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
    }

    public boolean isModerator(BlockState bs) {
        return validModerators.contains(bs.getBlock());
    }

    public boolean isModerator(BlockPos pos) {
        return allModerators.contains(pos.asLong()) || isModerator(getBlockState(pos));
    }

    public boolean isIrradiator(BlockPos pos) {
        return getBlockState(pos).getBlock() instanceof IrradiationChamberBlock;
    }

    protected boolean isHeatSink(BlockState bs) {
        return bs.getBlock() instanceof HeatSinkBlock;
    }

    protected boolean isHeatSink(BlockPos pos) {
        return allHeatSinks.contains(pos.asLong()) || isHeatSink(getBlockState(pos));
    }

    private boolean isFuelCell(BlockPos pos) {
        return fuelCells.contains(pos.asLong()) || isFuelCell(getBlockState(pos));
    }

    protected boolean isFuelCell(BlockState bs) {
        return bs.getBlock() instanceof FissionFuelCellBlock;
    }

    @Override
    public void validate() {
        heatSinkCooling = 0;
        moderatorAttachments = 0;
        extraFuelCells = 0;
        irradiationLines = 0;
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
            clearStats();
            return;
        }
        resolveDimensions();
        extraFuelCells = 0;
        moderatorAttachments = 0;
        irradiationLines = 0;
        //Stage 1: Index all inner blocks
        indexInnerBlocks();
        if(validationResult != ValidationResult.VALID) {
            clearStats();
            return;
        }
        //Stage 2: Validate moderators and count attachments
        //indexModerators();
        //Stage 3: count fuel cell attachments
        indexFuelCellAttachments();
        //Stage 4: index irradiators and count irradiation lines
        indexIrradiators();
        //Stage 5: count heat sinks and their cooling
        indexHeatSinks();
        //Stage 6: update controller stats
        controllerBE().irradiationLines = irradiationLines;
        controllerBE().allIrradiators = irradiators.size();
        controllerBE().validIrradiators = validIrradiators.size();
        controllerBE().heatSinksCount = heatSinks.size();
        controllerBE().allHeatSinks = allHeatSinks.size();
        controllerBE().activeCoolingHeatsinks = activeCoolers.size();
        controllerBE().moderatorsCount = moderators.size();
        controllerBE().allModerators = allModerators.size();
        controllerBE().moderatorAttachments = moderatorAttachments;
        controllerBE().connectedPorts = connectedPorts;
        controllerBE().extraFuelCells = extraFuelCells;
        controllerBE().fuelCellsCount = fuelCells.size();
        controllerBE().cellsHeatMult = cellsHeatMult;
        controllerBE().moderatorsHeatMult = moderatorsHeatMult;
        controllerBE().cellsEnergyMult = cellsEnergyMult;
        controllerBE().moderatorsEnergyMult = moderatorsEnergyMult;
        heatSinkCooling = countCooling(true);
        controllerBE().setChanged();
    }

    private void indexIrradiators() {
        validIrradiators.clear();
        irradiationLines = 0;
        for(long pos: irradiators) {
            BlockPos toCheck = BlockPos.of(pos);
            for(Direction d: Direction.values()) {
                if(isModerator(toCheck.relative(d)) && isFuelCell(toCheck.relative(d, 2))) {
                    irradiationLines++;
                    addIfNotExists(pos, validIrradiators);
                }
            }
        }
    }

    private void indexHeatSinks() {
        validHeatSinks.clear();
        delayedValidation.clear();
        for(long pos: allHeatSinks) {
            BlockPos hsPos = BlockPos.of(pos);
            if(isHeatSinkValid(hsPos)) {
                HeatSinkBlock hb = (HeatSinkBlock) getBlockState(pos).getBlock();
                addIfNotExists(pos, heatSinks);
                validHeatSinks.put(pos, hb);
                addSecondConnectionsToFuelCell(hsPos);
                if(hb.isActive()) {
                    addIfNotExists(pos, activeCoolers);
                }
            } else {
                addIfNotExists(pos, delayedValidation);
            }
        }
        for (long pos: delayedValidation) {
            BlockPos hsPos = BlockPos.of(pos);
            if(isHeatSinkValid(hsPos)) {
                HeatSinkBlock hb = (HeatSinkBlock) getBlockState(pos).getBlock();
                validHeatSinks.put(pos, hb);
                addIfNotExists(pos, heatSinks);
                addSecondConnectionsToFuelCell(hsPos);
                if(hb.isActive()) {
                    addIfNotExists(pos, activeCoolers);
                }
            } else {
                debugLog("Invalid: " + hsPos.toShortString() + " - " + getBlockState(pos).getBlock().asItem());
            }
        }
    }

    private boolean isHeatSinkValid(BlockPos pos) {
        HeatSinkBlock hb = (HeatSinkBlock) getBlockState(pos).getBlock();
        return hb.isValid(getLevel(), pos, this);
    }

    private void indexFuelCellAttachments() {
        cellsHeatMult = 0D;
        moderatorsHeatMult = 0D;
        cellsEnergyMult = 0D;
        moderatorsEnergyMult = 0D;
        for(long pos: fuelCells) {
            extraFuelCells = 0;
            extraFuelCells += countAdjacentFuelCells(BlockPos.of(pos));
            cellsHeatMult += (extraFuelCells + 1D)*(extraFuelCells + 2D)/2D;
            cellsEnergyMult += extraFuelCells + 1D;
            int moderators = getFuelCellModerators(pos);
            moderatorsHeatMult += moderators * (extraFuelCells+1D)*(FISSION_CONFIG.MODERATOR_HEAT_MULTIPLIER.get() / 100);
            moderatorsEnergyMult += moderators * (extraFuelCells+1D)*(FISSION_CONFIG.MODERATOR_FE_MULTIPLIER.get() / 100);
        }
    }

    private int getFuelCellModerators(long pos) {
        BlockPos fuelCellPos = BlockPos.of(pos);
        int count = 0;
        for(Direction d : Direction.values()) {
            BlockPos toCheck = fuelCellPos.relative(d);
            if(isModerator(toCheck)) {
                addIfNotExists(toCheck.asLong(), moderators);
                addDirectFuelCellConnection(toCheck);
                count++;
            }
        }
        return count;
    }

    private void indexModerators() {
        moderatorAttachments = 0;
        for(long pos: allModerators) {
            BlockPos moderatorPos = BlockPos.of(pos);
            int attachments = getFuelCellModerators(moderatorPos);
            if(attachments > 0) {
                addIfNotExists(pos, moderators);
            }
            moderatorAttachments += attachments;
        }
    }

    private int getFuelCellModerators(BlockPos moderatorPos) {
        int count = 0;
        for(Direction d : Direction.values()) {
            BlockPos toCheck = moderatorPos.relative(d);
            if(isFuelCell(toCheck)) {
                addIfNotExists(moderatorPos, directFuelCellConnectionPos);
                count++;
            }
        }
        return count;
    }

    private void indexInnerBlocks() {
        for(int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                for (int z = 1; z < depth - 1; z++) {
                    NCBlockPos toCheck = new NCBlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                    if(!processInnerBlock(toCheck)) {
                        validationResult = ValidationResult.WRONG_INNER;
                        errorBlockPos = new BlockPos(toCheck);
                        return;
                    }
                }
            }
        }
        validationResult = ValidationResult.VALID;
        errorBlockPos = null;
    }

    protected FissionControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (FissionControllerBE) controllerBe;
    }


    @Override
    protected boolean processInnerBlock(BlockPos toCheck) {
        addIfNotExists(toCheck, allBlocks);
        final BlockState bs = getBlockState(toCheck);
        if(isFuelCell(bs)) {
            addDirectFuelCellConnection(new BlockPos(toCheck));
            addIfNotExists(toCheck, fuelCells);
            return true;
        }
        if(isModerator(bs)) {
            addIfNotExists(toCheck, allModerators);
            return true;
        }
        if(isHeatSink(bs)) {
            addIfNotExists(toCheck, allHeatSinks);
            return true;
        }
        if(isIrradiator(toCheck)) {
            addIfNotExists(toCheck, irradiators);
            return true;
        }

        return isValidForInner(bs);
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

    private int countAdjacentFuelCells(BlockPos toCheck) {
        int count = 0;
        for (Direction d : Direction.values()) {
            for(int l = 1; l < 5; l++) {
                BlockState blockState = getBlockState(toCheck.relative(d, l));
                if (isFuelCell(blockState)) {
                    count++;
                    break;
                }

                if(isModerator(blockState)) {
                    if(isFuelCell(toCheck.relative(d, l + 1))) {
                        count ++;
                        break;
                    }
                } else {
                    break;
                }
            }
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
                allModerators.remove(packedPos);
                allHeatSinks.remove(packedPos);
                fuelCells.remove(packedPos);
                irradiators.remove(packedPos);
                validIrradiators.remove(packedPos);
                directFuelCellConnectionPos.remove(packedPos);
                secondFuelCellConnectionPos.remove(packedPos);
            }
        }
    }

    public void clearStats()
    {
        controller().clearStats();
        controllerBE().fuelCellsCount = 0;
        controllerBE().extraFuelCells = 0;
        controllerBE().moderatorAttachments = 0;
        controllerBE().moderatorsCount = 0;
        controllerBE().allModerators = 0;
        controllerBE().allIrradiators = 0;
        controllerBE().irradiationLines = 0;
        controllerBE().validIrradiators = 0;
        controllerBE().heatSinksCount = 0;
        controllerBE().allHeatSinks = 0;
        controllerBE().activeCoolingHeatsinks = 0;
        controllerBE().connectedPorts = 0;
        controllerBE().moderatorsHeatMult = 0;
        controllerBE().cellsEnergyMult = 0;
        controllerBE().moderatorsEnergyMult = 0;
        controllerBE().cellsHeatMult = 0;
        controllerBE().bottomLeft = BlockPos.ZERO;
        controllerBE().topRight = BlockPos.ZERO;
        controllerBE().setChanged();
    }

    protected Direction getControllerDirection() {
        return controllerBE().getFacing();
    }

    public double countCooling(boolean forceCheck) {
        if(forceCheck) {
            heatSinkCooling = 0;
            for (HeatSinkBlock hs : validHeatSinks().values()) {
                heatSinkCooling += hs.heat;
            }
        }
        return heatSinkCooling;
    }
}
