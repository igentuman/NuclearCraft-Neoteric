package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server: load a recipe into the encoder (grid recipe into the ghost grid, or a processor recipe). */
public record PacketEncoderFillGrid(BlockPos pos, ResourceLocation recipeId) implements CustomPacketPayload {

    public static final Type<PacketEncoderFillGrid> TYPE = new Type<>(NuclearCraft.rl("encoder_fill_grid"));

    public static final StreamCodec<FriendlyByteBuf, PacketEncoderFillGrid> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketEncoderFillGrid::pos,
                    ResourceLocation.STREAM_CODEC, PacketEncoderFillGrid::recipeId,
                    PacketEncoderFillGrid::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    public static void handle(PacketEncoderFillGrid packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof EngineersEncoderContainer menu)) return;
            if (!menu.getBlockPos().equals(packet.pos())) return;
            if (!menu.stillValid(player)) return;

            RecipeHolder<?> holder = player.level().getRecipeManager().byKey(packet.recipeId()).orElse(null);
            if (holder == null) return;
            if (holder.value() instanceof CraftingRecipe craftingRecipe) {
                menu.fillGhostGrid(craftingRecipe);
            } else if (holder.value() instanceof UniversalProcessorRecipe upr && upr.isComplete()) {
                menu.setProcessingRecipe((RecipeHolder<UniversalProcessorRecipe>) holder);
            } else {
                return;
            }
            menu.broadcastChanges();
        });
    }
}
