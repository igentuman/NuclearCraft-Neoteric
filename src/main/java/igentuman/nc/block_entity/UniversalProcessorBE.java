package igentuman.nc.block_entity;

import igentuman.nc.block_entity.catalyst.Catalyst;
import igentuman.nc.block_entity.catalyst.CatalystDef;
import igentuman.nc.block_entity.catalyst.CatalystRegistry;
import igentuman.nc.block_entity.catalyst.CatalystType;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.caps.ItemCapDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class UniversalProcessorBE extends GlobalBlockEntity implements MenuProvider {

    private final Map<CatalystType, Catalyst> activeCatalysts = new EnumMap<>(CatalystType.class);

    public UniversalProcessorBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Override
    public void serverTick() {
        if (name != null && !ModEntries.isEnabled(name)) return;
        refreshCatalysts();
        recipeInfo.resetCatalystModifiers();
        for (Catalyst c : activeCatalysts.values()) c.preTick();
        super.serverTick();
        for (Catalyst c : activeCatalysts.values()) c.postTick();
    }

    /** Rebuilds {@link #activeCatalysts} from the per-type catalyst slots and refreshes each power. */
    private void refreshCatalysts() {
        ModEntry entry = ModEntries.get(name);
        if (entry == null || !entry.hasCatalysts()) return;
        if (!contentHandler.hasItemCapability()) return;
        ItemCapDefinition cap = entry.itemCap();
        if (cap == null) return;

        var handler = contentHandler.getItemHandler();
        Set<CatalystType> supported = entry.supportedCatalysts();
        int base = cap.inputSlots + cap.outputSlots + cap.globalSlots;
        int i = 0;
        for (CatalystType type : supported) {
            int slot = base + i;
            i++;
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                activeCatalysts.remove(type);
                continue;
            }
            CatalystDef def = findDef(type, stack);
            if (def == null) {
                activeCatalysts.remove(type);
                continue;
            }
            Catalyst catalyst = activeCatalysts.get(type);
            if (catalyst == null || catalyst.item != stack.getItem()) {
                catalyst = def.factory().create(this);
                catalyst.item = stack.getItem();
                activeCatalysts.put(type, catalyst);
            }
            catalyst.power = stack.getCount();
        }
    }

    private CatalystDef findDef(CatalystType type, ItemStack stack) {
        for (CatalystDef def : CatalystRegistry.byType(type)) {
            if (def.item().get() == stack.getItem()) return def;
        }
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.nc." + name);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new UniversalProcessorContainer(containerId, playerInventory, this, containerData);
    }
}
