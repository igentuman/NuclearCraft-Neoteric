package igentuman.nc.block_entity.catalyst;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Reference EFFICIENCY catalyst: on a tick where the host just finished an operation,
 * rolls a power-scaled chance to yield one bonus item into an existing output stack.
 */
public class EfficiencyCatalyst extends Catalyst {

    private static final float CHANCE_PER_POWER = 0.05f;
    private static final float MAX_CHANCE = 0.9f;

    public EfficiencyCatalyst(GlobalBlockEntity host) {
        super(CatalystType.EFFICIENCY, host);
    }

    @Override
    public void postTick() {
        if (!host.recipeInfo.justProduced) return;
        if (!host.contentHandler.hasItemCapability()) return;
        ModEntry entry = ModEntries.get(host.name);
        if (entry == null || entry.itemCap() == null) return;
        Level level = host.getLevel();
        if (level == null) return;

        float chance = Math.min(MAX_CHANCE, power * CHANCE_PER_POWER);
        if (level.getRandom().nextFloat() >= chance) return;

        var handler = host.contentHandler.getItemHandler();
        int start = entry.itemCap().inputSlots;
        int count = entry.itemCap().outputSlots;
        for (int i = 0; i < count; i++) {
            ItemStack stack = handler.getStackInSlot(start + i);
            if (!stack.isEmpty() && stack.getCount() < stack.getMaxStackSize()) {
                stack.grow(1);
                host.markDirty();
                return;
            }
        }
    }
}
