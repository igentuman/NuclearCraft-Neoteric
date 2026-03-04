package igentuman.nc.block.storage.entity;

import igentuman.api.platform.NCNames;
import igentuman.api.nc.SideModeToggleable;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.content.energy.BatteryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static igentuman.nc.compat.gregtech.GTUtils.getGTEnergy;
import static igentuman.nc.compat.gregtech.GTUtils.isOnlyGTCEUCapEnabled;
import static igentuman.nc.handler.config.CommonConfig.ENERGY_STORAGE;
import static igentuman.nc.util.ModUtil.isGtLoaded;

public class BatteryBE extends NCEnergy {

    public static final ModelProperty<HashMap<Integer, SideModeToggleable.SideMode>> SIDE_CONFIG = new ModelProperty<>();
    private int chargeCooldown = 0;

    public BatteryBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
        for (Direction direction : Direction.values()) {
            sideConfig.put(direction.ordinal(), SideModeToggleable.SideMode.DEFAULT);
        }
    }

    public static String getName(BlockState pBlockState) {
        return NCNames.of(pBlockState.getBlock().asItem());
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(SIDE_CONFIG, sideConfig)
                .build();
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        super.tickServer();
        transferEnergy();
        if(chargeCooldown > 0) chargeCooldown--;
    }

    /**
     * Push/pull energy to adjacent blocks
     */
    protected void transferEnergy() {
        for (Direction direction : Direction.values()) {
            switch (sideConfig.get(direction.ordinal())) {
                case OUT -> transferEnergyToSide(direction);
                case IN -> pullEnergyFromSide(direction);
            }
        }
    }




    @Override
    protected int getEnergyTransferPerTick() {
        return Math.min(BatteryBlocks.all().get(getName()).getStorage(), energyStorage().getEnergyStored());
    }

    public int getMaxTransfer() {
        return getEnergyMaxStorage();
    }

    @Override
    public long getInputEnergyTier() {
        return BatteryBlocks.all().get(NCNames.of(getBlockState().getBlock().asItem())).getEnergyTier().ordinal();
    }

    @Override
    public long getOutputEnergyTier() {
        return BatteryBlocks.all().get(NCNames.of(getBlockState().getBlock().asItem())).getEnergyTier().ordinal();
    }

    protected int getEnergyMaxStorage() {
        return BatteryBlocks.all().get(NCNames.of(getBlockState().getBlock().asItem())).getStorage();
    }


    @Override
    protected void saveClientData(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveClientData(tag, registries);
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    @Override
    public void loadClientData(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadClientData(tag, registries);
        if (!tag.contains("sideConfig")) return;
        loadSideConfig(tag.getIntArray("sideConfig"));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if(!tag.contains("sideConfig")) return;
        loadSideConfig(tag.getIntArray("sideConfig"));
    }

    private void loadSideConfig(int[] tagData) {
        boolean changed = false;
        for (int i = 0; i < sideConfig.size(); i++) {
            SideModeToggleable.SideMode newMode = SideModeToggleable.SideMode.values()[tagData[i]];
            if(sideConfig.get(i) != newMode) {
                changed = true;
                sideConfig.remove(i);
                sideConfig.put(i, newMode);
            }
        }
        if(changed) {
            requestModelDataUpdate();
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    public SideModeToggleable.SideMode toggleSideConfig(int direction) {
        sideConfig.put(direction, SideModeToggleable.SideMode.values()[(sideConfig.get(direction).ordinal() + 1) % 4]);
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        return sideConfig.get(direction);
    }

    public void onLightningStrike() {
        if(chargeCooldown > 0) return;
        chargeCooldown = 600;
        energyStorage.addEnergy(ENERGY_STORAGE.LIGHTNING_ROD_CHARGE.get());
        setChanged();
        BlockPos pos = worldPosition;
        Direction direction = Direction.UP;
        Direction.Axis direction$axis = direction.getAxis();
        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY();
        double d2 = (double)pos.getZ() + 0.5D;
        double d3 = 0.52D;
        double d4 = level.getRandom().nextDouble() * 0.6D - 0.3D;
        double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52D : d4;
        double d6 = level.getRandom().nextDouble() * 6.0D / 16.0D;
        double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52D : d4;
        level.addParticle(DustParticleOptions.REDSTONE, d0 + d5, d1 + d6, d2 + d7, 0, 0, 0);
    }
}
