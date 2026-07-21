package igentuman.nc.block_entity.storage;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.setup.ModEntries;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractStorageBE extends GlobalBlockEntity {

    public static final ModelProperty<SideMode[]> SIDE_CONFIG = new ModelProperty<>();

    public final SideMode[] sideConfig = new SideMode[6];

    protected AbstractStorageBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
        for (int i = 0; i < 6; i++) sideConfig[i] = SideMode.DEFAULT;
    }

    public SideMode getSideMode(Direction direction) {
        return sideConfig[direction.ordinal()];
    }

    public SideMode toggleSideConfig(Direction direction) {
        sideConfig[direction.ordinal()] = sideConfig[direction.ordinal()].next();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
        return sideConfig[direction.ordinal()];
    }

    @NotNull
    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(SIDE_CONFIG, sideConfig.clone()).build();
    }

    /** Moves content to/from neighbours per {@link SideMode}. Returns true if anything changed. */
    protected abstract boolean transfer();

    /** Analog comparator strength 0-15 based on fill level. */
    public abstract int getComparatorSignal();

    @Override
    public void serverTick() {
        if (name != null && !ModEntries.isEnabled(name)) return;
        boolean changed = transfer();
        if (changed && level != null) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void clientTick() {
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        int[] modes = new int[6];
        for (int i = 0; i < 6; i++) modes[i] = sideConfig[i].ordinal();
        tag.putIntArray("sideConfig", modes);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("sideConfig")) {
            int[] modes = tag.getIntArray("sideConfig");
            for (int i = 0; i < 6 && i < modes.length; i++) {
                sideConfig[i] = SideMode.values()[modes[i]];
            }
            if (level != null && level.isClientSide) {
                requestModelDataUpdate();
                Minecraft.getInstance().levelRenderer.setSectionDirty(
                        SectionPos.blockToSectionCoord(worldPosition.getX()),
                        SectionPos.blockToSectionCoord(worldPosition.getY()),
                        SectionPos.blockToSectionCoord(worldPosition.getZ()));
            }
        }
    }
}
