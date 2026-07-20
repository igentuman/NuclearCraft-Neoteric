package igentuman.nc.block_entity.energy;

import igentuman.nc.content.energy.EnergyGenDefs;
import igentuman.nc.util.NBTField;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.binding.RadiationBindings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.util.TagUtil.getSingleBlockByTagKey;

public class DecayGeneratorBE extends AbstractEnergyBE {

    @NBTField
    public int[] ticks = new int[6];

    private Block leadBlock;
    private RadiationProfile pendingRadiation;

    public DecayGeneratorBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name, EnergyGenDefs.DECAY_GENERATOR_CAPACITY);
    }

    private Block getLeadBlock() {
        if (leadBlock == null) {
            leadBlock = getSingleBlockByTagKey("c:storage_blocks/lead");
        }
        return leadBlock;
    }

    @Override
    protected int generate() {
        if (level == null) return 0;
        long now = level instanceof ServerLevel sl ? sl.getGameTime() : 0L;
        RadiationProfile accumulated = new RadiationProfile();
        double energy = 0;
        for (Direction side : Direction.values()) {
            BlockPos neighbour = worldPosition.relative(side);
            BlockState neighbourState = level.getBlockState(neighbour);
            if (!neighbourState.is(EnergyGenDefs.DECAY_GENERATOR_BLOCKS)) {
                ticks[side.ordinal()] = 0;
                continue;
            }
            RadiationProfile profile = RadiationBindings.of(neighbourState);
            double activity = profile.totalActivityBq();
            if (!profile.isEmpty()) {
                accumulated.mergeAtoms(profile, 1.0, now);
            }
            energy += activity > 0 ? Math.log1p(activity) * 20 : EnergyGenDefs.DECAY_GENERATOR_BASE_FE;
            ticks[side.ordinal()]++;
            if (ticks[side.ordinal()] > EnergyGenDefs.DECAY_GENERATOR_DURATION) {
                ticks[side.ordinal()] = 0;
                decayBlock(neighbour);
            }
        }
        pendingRadiation = accumulated;
        return (int) energy;
    }

    private void decayBlock(BlockPos pos) {
        if (level == null) return;
        Block lead = getLeadBlock();
        if (lead == Blocks.AIR) return;
        level.setBlock(pos, lead.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void emitRadiation() {
        if (!(level instanceof ServerLevel server)) return;
        if (server.getGameTime() % 200 != 0) return;
        if (pendingRadiation != null && !pendingRadiation.isEmpty()) {
            assertRadiationSource(pendingRadiation);
        } else {
            clearRadiationSource();
        }
    }
}
