package igentuman.nc.network.toServer;

import igentuman.api.platform.NCItemStacks;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketRecipeTransfer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketRecipeTransfer> TYPE =
        new CustomPacketPayload.Type<>(NuclearCraft.rl("recipe_transfer"));

    public static final StreamCodec<FriendlyByteBuf, PacketRecipeTransfer> STREAM_CODEC =
        StreamCodec.of((buf, pkt) -> pkt.encode(buf), PacketRecipeTransfer::decode);

    private BlockPos blockPos;
    private ResourceLocation recipeId;

    public PacketRecipeTransfer() {
    }

    public PacketRecipeTransfer(BlockPos blockPos, ResourceLocation recipeId) {
        this.blockPos = blockPos;
        this.recipeId = recipeId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketRecipeTransfer packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handlePacket(context));
    }

    private void handlePacket(IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player == null) {
            return;
        }

        // Check if player is within 5 blocks of the processor
        double distance = player.distanceToSqr(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
        if (distance > 25.0) { // 5 blocks squared = 25
            return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(blockPos);
        if (!(blockEntity instanceof NCProcessorBE processorBE)) {
            return;
        }

        // Check if player is looking at the processor (within reasonable angle)
        if (!isPlayerLookingAtBlock(player, blockPos)) {
            return;
        }

        // Find the recipe by ID — byKey returns Optional<RecipeHolder<?>> in 1.21.1
        NcRecipe ncRecipe = player.level().getRecipeManager().byKey(recipeId)
                .map(RecipeHolder::value)
                .filter(r -> r instanceof NcRecipe)
                .map(r -> (NcRecipe) r)
                .orElse(null);
        if (ncRecipe == null) {
            return;
        }

        // Check if player has the required ingredients and transfer them
        transferRecipeItems(player, processorBE, ncRecipe);
    }

    private void transferRecipeItems(ServerPlayer player, NCProcessorBE processorBE, NcRecipe recipe) {
        // Get the processor's item handler directly
        var processorItemHandler = processorBE.contentHandler().itemHandler;
        if (processorItemHandler == null) return;

        int inputSlotIndex = 0;
        int maxInputSlots = processorBE.prefab().getSlotsConfig().getInputItems();

        // Process each input ingredient
        for (var inputIngredient : recipe.getInputItems()) {
            if (inputSlotIndex >= maxInputSlots) break;
            if (inputIngredient == null || inputIngredient.getRepresentations().isEmpty()) {
                inputSlotIndex++;
                continue;
            }

            // Find the first matching item in player inventory
            for (ItemStack requiredStack : inputIngredient.getRepresentations()) {
                int playerSlotIndex = findItemSlotInPlayerInventory(player, requiredStack);
                if (playerSlotIndex != -1) {
                    ItemStack playerStack = player.getInventory().getItem(playerSlotIndex);

                    // Calculate how much we can transfer
                    int transferAmount = Math.min(requiredStack.getCount(), playerStack.getCount());

                    // Check if the processor slot can accept this item
                    ItemStack toTransfer = playerStack.copy();
                    toTransfer.setCount(transferAmount);

                    ItemStack currentInSlot = processorItemHandler.getStackInSlot(inputSlotIndex);

                    if (currentInSlot.isEmpty()) {
                        // Slot is empty, try to insert
                        ItemStack remainder = processorItemHandler.insertItem(inputSlotIndex, toTransfer, false);
                        int actualTransferred = transferAmount - remainder.getCount();

                        if (actualTransferred > 0) {
                            playerStack.shrink(actualTransferred);
                            if (playerStack.isEmpty()) {
                                player.getInventory().setItem(playerSlotIndex, ItemStack.EMPTY);
                            }
                            break;
                        }
                    } else if (NCItemStacks.canStack(currentInSlot, toTransfer)) {
                        // Same item, try to stack
                        int spaceLeft = currentInSlot.getMaxStackSize() - currentInSlot.getCount();
                        int actualTransfer = Math.min(transferAmount, spaceLeft);

                        if (actualTransfer > 0) {
                            ItemStack toInsert = toTransfer.copy();
                            toInsert.setCount(actualTransfer);
                            ItemStack remainder = processorItemHandler.insertItem(inputSlotIndex, toInsert, false);
                            int actualTransferred = actualTransfer - remainder.getCount();

                            if (actualTransferred > 0) {
                                playerStack.shrink(actualTransferred);
                                if (playerStack.isEmpty()) {
                                    player.getInventory().setItem(playerSlotIndex, ItemStack.EMPTY);
                                }
                                break;
                            }
                        }
                    }
                }
            }
            inputSlotIndex++;
        }

        // Mark the processor as changed to sync with clients
        processorBE.setChanged();
    }

    private int findItemSlotInPlayerInventory(ServerPlayer player, ItemStack required) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (NCItemStacks.canStack(stack, required) && stack.getCount() >= required.getCount()) {
                return i;
            }
        }
        return -1;
    }

    private boolean isPlayerLookingAtBlock(ServerPlayer player, BlockPos blockPos) {
        // Get player's look vector
        var lookVec = player.getLookAngle();

        // Get vector from player's eye position to block center
        var playerEyePos = player.getEyePosition();
        var blockCenter = blockPos.getCenter();
        var toBlock = blockCenter.subtract(playerEyePos).normalize();

        // Calculate dot product to determine angle
        double dotProduct = lookVec.dot(toBlock);

        // Allow for a reasonable viewing angle (about 45 degrees = cos(45) ~ 0.707)
        return dotProduct > 0.5;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeResourceLocation(recipeId);
    }

    public static PacketRecipeTransfer decode(FriendlyByteBuf buffer) {
        PacketRecipeTransfer packet = new PacketRecipeTransfer();
        packet.blockPos = buffer.readBlockPos();
        packet.recipeId = buffer.readResourceLocation();
        return packet;
    }
}
