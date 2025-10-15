package igentuman.nc.block.kugelblitz.entity;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.client.particle.FusionBeamParticleData;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.kugelblitz.KugelblitzMultiblock;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import mekanism.api.math.FloatingLong;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.handler.config.KugelblitzConfig.KUGELBLITZ_CONFIG;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.EXPL_BE;
import static igentuman.nc.setup.registration.NCSounds.LASER_SHOOT;
import static igentuman.nc.util.ModUtil.isBfrLoaded;
import static igentuman.nc.util.ModUtil.isMekanismGeneratorsLoaded;
import static net.minecraft.world.level.block.DirectionalBlock.FACING;

public class EXPLBE extends NuclearCraftBE {

    @NBTField
    public int inputRedstoneSignal = 0;
    @NBTField
    public boolean activated = false;
    @NBTField
    public int pulseTime = 0;
    @NBTField
    public long aggregatedEnergy = 0;
    public boolean activatedByOther = false;
    KugelblitzMultiblock chamber = null;

    protected final LazyOptional<IEnergyStorage> energy;
    public final CustomEnergyStorage energyStorage;
    private EXPLProxyBE[] proxyBES;
    private boolean energyTransfered = false;
    private boolean allLasersBurst = false;

    public EXPLBE(BlockPos pPos, BlockState pBlockState) {
        super(EXPL_BE.get(), pPos, pBlockState);
        energyStorage = createEnergy();
        energyStorage.setInputEnergyTier(GTCEU_CONFIG.KUGELBLITZ_ENERGY_TIER.get().ordinal());
        energyStorage.setOutputEnergyTier(GTCEU_CONFIG.KUGELBLITZ_ENERGY_TIER.get().ordinal());
        energy = LazyOptional.of(() -> energyStorage);
    }

    public EXPLBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        this(pPos, pBlockState);
    }

    protected void sendBeamData(FusionBeamParticleData data, BlockPos from) {
        Vec3 vec = Vec3.atCenterOf(from);
        if (!getLevel().isClientSide() && level instanceof ServerLevel serverWorld) {
            for (ServerPlayer player : serverWorld.players()) {
                serverWorld.sendParticles(player, data, true, vec.x, vec.y, vec.z, 1, 0, 0, 0, 0);
            }
        }
    }

    protected void renderBeam() {
        sendBeamData(new FusionBeamParticleData(getFacing(), getActualLaserDistance()+1, (float) (aggregatedEnergy/((double)KUGELBLITZ_CONFIG.EXPL_CHARGE.get()))*0.5f),
                getBlockPos().relative(getFacing())
        );
    }

    private Direction getFacing() {
        return getBlockState().getValue(FACING);
    }

    protected CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(2_048_000_000, 10000000, 0, true) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    protected EXPLProxyBE[] getProxies() {
        if(proxyBES == null) {
            //proxy block placement depends on facing
            Direction facing = getFacing();
            int minX = -1, maxX = 0, minY = -1, minZ = 2, maxY = 4, maxZ = 2;
            switch (facing) {
                case UP:
                    minX = -1; minY = 0; minZ = -1; maxX = 2;  maxY = 4; maxZ = 2;
                    break;
                case DOWN:
                    minX = -1; minY = -3; minZ = -1; maxX = 2; maxY = 1; maxZ = 2;
                    break;
                case NORTH:
                    minX = -1; minY = -1; minZ = -3; maxX = 2; maxY = 2; maxZ = 1;
                    break;
                case SOUTH:
                    minX = -1; minY = -1; minZ = 0; maxX = 2; maxY = 2; maxZ = 4;
                    break;
                case WEST:
                    minX = -3; minY = -1; minZ = -1; maxX = 1; maxY = 2; maxZ = 2;
                    break;
                case EAST:
                    minX = 0; minY = -1; minZ = -1; maxX = 4; maxY = 2; maxZ = 2;
                    break;
            }
            proxyBES = new EXPLProxyBE[35];
            int i = 0;

            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    for (int y = minY; y < maxY; y++) {
                        BlockEntity be = blockEntity(getBlockPos().offset(x, y, z));
                        if (be instanceof EXPLProxyBE explProxy) {
                            proxyBES[i] = explProxy;
                            i++;
                        }
                    }
                }
            }
        }
        return proxyBES;
    }

    private void tickProxyBlocks() {
        for(EXPLProxyBE proxy: getProxies()) {
            if(proxy == null) continue;
            proxy.forceTickServer(this);
        }
    }

    @Override
    public void tickServer() {
        //Disallow boosters like torcherino
        if(lastTickTime == level.getGameTime()) {
            return;
        }
        lastTickTime = level.getGameTime();
        energyStorage().tick();
        long wasCharge = aggregatedEnergy;
        if(!hasEnoughEnergy()) {
            aggregatedEnergy += energyStorage().getEnergyStored();
            aggregatedEnergy = Math.min(aggregatedEnergy, KUGELBLITZ_CONFIG.EXPL_CHARGE.get());
            energyStorage().setEnergy(0);
        }

        inputRedstoneSignal = 0;
        tickProxyBlocks();
        activated = activated || inputRedstoneSignal > 0;
        if (activated && hasEnoughEnergy()) {
            if (pulseTime == 0) {
                pulseTime = 90;
                if(activated && inputRedstoneSignal == 0 && !activatedByOther) {
                    findAndPulseOtherLasers();
                }
            }
        }
        if(pulseTime > 0) {
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_NEIGHBORS);
            pulseTime--;
            setChanged();
            if(pulseTime < 35) {
                renderBeam();
                aggregatedEnergy /= 2;
            }
            if(pulseTime < 33 && pulseTime > 30) {
                transferEnergy();
                activatedByOther = false;
            }
            if (pulseTime < 10) {
                aggregatedEnergy = 0;
                energyTransfered = false;
            }
        }
        if(activated && pulseTime < 1 || wasCharge != aggregatedEnergy) {
            activated = false;
            setChanged();
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_NEIGHBORS);
        }
    }

    private void findAndPulseOtherLasers() {
        int activatedLasers = 1;
        for (int i = 4; i <= KUGELBLITZ_CONFIG.LASER_DISTANCE.get()+4; i++) {
            BlockPos pos = getBlockPos().relative(getFacing(), i);
            BlockEntity be = level.getExistingBlockEntity(pos);
            if (be instanceof PhotonConcentratorBE photonConcentrator) {
                AbstractMultiblock multiblock = photonConcentrator.getMultiblock();
                if (multiblock instanceof KugelblitzMultiblock kugelblitzMultiblock) {
                    chamber = kugelblitzMultiblock;
                    BlockPos center = kugelblitzMultiblock.getCenter();
                    if(center == null) {
                        center = pos.relative(getFacing(), 5);
                    }
                    for(Direction direction : Direction.values()) {
                        if(direction == getFacing().getOpposite()) {
                            continue;
                        }
                        for(int j = 10; j < KUGELBLITZ_CONFIG.LASER_DISTANCE.get() + 20; j++) {
                            BlockEntity be2 = level.getExistingBlockEntity(center.relative(direction, j));
                            if(be2 instanceof EXPLBE expl) {
                                expl.activate(true);
                                activatedLasers++;
                                break;
                            }
                        }

                    }
                    break;
                }
                break;
            }
        }
        allLasersBurst = activatedLasers == 6 && chamber != null;
    }

    public void activate(boolean activatedByOther) {
        if(hasEnoughEnergy()) {
            if (pulseTime == 0) {
                pulseTime = 90;
                this.activatedByOther = true;
                activated = true;
                setChanged();
            }
        }
    }

    private int getActualLaserDistance() {
        for (int i = 4; i <= KUGELBLITZ_CONFIG.LASER_DISTANCE.get()+4; i++) {
            BlockPos pos = getBlockPos().relative(getFacing(), i);
            BlockEntity be = level.getExistingBlockEntity(pos);
            if (be instanceof PhotonConcentratorBE) {
                return i;
            }
            if (isMekanismGeneratorsLoaded() && be instanceof mekanism.generators.common.tile.fusion.TileEntityLaserFocusMatrix) {
                return i;
            }
            if (isBfrLoaded() && be instanceof igentuman.bfr.common.tile.fusion.TileEntityLaserFocusMatrix) {
                return i;
            }
        }
        return KUGELBLITZ_CONFIG.LASER_DISTANCE.get();
    }

    private void transferEnergy() {
        if (energyTransfered) return;
        killEntitiesInBeam();
        energyTransfered = true;
        BlockPos pos = getBlockPos().relative(getFacing(), getActualLaserDistance());
        BlockEntity be = level.getExistingBlockEntity(pos);
        if(allLasersBurst) {
            chamber.gotLaserBurst();
            allLasersBurst = false;
        }
        if (be instanceof PhotonConcentratorBE photonConcentratorBE) {
            photonConcentratorBE.gotEnergy(getFacing());
        }
        if (isMekanismGeneratorsLoaded() && be instanceof mekanism.generators.common.tile.fusion.TileEntityLaserFocusMatrix matrixBe) {
            matrixBe.receiveLaserEnergy(FloatingLong.create(aggregatedEnergy*5));
        }
        if (isBfrLoaded() && be instanceof igentuman.bfr.common.tile.fusion.TileEntityLaserFocusMatrix matrixBe) {
            matrixBe.receiveLaserEnergy(FloatingLong.create(aggregatedEnergy*5));
        }
    }

    private void killEntitiesInBeam() {
        Direction facing = getFacing();
        int distance = getActualLaserDistance();
        
        for (int i = 1; i < distance; i++) {
            BlockPos pos = getBlockPos().relative(facing, i);
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                new net.minecraft.world.phys.AABB(pos), 
                entity -> entity != null && entity.isAlive())
                .forEach(entity -> {
                    entity.hurt(level.damageSources().magic(), 1000.0f);
                });
        }
    }

    @Override
    public void tickClient() {
        if(aggregatedEnergy > 0 && pulseTime > 88) {
            playSound(LASER_SHOOT, 0.2f);
        }
        if(pulseTime < 20) {
            stopSound();
        }
    }

    @Override
    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }

    @Override
    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    public boolean hasEnoughEnergy() {
        return aggregatedEnergy >= KUGELBLITZ_CONFIG.EXPL_CHARGE.get();
    }
}
