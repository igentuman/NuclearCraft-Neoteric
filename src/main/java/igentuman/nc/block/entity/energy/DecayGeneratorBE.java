package igentuman.nc.block.entity.energy;

import igentuman.api.platform.NCNames;
import igentuman.nc.NuclearCraft;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.data.RadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.handler.config.CommonConfig.ENERGY_GENERATION;
import static igentuman.nc.setup.registration.NCBlocks.DECAY_GEN_BLOCK;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;
import static igentuman.nc.util.TagUtil.getSingleBlockByTagKey;

public class DecayGeneratorBE extends NCEnergy {

    private int[] ticks = new int[6];
    public Block leadBlock;
    public int decayDuration = 36000; // 30 minutes

    public DecayGeneratorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
        energyStorage.setMaxCapacity(ENERGY_GENERATION.DECAY_GENERATOR.get()*32);
    }

    public static String getName(BlockState pBlockState) {
        return NCNames.of(pBlockState.getBlock().asItem());
    }

    @Override
    public String getName() {
        return NCNames.of(getBlockState().getBlock().asItem());
    }

    private List<Block> allowedBlocks = new ArrayList<>();

    private List<Block> getAllowedBlocks() {
        if(allowedBlocks.isEmpty()) {
            allowedBlocks = getBlocksByTagKey(DECAY_GEN_BLOCK.location().toString()).stream().toList();
        }
        return allowedBlocks;
    }

    /**
     * Get block by tag. Use mod priority
     * @return Block
     */
    private Block getLeadBlock() {
        if(leadBlock == null) {
            leadBlock = getSingleBlockByTagKey("c:storage_blocks/lead");
        }
        return leadBlock;
    }

    /**
     * Checks aligned blocks
     * Energy depends on block radiation
     *
     * @return double
     */
    private int getEnergyFromConnectedBlocks() {
        double energy = 0;
        for(Direction side : Direction.values()) {
            Block connectedBlock = getLevel().getBlockState(getBlockPos().relative(side)).getBlock();
            if (!getAllowedBlocks().contains(connectedBlock)) {
                ticks[side.ordinal()] = 0;
                continue;
            }
            energy += Math.log(ItemRadiation.byItem(connectedBlock.asItem())*5000000)*10;
            ticks[side.ordinal()]++;
            if(ticks[side.ordinal()] > decayDuration) {
                ticks[side.ordinal()] = 0;
                decayBlock(getBlockPos().relative(side));
            }
        }
        return (int) ((int) energy * ENERGY_GENERATION.GENERATION_MULTIPLIER.get());
    }

    //transform block
    private void decayBlock(BlockPos relative) {
        getLevel().setBlock(relative, getLeadBlock().defaultBlockState(), 3);
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        super.tickServer();
        energyStorage.addEnergy(getEnergyFromConnectedBlocks());
        sendOutPower();
        if(currentTick % 40 == 0) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), (double) RTGs.all().get("uranium_rtg").config().getRadiation() / 500000000, worldPosition);
        }
    }

    @Override
    protected int getEnergyMaxStorage() {
        return ENERGY_GENERATION.DECAY_GENERATOR.get()*32;
    }
    @Override
    protected int getEnergyTransferPerTick() {
        return energyStorage.getEnergyStored();
    }
}
