package igentuman.nc.block.entity.kugelblitz;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.util.CustomEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.EXPL_BE;

public class EXPLBE extends NuclearCraftBE {

    public int energyPerTick;
    protected final LazyOptional<IEnergyStorage> energy;
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;

    public EXPLBE(BlockPos pPos, BlockState pBlockState) {
        super(EXPL_BE.get(), pPos, pBlockState);
        energyStorage = createEnergy();
        energy = LazyOptional.of(() -> energyStorage);
        contentHandler = new SidedContentHandler(
                0, 0,
                1, 1, 10, 10);
        contentHandler().setBlockEntity(this);
        contentHandler().fluidCapability.setGlobalMode(0, SlotModePair.SlotMode.INPUT);
        contentHandler().fluidCapability.setGlobalMode(1, SlotModePair.SlotMode.OUTPUT);
    }

    public EXPLBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        this(pPos, pBlockState);
    }

    protected CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(2_048_000_000, 5000000, 0) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    @Override
    public SidedContentHandler contentHandler() {
        return contentHandler;
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
