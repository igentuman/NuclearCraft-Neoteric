package igentuman.nc.client.gui.fission.designer;

import igentuman.nc.block.fission.FissionDesignerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientFissionDesignCache {

    private record Key(ResourceLocation dimension, BlockPos pos) {}

    private static final Map<Key, CompoundTag> CACHE = new HashMap<>();
    private static int tickCounter = 0;

    private ClientFissionDesignCache() {}

    private static Key key(BlockPos pos) {
        return new Key(Minecraft.getInstance().level.dimension().location(), pos);
    }

    public static CompoundTag get(BlockPos pos) {
        if (Minecraft.getInstance().level == null) return null;
        return CACHE.get(key(pos));
    }

    public static void put(BlockPos pos, CompoundTag tag) {
        if (Minecraft.getInstance().level == null || tag == null) return;
        CACHE.put(key(pos), tag);
    }

    public static void remove(BlockPos pos) {
        if (Minecraft.getInstance().level == null) return;
        CACHE.remove(key(pos));
    }

    public static void clear() {
        CACHE.clear();
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (++tickCounter < 20) return;
        tickCounter = 0;
        ResourceLocation dim = mc.level.dimension().location();
        Iterator<Map.Entry<Key, CompoundTag>> it = CACHE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Key, CompoundTag> e = it.next();
            Key k = e.getKey();
            if (!k.dimension().equals(dim)) continue;
            BlockPos p = k.pos();
            if (!mc.level.hasChunkAt(p)) continue;
            if (!(mc.level.getBlockState(p).getBlock() instanceof FissionDesignerBlock)) {
                it.remove();
            }
        }
    }

    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
