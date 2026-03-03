package igentuman.nc.item;

import igentuman.nc.radiation.data.PlayerRadiation;
import igentuman.nc.setup.registration.NCAttachments;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatRads;

public class DosimiterItem extends Item
{
	public DosimiterItem(Properties props)
	{
		super(props);
	}

	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!world.isClientSide()) {
			PlayerRadiation radiationCap = player.getData(NCAttachments.PLAYER_RADIATION.get());
			long radiation = radiationCap.getRadiation();
			player.sendSystemMessage(__("message.nc.player_radiation_contamination", formatRads(radiation)));
			CriteriaTriggers.USING_ITEM.trigger((ServerPlayer) player, stack);
		}
		return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
	}
}
