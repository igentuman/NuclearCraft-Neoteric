package igentuman.nc.block.entity.energy;

import igentuman.nc.NuclearCraft;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.data.RadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.ENERGY_GENERATION;
import static igentuman.nc.setup.registration.NCBlocks.DECAY_GEN_BLOCK;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;
import static igentuman.nc.util.TagUtil.getSingleBlockByTagKey;

public class DecayGeneratorBE extends NCEnergy {
    public DecayGeneratorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
        energyStorage.setMaxCapacity(2000);
    }

    public Block leadBlock;
    public int decayDuration = 36000; // 30 minutes
    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString();
    }
    private int[] ticks = new int[6];

    @Override
    public String getName() {
        return getBlockState().getBlock().asItem().toString();
    }

    private List<Block> allowedBlocks = new ArrayList<>();

    private List<Block> getAllowedBlocks() {
        if(allowedBlocks.isEmpty()) {
            allowedBlocks = getBlocksByTagKey(DECAY_GEN_BLOCK.location().toString());
        }
        return allowedBlocks;
    }

    /**
     * Get block by tag. Use mod priority
     * @return Block
     */
    private Block getLeadBlock() {
        if(leadBlock == null) {
            leadBlock = getSingleBlockByTagKey("forge:storage_blocks/lead");
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
        return (int) energy;
    }

    //transform block
    private void decayBlock(BlockPos relative) {
        getLevel().setBlock(relative, getLeadBlock().defaultBlockState(), 3);
    }

    protected int radiationTimer = 40;
    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        super.tickServer();
        energyStorage.setEnergy(getEnergyFromConnectedBlocks());
        sendOutPower();
        radiationTimer--;
        if(radiationTimer <= 0) {
            radiationTimer = 40;
            RadiationManager.get(getLevel()).addRadiation(getLevel(), (double) RTGs.all().get("uranium_rtg").config().getRadiation() /500000000, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
    }

    @Override
    protected int getEnergyMaxStorage() {
        return ENERGY_GENERATION.DECAY_GENERATOR.get();
    }
    @Override
    protected int getEnergyTransferPerTick() {
        return energyStorage.getEnergyStored();
    }
}
