package igentuman.nc.item;

import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.radiation.data.WorldRadiation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatRads;

public class GeigerCounterItem extends Item
{
	public GeigerCounterItem(Properties props)
	{
		super(props);
	}

    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if(pLevel.getGameTime() % 20 != 0 || pLevel.isClientSide() || currentTick < 50) return;
        if(!(pEntity instanceof Player)) return;

        Player player = (Player) pEntity;
        WorldRadiation worldRadiation = RadiationManager.get(pLevel).getWorldRadiation();
        int radiation = worldRadiation.getChunkRadiation(pEntity.chunkPosition().x, pEntity.chunkPosition().z);
        
        Component message = __("message.nc.geiger_radiation_measure", formatRads(radiation));
        player.displayClientMessage(message, true);
    }
}
