package igentuman.nc.network;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.util.ConfigInventory;
import igentuman.nc.NuclearCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.Field;
import java.util.List;

/** Client -> server payload that encodes a processing pattern into an AE2 pattern terminal menu. */
public class PacketAE2PatternTransfer implements CustomPacketPayload {
    public static final Type<PacketAE2PatternTransfer> TYPE = new Type<>(NuclearCraft.rl("ae2_pattern_transfer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketAE2PatternTransfer> STREAM_CODEC = StreamCodec.composite(
        ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), PacketAE2PatternTransfer::inputItems,
        FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketAE2PatternTransfer::inputFluids,
        ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), PacketAE2PatternTransfer::outputItems,
        FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketAE2PatternTransfer::outputFluids,
        PacketAE2PatternTransfer::new
    );

    private static final Field encodingLogicField;

    static {
        try {
            encodingLogicField = PatternEncodingTermMenu.class.getDeclaredField("encodingLogic");
            encodingLogicField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to find encodingLogic field in PatternEncodingTermMenu", e);
        }
    }

    private final List<ItemStack> inputItems;
    private final List<FluidStack> inputFluids;
    private final List<ItemStack> outputItems;
    private final List<FluidStack> outputFluids;

    public PacketAE2PatternTransfer(List<ItemStack> inputItems, List<FluidStack> inputFluids,
                                    List<ItemStack> outputItems, List<FluidStack> outputFluids) {
        this.inputItems = inputItems;
        this.inputFluids = inputFluids;
        this.outputItems = outputItems;
        this.outputFluids = outputFluids;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public List<ItemStack> inputItems() { return inputItems; }
    public List<FluidStack> inputFluids() { return inputFluids; }
    public List<ItemStack> outputItems() { return outputItems; }
    public List<FluidStack> outputFluids() { return outputFluids; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof PatternEncodingTermMenu menu)) return;

            try {
                menu.setMode(EncodingMode.PROCESSING);
                PatternEncodingLogic encodingLogic = (PatternEncodingLogic) encodingLogicField.get(menu);

                ConfigInventory inputInv = encodingLogic.getEncodedInputInv();
                ConfigInventory outputInv = encodingLogic.getEncodedOutputInv();

                inputInv.clear();
                outputInv.clear();

                int slot = 0;
                for (ItemStack stack : inputItems) {
                    if (slot >= inputInv.size()) break;
                    if (!stack.isEmpty()) {
                        AEItemKey key = AEItemKey.of(stack);
                        if (key != null) inputInv.setStack(slot++, new GenericStack(key, stack.getCount()));
                    }
                }
                for (FluidStack fluid : inputFluids) {
                    if (slot >= inputInv.size()) break;
                    if (!fluid.isEmpty()) {
                        AEFluidKey key = AEFluidKey.of(fluid.getFluid());
                        if (key != null) inputInv.setStack(slot++, new GenericStack(key, fluid.getAmount()));
                    }
                }

                slot = 0;
                for (ItemStack stack : outputItems) {
                    if (slot >= outputInv.size()) break;
                    if (!stack.isEmpty()) {
                        AEItemKey key = AEItemKey.of(stack);
                        if (key != null) outputInv.setStack(slot++, new GenericStack(key, stack.getCount()));
                    }
                }
                for (FluidStack fluid : outputFluids) {
                    if (slot >= outputInv.size()) break;
                    if (!fluid.isEmpty()) {
                        AEFluidKey key = AEFluidKey.of(fluid.getFluid());
                        if (key != null) outputInv.setStack(slot++, new GenericStack(key, fluid.getAmount()));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
