package igentuman.nc.item;

import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.radiation.data.WorldRadiation;
import igentuman.nc.radiation.data.WorldRadiationProvider;
import igentuman.nc.setup.registration.CreativeTabs;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GeigerCounterItem extends Item
{
	public GeigerCounterItem(Properties props)
	{
		super(props);
	}

	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!world.isClientSide()) {
			WorldRadiation worldRadiation = RadiationManager.get(world).getWorldRadiation();
			int radiation = worldRadiation.getChunkRadiation(player.chunkPosition().x, player.chunkPosition().z);
			player.sendMessage(new TranslatableComponent("message.nc.geiger_radiation_measure", format(radiation)), UUID.randomUUID());
			CriteriaTriggers.USING_ITEM.trigger((ServerPlayer) player, stack);
		}
		return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
	}

	private static String format(int radiation) {
		if(radiation >= 1000000) {
			return String.format("%.2f", (float)radiation/1000000)+" Rad";
		}
		if(radiation >= 1000) {
			return String.format("%.2f", (float)radiation/1000)+" mRad";
		}
		return String.format("%.2f", (float)radiation)+" uRad";
	}
}
