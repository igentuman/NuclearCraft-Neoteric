package igentuman.nc.network.toServer;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.util.ConfigInventory;
import igentuman.nc.NuclearCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PacketAE2PatternTransfer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketAE2PatternTransfer> TYPE =
        new CustomPacketPayload.Type<>(NuclearCraft.rl("ae2_pattern_transfer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketAE2PatternTransfer> STREAM_CODEC =
        StreamCodec.of((buf, pkt) -> pkt.encode(buf), PacketAE2PatternTransfer::decode);

    private static Field encodingLogicField;

    static {
        try {
            encodingLogicField = PatternEncodingTermMenu.class.getDeclaredField("encodingLogic");
            encodingLogicField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to find encodingLogic field in PatternEncodingTermMenu", e);
        }
    }

    private List<ItemStack> inputItems;
    private List<FluidStack> inputFluids;
    private List<ItemStack> outputItems;
    private List<FluidStack> outputFluids;

    public PacketAE2PatternTransfer() {
        this.inputItems = new ArrayList<>();
        this.inputFluids = new ArrayList<>();
        this.outputItems = new ArrayList<>();
        this.outputFluids = new ArrayList<>();
    }

    public PacketAE2PatternTransfer(List<ItemStack> inputItems, List<FluidStack> inputFluids,
                                     List<ItemStack> outputItems, List<FluidStack> outputFluids) {
        this.inputItems = inputItems;
        this.inputFluids = inputFluids;
        this.outputItems = outputItems;
        this.outputFluids = outputFluids;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketAE2PatternTransfer packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handlePacket(context));
    }

    private void handlePacket(IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player == null) {
            return;
        }

        // Check if the player has a PatternEncodingTermMenu open
        if (!(player.containerMenu instanceof PatternEncodingTermMenu patternEncodingTermMenu)) {
            return;
        }

        try {
            // Set the mode to processing
            patternEncodingTermMenu.setMode(EncodingMode.PROCESSING);

            // Get the encoding logic using reflection
            PatternEncodingLogic encodingLogic = (PatternEncodingLogic) encodingLogicField.get(patternEncodingTermMenu);

            // Get the config inventories from the pattern encoding logic
            ConfigInventory encodedInputInv = encodingLogic.getEncodedInputInv();
            ConfigInventory encodedOutputInv = encodingLogic.getEncodedOutputInv();

            // Clear existing data
            encodedInputInv.clear();
            encodedOutputInv.clear();

            // Process inputs - both items and fluids
            int inputSlot = 0;

            // Add item inputs
            for (ItemStack stack : inputItems) {
                if (inputSlot >= encodedInputInv.size()) break;

                if (!stack.isEmpty()) {
                    AEItemKey itemKey = AEItemKey.of(stack);
                    if (itemKey != null) {
                        encodedInputInv.setStack(inputSlot++, new GenericStack(itemKey, stack.getCount()));
                    }
                }
            }

            // Add fluid inputs
            for (FluidStack fluidStack : inputFluids) {
                if (inputSlot >= encodedInputInv.size()) break;

                if (!fluidStack.isEmpty()) {
                    AEFluidKey fluidKey = AEFluidKey.of(fluidStack);
                    if (fluidKey != null) {
                        encodedInputInv.setStack(inputSlot++, new GenericStack(fluidKey, fluidStack.getAmount()));
                    }
                }
            }

            // Process outputs - both items and fluids
            int outputSlot = 0;

            // Add item outputs
            for (ItemStack stack : outputItems) {
                if (outputSlot >= encodedOutputInv.size()) break;

                if (!stack.isEmpty()) {
                    AEItemKey itemKey = AEItemKey.of(stack);
                    if (itemKey != null) {
                        encodedOutputInv.setStack(outputSlot++, new GenericStack(itemKey, stack.getCount()));
                    }
                }
            }

            // Add fluid outputs
            for (FluidStack fluidStack : outputFluids) {
                if (outputSlot >= encodedOutputInv.size()) break;

                if (!fluidStack.isEmpty()) {
                    AEFluidKey fluidKey = AEFluidKey.of(fluidStack);
                    if (fluidKey != null) {
                        encodedOutputInv.setStack(outputSlot++, new GenericStack(fluidKey, fluidStack.getAmount()));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        // Write input items
        buffer.writeInt(inputItems.size());
        for (ItemStack stack : inputItems) {
            ItemStack.STREAM_CODEC.encode(buffer, stack);
        }

        // Write input fluids
        buffer.writeInt(inputFluids.size());
        for (FluidStack fluidStack : inputFluids) {
            FluidStack.STREAM_CODEC.encode(buffer, fluidStack);
        }

        // Write output items
        buffer.writeInt(outputItems.size());
        for (ItemStack stack : outputItems) {
            ItemStack.STREAM_CODEC.encode(buffer, stack);
        }

        // Write output fluids
        buffer.writeInt(outputFluids.size());
        for (FluidStack fluidStack : outputFluids) {
            FluidStack.STREAM_CODEC.encode(buffer, fluidStack);
        }
    }

    public static PacketAE2PatternTransfer decode(RegistryFriendlyByteBuf buffer) {
        PacketAE2PatternTransfer packet = new PacketAE2PatternTransfer();

        // Read input items
        int inputItemCount = buffer.readInt();
        for (int i = 0; i < inputItemCount; i++) {
            packet.inputItems.add(ItemStack.STREAM_CODEC.decode(buffer));
        }

        // Read input fluids
        int inputFluidCount = buffer.readInt();
        for (int i = 0; i < inputFluidCount; i++) {
            packet.inputFluids.add(FluidStack.STREAM_CODEC.decode(buffer));
        }

        // Read output items
        int outputItemCount = buffer.readInt();
        for (int i = 0; i < outputItemCount; i++) {
            packet.outputItems.add(ItemStack.STREAM_CODEC.decode(buffer));
        }

        // Read output fluids
        int outputFluidCount = buffer.readInt();
        for (int i = 0; i < outputFluidCount; i++) {
            packet.outputFluids.add(FluidStack.STREAM_CODEC.decode(buffer));
        }

        return packet;
    }
}
