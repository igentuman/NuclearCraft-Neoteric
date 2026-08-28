package igentuman.nc.compat.curios;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;

import static igentuman.nc.handler.event.ServerEvents.accumulateCrystal;

public class CuriosHelper {

    public static void accumulateCurios(Map<Holder<MobEffect>, Integer> strongest, Player player) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            Map<String, ICurioStacksHandler> stacks = handler.getCurios();
            for (ICurioStacksHandler stackHandler : stacks.values()) {
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStacks().getStackInSlot(i);
                    accumulateCrystal(strongest, stack);
                }
            }
        });
    }
}
