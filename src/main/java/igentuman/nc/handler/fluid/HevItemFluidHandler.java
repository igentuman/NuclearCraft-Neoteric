package igentuman.nc.handler.fluid;

import igentuman.nc.item.HEVItem;
import igentuman.nc.setup.Registers;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;

/** Insert-only HEV quantite access, retaining the capability's one mB per QE conversion. */
public class HevItemFluidHandler extends ItemAccessResourceHandler<FluidResource> {
    private final Item validItem;
    private final FluidResource quantite;

    public HevItemFluidHandler(ItemAccess access, Fluid quantite) {
        super(access, 1);
        validItem = access.getResource().getItem();
        this.quantite = FluidResource.of(quantite);
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource resource, int index) {
        return getAmountFrom(resource, index) > 0 ? quantite : FluidResource.EMPTY;
    }

    @Override
    protected int getAmountFrom(ItemResource resource, int index) {
        return resource.is(validItem) ? resource.getOrDefault(Registers.HEV_QE_CHARGE.get(), 0) : 0;
    }

    @Override
    protected ItemResource update(ItemResource resource, int index, FluidResource fluid, int amount) {
        return resource.is(validItem) ? resource.with(Registers.HEV_QE_CHARGE.get(), amount) : ItemResource.EMPTY;
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return itemAccess.getResource().is(validItem) ? HEVItem.MAX_QE_CHARGE : 0;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return itemAccess.getResource().is(validItem) && !resource.isEmpty() && resource.getFluid() == quantite.getFluid();
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int count = itemAccess.getAmount();
        if (count == 0 || !isValid(index, resource)) return 0;
        var current = itemAccess.getResource();
        int stored = getAmountFrom(current, index);
        int insertedPerItem = Math.min(amount / count, HEVItem.MAX_QE_CHARGE - stored);
        if (insertedPerItem <= 0) return 0;
        return insertedPerItem * itemAccess.exchange(update(current, index, resource, stored + insertedPerItem),
                count, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return 0;
    }
}
