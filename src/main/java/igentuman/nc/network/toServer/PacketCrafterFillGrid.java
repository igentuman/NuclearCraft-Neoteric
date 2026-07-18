package igentuman.nc.network.toServer;

import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.network.NetworkEvent;

/**
 * EMI recipe transfer for the engineer's crafting terminal. The recipe is looked up server-side by id
 * and its ingredients are pulled from the player inventory and the crafter's inserted containers into
 * the 3x3 matrix. Only the recipe id crosses the wire, so a spoofed packet cannot fabricate items.
 */
public class PacketCrafterFillGrid implements INcPacket {

    private BlockPos pos;
    private ResourceLocation recipeId;

    public PacketCrafterFillGrid() {
    }

    public PacketCrafterFillGrid(BlockPos pos, ResourceLocation recipeId) {
        this.pos = pos;
        this.recipeId = recipeId;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        if (!(player.containerMenu instanceof EngineersCrafterContainer menu)) return;
        if (!menu.getBlockPos().equals(pos)) return;
        if (!menu.stillValid(player)) return;

        Recipe<?> recipe = player.level.getRecipeManager().byKey(recipeId).orElse(null);
        if (!(recipe instanceof CraftingRecipe craftingRecipe)) return;

        menu.fillCraftGrid(craftingRecipe, player);
        menu.broadcastChanges();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeResourceLocation(recipeId);
    }

    public static PacketCrafterFillGrid decode(FriendlyByteBuf buffer) {
        PacketCrafterFillGrid packet = new PacketCrafterFillGrid();
        packet.pos = buffer.readBlockPos();
        packet.recipeId = buffer.readResourceLocation();
        return packet;
    }
}
