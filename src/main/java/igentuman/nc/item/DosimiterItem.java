package igentuman.nc.item;

import igentuman.nc.radiation.data.PlayerRadiation;
import igentuman.nc.radiation.data.PlayerRadiationProvider;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.UUID;

public class DosimiterItem extends Item
{
	public DosimiterItem(Properties props)
	{
		super(props);
	}

/*
	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(@NotNull World world, PlayerEntity player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!world.isClientSide()) {
			PlayerRadiation radiationCap = player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).orElse(null);
			if(radiationCap == null) return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
			int radiation = radiationCap.getRadiation();
			player.sendMessage(new TranslationTextComponent("message.nc.player_radiation_contamination", format(radiation)),  UUID.randomUUID());
			CriteriaTriggers.USING_ITEM.trigger((ServerPlayer) player, stack);
		}
		return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
	}*/

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
