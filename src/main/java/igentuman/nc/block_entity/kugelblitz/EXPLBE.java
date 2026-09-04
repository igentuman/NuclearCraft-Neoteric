package igentuman.nc.block_entity.kugelblitz;

import igentuman.nc.block.kugelblitz.EXPLBlock;
import igentuman.nc.client.particle.FusionBeamParticleData;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.NCSounds;
import igentuman.nc.container.EXPLContainer;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class EXPLBE extends GlobalBlockEntity implements MenuProvider {

    @NBTField public int inputRedstoneSignal = 0;
    @NBTField public boolean activated = false;
    @NBTField public int pulseTime = 0;
    @NBTField public long aggregatedEnergy = 0;

    private boolean activatedByOther = false;
    private boolean energyTransfered = false;
    private long lastTickTime = -1;

    public EXPLBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    private Direction getFacing() {
        return getBlockState().getValue(FACING);
    }

    public boolean hasEnoughEnergy() {
        return aggregatedEnergy >= Multiblocks.kugelblitzExplCharge;
    }

    public void activate(boolean byOther) {
        if (hasEnoughEnergy() && pulseTime == 0) {
            pulseTime = 90;
            activatedByOther = byOther;
            activated = true;
            setChanged();
        }
    }

    public void triggerBurst() {
        if (hasEnoughEnergy() && !activated) {
            activated = true;
            setChanged();
        }
    }

    public long getCharge() {
        return aggregatedEnergy;
    }

    public boolean isReadyToBurst() {
        return hasEnoughEnergy() && !activated;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.nuclearcraft.expl");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new EXPLContainer(containerId, playerInventory, this, syncData());
    }

    private ContainerData syncData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int) (aggregatedEnergy & 0xFFFFFFFFL);
                    case 1 -> (int) (aggregatedEnergy >>> 32);
                    case 2 -> isReadyToBurst() ? 1 : 0;
                    case 3 -> (int) (Multiblocks.kugelblitzExplCharge & 0xFFFFFFFFL);
                    case 4 -> (int) (Multiblocks.kugelblitzExplCharge >>> 32);
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) { }

            @Override
            public int getCount() {
                return 5;
            }
        };
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel) || isRemoved()) return;
        if (lastTickTime == level.getGameTime()) return;
        lastTickTime = level.getGameTime();

        long wasCharge = aggregatedEnergy;
        if (!hasEnoughEnergy() && energyStorage != null) {
            aggregatedEnergy += energyStorage.getEnergyStored();
            aggregatedEnergy = Math.min(aggregatedEnergy, Multiblocks.kugelblitzExplCharge);
            energyStorage.setEnergyStored(0);
        }

        inputRedstoneSignal = level.getBestNeighborSignal(worldPosition);
        for (BlockPos proxyPos : EXPLBlock.proxyPositions(worldPosition, getFacing())) {
            inputRedstoneSignal = Math.max(inputRedstoneSignal, level.getBestNeighborSignal(proxyPos));
        }
        activated = activated || inputRedstoneSignal > 0;

        if (activated && hasEnoughEnergy() && pulseTime == 0) {
            pulseTime = 90;
            if (inputRedstoneSignal == 0 && !activatedByOther) {
                findAndPulseOtherLasers();
            }
        }

        if (pulseTime > 0) {
            pulseTime--;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            if (pulseTime < 35) {
                renderBeam();
                aggregatedEnergy /= 2;
            }
            if (pulseTime < 33 && pulseTime > 30) {
                transferEnergy();
                activatedByOther = false;
            }
            if (pulseTime < 10) {
                aggregatedEnergy = 0;
                energyTransfered = false;
            }
        }

        if ((activated && pulseTime < 1) || wasCharge != aggregatedEnergy) {
            if (pulseTime < 1) activated = false;
            setChanged();
        }
    }

    private void findAndPulseOtherLasers() {
        Direction facing = getFacing();
        int max = Multiblocks.kugelblitzLaserDistance + 4;
        for (int i = 4; i <= max; i++) {
            BlockPos pos = worldPosition.relative(facing, i);
            if (level.getBlockEntity(pos) instanceof PhotonConcentratorBE) {
                BlockPos center = pos.relative(facing, 5);
                for (Direction dir : Direction.values()) {
                    if (dir == facing.getOpposite()) continue;
                    for (int j = 10; j < Multiblocks.kugelblitzLaserDistance + 20; j++) {
                        if (level.getBlockEntity(center.relative(dir, j)) instanceof EXPLBE expl) {
                            expl.activate(true);
                            break;
                        }
                    }
                }
                return;
            }
        }
    }

    private int getActualLaserDistance() {
        int max = Multiblocks.kugelblitzLaserDistance + 4;
        for (int i = 4; i <= max; i++) {
            if (level.getBlockEntity(worldPosition.relative(getFacing(), i)) instanceof PhotonConcentratorBE) {
                return i;
            }
        }
        return Multiblocks.kugelblitzLaserDistance;
    }

    private void transferEnergy() {
        if (energyTransfered) return;
        killEntitiesInBeam();
        energyTransfered = true;
        BlockPos pos = worldPosition.relative(getFacing(), getActualLaserDistance());
        if (level.getBlockEntity(pos) instanceof PhotonConcentratorBE pc) {
            pc.gotEnergy(getFacing());
        }
    }

    private void killEntitiesInBeam() {
        Direction facing = getFacing();
        int distance = getActualLaserDistance();
        for (int i = 1; i < distance; i++) {
            BlockPos pos = worldPosition.relative(facing, i);
            level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), e -> e != null && e.isAlive())
                    .forEach(e -> e.hurt(level.damageSources().magic(), 1000.0f));
        }
    }

    private void renderBeam() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        float thickness = (float) (aggregatedEnergy / (double) Multiblocks.kugelblitzExplCharge) * 0.5f;
        FusionBeamParticleData data = new FusionBeamParticleData(getFacing(), getActualLaserDistance() + 1, thickness);
        Vec3 vec = Vec3.atCenterOf(worldPosition.relative(getFacing()));
        for (ServerPlayer player : serverLevel.players()) {
            serverLevel.sendParticles(player, data, true, vec.x, vec.y, vec.z, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public void clientTick() {
        if (aggregatedEnergy > 0 && pulseTime > 88) {
            playSound(NCSounds.LASER_SHOOT.get(), 0.2f);
        }
        if (pulseTime < 20) {
            stopSound();
        }
    }
}
