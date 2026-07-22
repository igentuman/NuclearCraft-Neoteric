package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.container.EngineersCrafterContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketCrafterFillGrid(BlockPos pos, ResourceLocation recipeId) implements CustomPacketPayload {

    public static final Type<PacketCrafterFillGrid> TYPE = new Type<>(NuclearCraft.rl("crafter_fill_grid"));

    public static final StreamCodec<FriendlyByteBuf, PacketCrafterFillGrid> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketCrafterFillGrid::pos,
                    ResourceLocation.STREAM_CODEC, PacketCrafterFillGrid::recipeId,
                    PacketCrafterFillGrid::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketCrafterFillGrid packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof EngineersCrafterContainer menu)) return;
            if (!menu.getBlockPos().equals(packet.pos())) return;
            if (!menu.stillValid(player)) return;

            Recipe<?> recipe = player.level().getRecipeManager().byKey(packet.recipeId())
                    .map(h -> h.value()).orElse(null);
            if (!(recipe instanceof CraftingRecipe craftingRecipe)) return;

            menu.fillCraftGrid(craftingRecipe, player);
            menu.broadcastChanges();
        });
    }
}
