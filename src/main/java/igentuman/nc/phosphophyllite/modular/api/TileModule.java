package igentuman.nc.phosphophyllite.modular.api;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TileModule<InterfaceType extends IModularTile>  {
    
    @Nonnull
    public final InterfaceType iface;
    
    public TileModule(IModularTile iface) {
        //noinspection unchecked
        this.iface = (InterfaceType) iface;
    }
    
    public void postModuleConstruction() {
    }
    
    
    public void onAdded() {
    }
    
    public void onRemoved(boolean chunkUnload) {
    }
    
    /**
     * coped from ICapabilityProvider
     * <p>
     * Retrieves the Optional handler for the capability requested on the specific side.
     * The return value <strong>CAN</strong> be the same for multiple faces.
     * Modders are encouraged to cache this value, using the listener capabilities of the Optional to
     * be notified if the requested capability get lost.
     *
     * @param cap  The capability to check
     * @param side The Side to check from,
     *             <strong>CAN BE NULL</strong>. Null is defined to represent 'internal' or 'self'
     * @return The requested an optional holding the requested capability.
     */
    public <T> LazyOptional<T> capability(final Capability<T> cap, final @Nullable Direction side) {
        return LazyOptional.empty();
    }
    
    @Nullable
    public String saveKey() {
        return null;
    }
    
    /**
     * Standard world save NBT
     *
     * @param nbt
     */
    public void readNBT(CompoundTag nbt) {
    }
    
    @Nullable
    public CompoundTag writeNBT() {
        return null;
    }
    
    /**
     * Initial server -> client sync on client side chunk load
     *
     * @param nbt
     */
    public void handleDataNBT(CompoundTag nbt) {
        // mimmicks behavior of IForgeTileEntity
        readNBT(nbt);
    }
    
    @Nullable
    public CompoundTag getDataNBT() {
        // mimmicks behavior of IForgeTileEntity
        return writeNBT();
    }
    
    /**
     * Updates while chunk is loaded
     *
     * @param nbt
     */
    public void handleUpdateNBT(CompoundTag nbt) {
    }
    
    @Nullable
    public CompoundTag getUpdateNBT() {
        return null;
    }

}