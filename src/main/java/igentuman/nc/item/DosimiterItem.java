package igentuman.nc.item;

import igentuman.nc.radiation.data.PlayerRadiation;
import igentuman.nc.radiation.data.PlayerRadiationProvider;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.radiation.data.WorldRadiation;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.setup.ModSetup.ITEM_GROUP;

public class DosimiterItem extends Item
{
	public DosimiterItem(Properties props)
	{
		this(props, ITEM_GROUP);
	}

	public DosimiterItem(Properties props, CreativeModeTab group)
	{
		super(props.tab(group).stacksTo(1));
	}

	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!world.isClientSide()) {
			PlayerRadiation radiationCap = player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).orElse(null);
			if(radiationCap == null) return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
			int radiation = radiationCap.getRadiation();
			player.sendSystemMessage(Component.translatable("message.nc.player_radiation_contamination", format(radiation)));
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
