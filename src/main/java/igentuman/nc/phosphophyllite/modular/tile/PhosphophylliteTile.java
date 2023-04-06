package igentuman.nc.phosphophyllite.modular.tile;

import igentuman.nc.phosphophyllite.modular.api.IModularTile;
import igentuman.nc.phosphophyllite.modular.api.ModuleRegistry;
import igentuman.nc.phosphophyllite.modular.api.TileModule;
import igentuman.nc.util.annotation.NonnullDefault;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@NonnullDefault
public class PhosphophylliteTile extends BlockEntity implements IModularTile {
    
    public static final Logger MODULE_LOGGER = LogManager.getLogger("Phosphophyllite/ModularTile");
    @Deprecated(forRemoval = true)
    public static final Logger LOGGER = MODULE_LOGGER;
    
    boolean removed = false;
    private final Object2ObjectOpenHashMap<Class<?>, TileModule<?>> modules = new Object2ObjectOpenHashMap<>();
    private final ArrayList<TileModule<?>> moduleList = new ArrayList<>();
    private final List<TileModule<?>> moduleListRO = Collections.unmodifiableList(moduleList);
    
    public PhosphophylliteTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
        Class<?> thisClazz = this.getClass();
        ModuleRegistry.forEachTileModule((clazz, constructor) -> {
            if (clazz.isAssignableFrom(thisClazz)) {
                TileModule<?> module = constructor.apply(this);
                modules.put(clazz, module);
                moduleList.add(module);
            }
        });
        moduleList.forEach(TileModule::postModuleConstruction);
    }
    
    @Nullable
    public TileModule<?> module(Class<?> interfaceClazz) {
        return modules.get(interfaceClazz);
    }
    
    @Override
    public List<TileModule<?>> modules() {
        return moduleListRO;
    }
    
    @Override
    public final void clearRemoved() {
        super.clearRemoved();
    }
    
    private static final Object2ObjectOpenHashMap<Level, ObjectArrayList<PhosphophylliteTile>> clientWorldUnloadEventTiles = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<Level, ObjectArrayList<PhosphophylliteTile>> serverWorldUnloadEventTiles = new Object2ObjectOpenHashMap<>();
    private int index = 0;
    
    static {
        MinecraftForge.EVENT_BUS.addListener(PhosphophylliteTile::serverStopEvent);
        MinecraftForge.EVENT_BUS.addListener(PhosphophylliteTile::worldUnloadEvent);
    }
    
    private static void worldUnloadEvent(LevelEvent.Unload unload) {
        var removed = (unload.getLevel().isClientSide() ? clientWorldUnloadEventTiles : serverWorldUnloadEventTiles).remove((Level) unload.getLevel());
        if (removed != null) {
            for (int i = 0; i < removed.size(); i++) {
                removed.get(i).remove(true);
            }
        }
    }
    
    private static void serverStopEvent(ServerStoppedEvent stoppedEvent) {
        serverWorldUnloadEventTiles.clear();
    }
    
    @Override
    public final void onLoad() {
        assert level != null;
        super.onLoad();
        moduleList.forEach(TileModule::onAdded);
        onAdded();
        var worldUnloadTiles = (level.isClientSide() ? clientWorldUnloadEventTiles : serverWorldUnloadEventTiles).computeIfAbsent(level, __ -> new ObjectArrayList<>());
        index = worldUnloadTiles.size();
        worldUnloadTiles.add(this);
    }
    
    public void onAdded() {
    }
    
    @Override
    public final void setRemoved() {
        super.setRemoved();
        remove(false);
    }
    
    @Override
    public final void onChunkUnloaded() {
        super.onChunkUnloaded();
        remove(true);
    }
    
    private void remove(boolean chunkUnload) {
        if (removed) {
            return;
        }
        assert level != null;
        onRemoved(chunkUnload);
        moduleList.forEach(module -> module.onRemoved(chunkUnload));
        removed = true;
        if (index == -1) {
            return;
        }
        var worldUnloadTiles = (level.isClientSide() ? clientWorldUnloadEventTiles : serverWorldUnloadEventTiles).get(level);
        if (worldUnloadTiles != null) {
            var arrayTile = worldUnloadTiles.get(index);
            if (arrayTile == this) {
                var removed = worldUnloadTiles.pop();
                if (removed != this) {
                    removed.index = index;
                    worldUnloadTiles.set(index, removed);
                }
                this.index = -1;
            }
        }
    }
    
    public void onRemoved(@SuppressWarnings("unused") boolean chunkUnload) {
    }
    
    @Override
    public final void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("local")) {
            CompoundTag local = compound.getCompound("local");
            readNBT(local);
        }
        CompoundTag subNBTs = compound.getCompound("sub");
        for (var module : moduleList) {
            String key = module.saveKey();
            if (key != null && subNBTs.contains(key)) {
                CompoundTag nbt = subNBTs.getCompound(key);
                module.readNBT(nbt);
            }
        }
    }
    
    @Nullable
    private CompoundTag subNBTs(Function<TileModule<?>, CompoundTag> nbtSupplier) {
        CompoundTag subNBTs = new CompoundTag();
        for (var module : moduleList) {
            CompoundTag nbt = nbtSupplier.apply(module);
            if (nbt != null) {
                String key = module.saveKey();
                if (key != null) {
                    if (subNBTs.contains(key)) {
                        MODULE_LOGGER.warn("Multiple modules with the same save key \"" + key + "\" for tile type \"" + getClass().getSimpleName() + "\" at " + getBlockPos());
                    }
                    subNBTs.put(key, nbt);
                }
            }
        }
        if (subNBTs.isEmpty()) {
            return null;
        }
        return subNBTs;
    }
    
    @Override
    public final void saveAdditional(CompoundTag nbt) {
        CompoundTag subNBTs = subNBTs(TileModule::writeNBT);
        if (subNBTs != null) {
            nbt.put("sub", subNBTs);
        }
        
        CompoundTag localNBT = writeNBT();
        if (!localNBT.isEmpty()) {
            nbt.put("local", localNBT);
        }
    }
    
    protected void readNBT(CompoundTag compound) {
    }
    
    protected CompoundTag writeNBT() {
        return new CompoundTag();
    }
    
    private static final CompoundTag EMPTY_TAG = new CompoundTag();
    
    @Override
    public final void handleUpdateTag(CompoundTag compound) {
        super.handleUpdateTag(compound);
        if (compound.contains("local")) {
            CompoundTag local = compound.getCompound("local");
            handleDataNBT(local);
        }
        CompoundTag subNBTs = compound.getCompound("sub");
        for (var module : moduleList) {
            String key = module.saveKey();
            if (key != null) {
                CompoundTag nbt = EMPTY_TAG;
                if (subNBTs.contains(key)) {
                    nbt = subNBTs.getCompound(key);
                }
                module.handleDataNBT(nbt);
                if (nbt == EMPTY_TAG && !nbt.isEmpty()) {
                    MODULE_LOGGER.warn("Module " + key + " wrote to NBT in read!");
                    for (var str : EMPTY_TAG.getAllKeys().toArray(new String[0])) {
                        EMPTY_TAG.remove(str);
                    }
                }
            }
        }
    }
    
    @Override
    public final CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        CompoundTag subNBTs = subNBTs(TileModule::getDataNBT);
        if (subNBTs != null) {
            nbt.put("sub", subNBTs);
        }
        CompoundTag localNBT = getDataNBT();
        if (!localNBT.isEmpty()) {
            nbt.put("local", localNBT);
        }
        return nbt;
    }
    
    protected void handleDataNBT(CompoundTag nbt) {
        // mimmicks behavior of IForgeTileEntity
        readNBT(nbt);
    }
    
    protected CompoundTag getDataNBT() {
        return writeNBT();
    }
    
    @Override
    public final void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        assert level != null;
        // getters are client only, so, cant grab it on the server even if i want to
        if (!level.isClientSide) {
            return;
        }
        CompoundTag compound = pkt.getTag();
        if (compound.contains("local")) {
            CompoundTag local = compound.getCompound("local");
            handleUpdateNBT(local);
        }
        CompoundTag subNBTs = compound.getCompound("sub");
        for (var module : moduleList) {
            String key = module.saveKey();
            if (key != null && subNBTs.contains(key)) {
                CompoundTag nbt = subNBTs.getCompound(key);
                module.handleUpdateNBT(nbt);
            }
        }
    }
    
    @Nullable
    @Override
    public final ClientboundBlockEntityDataPacket getUpdatePacket() {
        boolean sendPacket = false;
        CompoundTag subNBTs = subNBTs(TileModule::getUpdateNBT);
        CompoundTag nbt = new CompoundTag();
        if (subNBTs != null) {
            nbt.put("sub", subNBTs);
        }
        CompoundTag localNBT = getUpdateNBT();
        if (localNBT != null) {
            sendPacket = true;
            nbt.put("local", localNBT);
        }
        if (!sendPacket) {
            return null;
        }
        return ClientboundBlockEntityDataPacket.create(this, e -> nbt);
    }
    
    protected void handleUpdateNBT(@SuppressWarnings("unused") CompoundTag nbt) {
    }
    
    @Nullable
    protected CompoundTag getUpdateNBT() {
        return null;
    }
    
    @Nonnull
    public final <T> LazyOptional<T> getCapability(final Capability<T> cap, final @Nullable Direction side) {
        var optional = capability(cap, side);
        for (var module : moduleList) {
            var moduleOptional = module.capability(cap, side);
            if (moduleOptional.isPresent()) {
                if (optional.isPresent()) {
                    MODULE_LOGGER.warn("Multiple implementations of same capability \"" + cap.getName() + "\" on " + side + " side for tile type \"" + getClass().getSimpleName() + "\" at " + getBlockPos());
                    continue;
                }
                optional = moduleOptional;
            }
        }
        return optional;
    }
    
    @Nonnull
    @Override
    public final <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        return getCapability(cap, null);
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
    protected <T> LazyOptional<T> capability(final Capability<T> cap, final @Nullable Direction side) {
        return LazyOptional.empty();
    }

}
