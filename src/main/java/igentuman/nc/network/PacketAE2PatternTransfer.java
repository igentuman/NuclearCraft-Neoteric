package igentuman.nc.network;

import appeng.api.stacks.GenericStack;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.util.ConfigInventory;
import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.ProcessorRecipeStacks;
import igentuman.nc.recipe.RecipeSources;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.stream.Stream;

/** Loads a registered processor recipe into the current AE2 pattern terminal. */
public record PacketAE2PatternTransfer(int containerId, Identifier recipeId) implements CustomPacketPayload {

    public static final Type<PacketAE2PatternTransfer> TYPE = new Type<>(NuclearCraft.rl("ae2_pattern_transfer"));

    public static final StreamCodec<FriendlyByteBuf, PacketAE2PatternTransfer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketAE2PatternTransfer::containerId,
            Identifier.STREAM_CODEC, PacketAE2PatternTransfer::recipeId,
            PacketAE2PatternTransfer::new
    );

    @Override
    public Type<PacketAE2PatternTransfer> type() {
        return TYPE;
    }

    public static void handle(PacketAE2PatternTransfer packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof PatternEncodingTermMenu menu)
                    || menu.containerId != packet.containerId() || !menu.stillValid(player)) return;
            if (!(menu.getHost() instanceof IPatternTerminalMenuHost host)) return;

            var holder = RecipeSources.byId(player.level(), packet.recipeId()).orElse(null);
            if (holder == null || !(holder.value() instanceof UniversalProcessorRecipe recipe)
                    || !recipe.isComplete()) return;

            ProcessorRecipeStacks stacks = ProcessorRecipeStacks.of(recipe, player.registryAccess());
            if (!stacks.resolvesAllSlots(recipe)) return;
            List<GenericStack> inputs = Stream.concat(
                    stacks.primaryItemInputs().stream().map(GenericStack::fromItemStack),
                    stacks.primaryFluidInputs().stream().map(GenericStack::fromFluidStack)).toList();
            List<GenericStack> outputs = Stream.concat(
                    stacks.itemOutputs().stream().map(GenericStack::fromItemStack),
                    stacks.fluidOutputs().stream().map(GenericStack::fromFluidStack)).toList();

            PatternEncodingLogic logic = host.getLogic();
            ConfigInventory inputInventory = logic.getEncodedInputInv();
            ConfigInventory outputInventory = logic.getEncodedOutputInv();
            if (!fits(inputs, inputInventory) || !fits(outputs, outputInventory)) return;

            logic.setMode(EncodingMode.PROCESSING);
            menu.setMode(EncodingMode.PROCESSING);
            fill(inputInventory, inputs);
            fill(outputInventory, outputs);
            menu.broadcastChanges();
        });
    }

    private static boolean fits(List<GenericStack> stacks, ConfigInventory inventory) {
        return !stacks.isEmpty() && stacks.size() <= inventory.size()
                && stacks.stream().allMatch(stack -> stack != null && stack.amount() > 0);
    }

    private static void fill(ConfigInventory inventory, List<GenericStack> stacks) {
        inventory.beginBatch();
        try {
            for (int slot = 0; slot < inventory.size(); slot++) {
                inventory.setStack(slot, slot < stacks.size() ? stacks.get(slot) : null);
            }
        } finally {
            inventory.endBatch();
        }
    }
}
