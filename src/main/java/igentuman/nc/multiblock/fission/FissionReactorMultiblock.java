package igentuman.nc.multiblock.fission;

import igentuman.nc.block.fission.FissionCasingBlock;
import igentuman.nc.block.fission.FissionFuelCellBlock;
import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.block.fission.IrradiationChamberBlock;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.handler.event.server.WorldEvents;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

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
    protected final HashSet<Block> validModerators;
    public final HashMap<Long, HeatSinkBlock> validHeatSinks = new HashMap<>();
    protected final HashSet<Long> moderators = new HashSet<>();
    protected final HashSet<Long> irradiators = new HashSet<>();
    public final HashSet<Long> fuelCells = new HashSet<>();
    protected final HashSet<Long> allModerators = new HashSet<>();
    protected final HashSet<Long> validIrradiators = new HashSet<>();
    protected final HashSet<Long> activeHeatSinks = new HashSet<>();
    protected final HashSet<Long> allHeatSinks = new HashSet<>();
    protected final HashMap<String, HashSet<BlockPos>> indexedHeatSinks = new HashMap<>();
    protected final HashMap<BlockPos, String> reversedIndexedHeatSinks = new HashMap<>();
    protected double cellsHeatMult = 0.0D;
    protected double moderatorsHeatMult = 0.0D;
    protected double cellsEnergyMult = 0.0D;
    protected double moderatorsEnergyMult = 0.0D;
    protected final HashSet<Long> directFuelCellConnectionPos = new HashSet<>();
    protected final HashSet<Long> secondFuelCellConnectionPos = new HashSet<>();
    public final HashMap<String, Integer> coolantPerTick = new HashMap<>();

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

    private void addActiveCoolant(String name) {
        if(!coolantPerTick.containsKey(name)) {
            coolantPerTick.put(name, 0);
        }
        coolantPerTick.replace(name, coolantPerTick.get(name)+FISSION_CONFIG.ACTIVE_HEATSINK_COOLANT_PER_TICK.get());
    }

    private void removeActiveCoolant(String name) {
        coolantPerTick.replace(name, coolantPerTick.get(name)-FISSION_CONFIG.ACTIVE_HEATSINK_COOLANT_PER_TICK.get());
        if (coolantPerTick.get(name) == 0) {
            coolantPerTick.remove(name);
        }
    }

    @Override
    public void tick(Level level) {
        super.tick(level);
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
        debugLog("=== Starting Fission Reactor validation at " + controllerPos.toShortString() + " ===");
        
        heatSinkCooling = 0;
        moderatorAttachments = 0;
        extraFuelCells = 0;
        irradiationLines = 0;
        validHeatSinks.clear();
        coolantPerTick.clear();
        allHeatSinks.clear();
        indexedHeatSinks.clear();
        reversedIndexedHeatSinks.clear();
        allModerators.clear();
        validIrradiators.clear();
        activeHeatSinks.clear();
        allBlocks.clear();
        fuelCells.clear();
        irradiators.clear();
        directFuelCellConnectionPos.clear();
        secondFuelCellConnectionPos.clear();
        
        debugLog("Cleared fission reactor specific caches and counters");
        super.validate();
    }

    @Override
    public void validateInner()
    {
        if(!outerValid) {
            debugLog("VALIDATION FAILED - Outer structure invalid, clearing stats");
            clearStats();
            return;
        }
        extraFuelCells = 0;
        moderatorAttachments = 0;
        irradiationLines = 0;
        //Stage 1: Index all inner blocks
        debugLog("Stage 1: Indexing inner blocks");
        indexInnerBlocks();
        debugLog("Stage 1 complete - Result: " + validationResult + 
                ", Fuel cells: " + fuelCells.size() + 
                ", Moderators: " + allModerators.size() + 
                ", Heat sinks: " + allHeatSinks.size() + 
                ", Irradiators: " + irradiators.size());
        if(validationResult != ValidationResult.VALID) {
            debugLog("VALIDATION FAILED - Inner structure invalid: " + validationResult + ", clearing stats");
            clearStats();
            return;
        }
        
        //Stage 2: count fuel cell attachments and moderators
        debugLog("Stage 2: Indexing fuel cell attachments");
        indexFuelCellAttachments();
        debugLog("Stage 2 complete - Extra fuel cells: " + extraFuelCells + 
                ", Moderator attachments: " + moderatorAttachments + 
                ", Cells heat mult: " + String.format("%.2f", cellsHeatMult) + 
                ", Cells energy mult: " + String.format("%.2f", cellsEnergyMult));
        
        //Stage 3: index irradiators and count irradiation lines
        debugLog("Stage 3: Indexing irradiators");
        indexIrradiators();
        debugLog("Stage 3 complete - Irradiation lines: " + irradiationLines + 
                ", Valid irradiators: " + validIrradiators.size() + "/" + irradiators.size());

        //Stage 4: count heat sinks and their cooling
        debugLog("Stage 4: Indexing heat sinks");
        indexHeatSinks();
        debugLog("Stage 4 complete - Valid heat sinks: " + validHeatSinks.size() + "/" + allHeatSinks.size() + 
                ", Active heat sinks: " + activeHeatSinks.size() + 
                ", Coolant types: " + coolantPerTick.size());
        //Stage 5: update controller stats
        debugLog("UPDATING CONTROLLER STATS - Setting values:");
        debugLog("  FuelCells: " + fuelCells.size() + " (was: " + controllerBE().fuelCellsCount + ")");
        debugLog("  Moderators: " + moderators.size() + " (was: " + controllerBE().moderatorsCount + ")");
        debugLog("  HeatSinks: " + validHeatSinks.size() + " (was: " + controllerBE().heatSinksCount + ")");
        debugLog("  AllHeatSinks: " + allHeatSinks.size() + " (was: " + controllerBE().allHeatSinks + ")");
        debugLog("  ActiveCoolingHeatsinks: " + activeHeatSinks.size() + " (was: " + controllerBE().activeCoolingHeatsinks + ")");
        debugLog("  CellsHeatMult: " + cellsHeatMult + " (was: " + controllerBE().cellsHeatMult + ")");
        debugLog("  CellsEnergyMult: " + cellsEnergyMult + " (was: " + controllerBE().cellsEnergyMult + ")");
        
        controllerBE().irradiationLines = irradiationLines;
        controllerBE().allIrradiators = irradiators.size();
        controllerBE().validIrradiators = validIrradiators.size();
        controllerBE().heatSinksCount = validHeatSinks.size();
        controllerBE().allHeatSinks = allHeatSinks.size();
        controllerBE().activeCoolingHeatsinks = activeHeatSinks.size();
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
        controllerBE().height = height;
        controllerBE().width = width;
        controllerBE().depth = depth;
        heatSinkCooling = countCooling(true);
        
        debugLog("CONTROLLER STATS UPDATED - Final values:");
        debugLog("  FuelCells: " + controllerBE().fuelCellsCount);
        debugLog("  Moderators: " + controllerBE().moderatorsCount);
        debugLog("  HeatSinks: " + controllerBE().heatSinksCount);
        debugLog("  HeatSinkCooling: " + heatSinkCooling);
        debugLog("  ControllerBE instance: " + controllerBE().toString());
        debugLog("  ControllerBE side: " + controllerBE().getLevel().isClientSide());
        controllerBE().refresh();
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
        coolantPerTick.clear();
        ArrayList<BlockPos> sortedHeatSinks = new ArrayList<>();
        for (String toCheck: FissionReactorRegistration.hsSchedule) {
            if (indexedHeatSinks.containsKey(toCheck)) {
                HashSet<BlockPos> posSet = indexedHeatSinks.get(toCheck);
                sortedHeatSinks.addAll(posSet);
                for (BlockPos hsPos: posSet) {
                    validHeatSinks.put(hsPos.asLong(), (HeatSinkBlock) getBlockState(hsPos).getBlock());
                }
            }
        }
        for (BlockPos hsPos: sortedHeatSinks) {
            long pos = hsPos.asLong();
            if (isHeatSinkValid(hsPos)) {
                HeatSinkBlock hb = validHeatSinks.get(pos);
                addSecondConnectionsToFuelCell(hsPos);
                if(hb.isActive()) {
                    if (!activeHeatSinks.contains(pos)) {
                        addActiveCoolant(hb.def.name.replace("active_", ""));
                    }
                    addIfNotExists(pos, activeHeatSinks);
                }
            } else {
                if (activeHeatSinks.contains(pos)) {
                    activeHeatSinks.remove(pos);
                    removeActiveCoolant(validHeatSinks.get(pos).def.name.replace("active_", ""));
                }
                validHeatSinks.remove(pos);
                removeSecondConnectionsToFuelCell(hsPos);
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
            String name = String.valueOf(ForgeRegistries.BLOCKS.getKey(bs.getBlock()));
            indexedHeatSinks.computeIfAbsent(name, k -> new HashSet<>()).add(toCheck);
            reversedIndexedHeatSinks.put(toCheck, name);
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

    private void removeSecondConnectionsToFuelCell(BlockPos toCheck) {
        secondFuelCellConnectionPos.remove(toCheck.asLong());
        for(Direction d : Direction.values()) {
            secondFuelCellConnectionPos.remove(toCheck.relative(d).asLong());
        }
    }

    private void addDirectFuelCellConnection(BlockPos toCheck) {
        addIfNotExists(toCheck, directFuelCellConnectionPos);
        for(Direction d : Direction.values()) {
            addIfNotExists(toCheck.relative(d), directFuelCellConnectionPos);
        }
    }

    public boolean checkAttachmentToBlock(Class<?> toCheck, Level level, BlockPos pos, Direction dir) {
        pos = pos.relative(dir);
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
            return directFuelCellConnectionPos.contains(pos.asLong()) || (secondFuelCellConnectionPos.contains(pos.asLong()) && !getBlockState(pos).isAir());
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
            }
        }
        if (bsCache.containsKey(packedPos)) {
            BlockState bs = getLevel().getBlockState(pos);
            BlockState cachedState = bsCache.get(packedPos);
            if(cachedState == null || !bs.is(bsCache.get(packedPos).getBlock())) {
                bsCache.remove(packedPos);
                moderators.remove(packedPos);
                for (Direction dir: Direction.values()) {
                    long pPos = pos.relative(dir).asLong();
                    moderators.remove(pPos);
                    allModerators.remove(pPos);
                    directFuelCellConnectionPos.remove(pPos);
                    secondFuelCellConnectionPos.remove(pPos);
                }
                String posHeatSink = reversedIndexedHeatSinks.get(pos);
                reversedIndexedHeatSinks.remove(pos);
                indexedHeatSinks.get(posHeatSink).remove(pos);
                if (indexedHeatSinks.get(posHeatSink).isEmpty()) {
                    indexedHeatSinks.remove(posHeatSink);
                }
                activeHeatSinks.remove(packedPos);
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
            for (HeatSinkBlock hs : validHeatSinks.values()) {
                heatSinkCooling += hs.heat;
            }
        }
        return heatSinkCooling;
    }
}
