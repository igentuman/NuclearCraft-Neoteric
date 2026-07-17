package igentuman.nc.compat.curios;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Map;

import static igentuman.nc.handler.event.server.CrystalBuffEvents.accumulate;

public class CuriosHelper {

    // Isolated so the CuriosApi reference only links when the mod is present (see onPlayerTick guard).
    public static void accumulateCurios(Map<MobEffect, Integer> strongest, Player player) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable equipped = handler.getEquippedCurios();
            for (int i = 0; i < equipped.getSlots(); i++) {
                accumulate(strongest, equipped.getStackInSlot(i));
            }
        });
    }
}
