package igentuman.nc.block.entity.kugelblitz;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.block.entity.fusion.FusionCoreProxyBE;
import igentuman.nc.client.particle.FusionBeamParticleData;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.kugelblitz.KugelblitzMultiblock;
import igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.NCBlockPos;
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
import net.minecraftforge.registries.ForgeRegistries;

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.handler.config.KugelblitzConfig.KUGELBLITZ_CONFIG;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.EXPL_BE;
import static igentuman.nc.setup.registration.NCSounds.LASER_SHOOT;
import static igentuman.nc.util.ModUtil.isMekanismLoaded;
import static net.minecraft.world.level.block.DirectionalBlock.FACING;

public class EXPLBE extends NuclearCraftBE {

    @NBTField
    public int pulseEnergy;
    @NBTField
    public int inputRedstoneSignal = 0;
    @NBTField
    public boolean activated = false;
    @NBTField
    public int energyPerTick;
    @NBTField
    public int pulseTime = 0;

    protected final LazyOptional<IEnergyStorage> energy;
    public final CustomEnergyStorage energyStorage;
    private EXPLProxyBE[] proxyBES;
    private boolean energyTransfered = false;

    public EXPLBE(BlockPos pPos, BlockState pBlockState) {
        super(EXPL_BE.get(), pPos, pBlockState);
        energyStorage = createEnergy();
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
        int beamLength = 12;
        sendBeamData(new FusionBeamParticleData(getFacing(), beamLength, energyStorage().getEnergyStored()/(float)energyStorage().getMaxEnergyStored()*0.5f),
                getBlockPos().relative(getFacing())
        );
    }

    private Direction getFacing() {
        return getBlockState().getValue(FACING);
    }

    protected CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(2_048_000_000, 1000000, 0, true) {
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
        energyStorage().tick();
        inputRedstoneSignal = 0;
        tickProxyBlocks();
        activated = activated || inputRedstoneSignal > 0;
        pulseEnergy = energyStorage().getEnergyStored();
        if (activated && pulseEnergy == energyStorage().getMaxEnergyStored()) {
            if (pulseTime == 0) {
                pulseTime = 90;
            }
        }
        if(pulseTime > 0) {
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_NEIGHBORS);
            pulseTime--;
            setChanged();
            if(pulseTime < 35) {
                renderBeam();
                energyStorage().setEnergy(energyStorage().getEnergyStored()/2);
            }
            if(pulseTime < 33 && pulseTime > 30) {
                transferEnergy();
            }
            if (pulseTime < 10) {
                energyStorage().setEnergy(0);
                energyTransfered = false;
            }
        }
        if(activated && pulseTime < 1) {
            activated = false;
            setChanged();
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_NEIGHBORS);
        }
    }

    private void transferEnergy() {
        if (energyTransfered) return;
        energyTransfered = true;
        for (int i = 4; i <= KUGELBLITZ_CONFIG.LASER_DISTANCE.get()+4; i++) {
            BlockPos pos = getBlockPos().relative(getFacing(), i);
            BlockEntity be = level.getExistingBlockEntity(pos);
            if (be instanceof PhotonConcentratorBE photonConcentrator) {
                AbstractNCMultiblock multiblock = photonConcentrator.getMultiblock();
                if (multiblock instanceof KugelblitzMultiblock kugelblitzMultiblock) {
                    kugelblitzMultiblock.addPulseEnergy(pulseEnergy, getFacing());
                    break;
                }
            }
            if (isMekanismLoaded() && be instanceof mekanism.generators.common.tile.fusion.TileEntityLaserFocusMatrix matrixBe) {
                matrixBe.receiveLaserEnergy(FloatingLong.create(pulseEnergy));
                break;
            }
        }
    }

    @Override
    public void tickClient() {
        if(pulseEnergy > 0 && pulseTime > 88) {
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
}
