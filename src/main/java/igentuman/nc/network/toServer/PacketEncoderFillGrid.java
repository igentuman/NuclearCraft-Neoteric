package igentuman.nc.network.toServer;

import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.network.NetworkEvent;

/**
 * EMI recipe transfer for the engineer's pattern encoder. Lays the recipe into the ghost matrix as a
 * template; no items are moved or required, so the encoder can template recipes the player cannot yet
 * craft.
 */
public class PacketEncoderFillGrid implements INcPacket {

    private BlockPos pos;
    private ResourceLocation recipeId;

    public PacketEncoderFillGrid() {
    }

    public PacketEncoderFillGrid(BlockPos pos, ResourceLocation recipeId) {
        this.pos = pos;
        this.recipeId = recipeId;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        if (!(player.containerMenu instanceof EngineersEncoderContainer menu)) return;
        if (!menu.getBlockPos().equals(pos)) return;
        if (!menu.stillValid(player)) return;

        Recipe<?> recipe = player.level().getRecipeManager().byKey(recipeId).orElse(null);
        if (!(recipe instanceof CraftingRecipe craftingRecipe)) return;

        menu.fillGhostGrid(craftingRecipe);
        menu.broadcastChanges();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeResourceLocation(recipeId);
    }

    public static PacketEncoderFillGrid decode(FriendlyByteBuf buffer) {
        PacketEncoderFillGrid packet = new PacketEncoderFillGrid();
        packet.pos = buffer.readBlockPos();
        packet.recipeId = buffer.readResourceLocation();
        return packet;
    }
}
