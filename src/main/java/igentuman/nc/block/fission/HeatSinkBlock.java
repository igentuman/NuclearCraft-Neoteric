package igentuman.nc.block.fission;

import igentuman.nc.block.entity.processor.NCProcessor;
import igentuman.nc.setup.multiblocks.FissionBlocks;
import igentuman.nc.setup.multiblocks.HeatSinkDef;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.event.client.InputEvents.SHIFT_PRESSED;
import static igentuman.nc.util.TextUtils.convertToName;

public class HeatSinkBlock extends Block implements EntityBlock {

    public HeatSinkBlock() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
    }

    public double heat = 0;
    public String type = "";
    public HeatSinkDef def;

    public HeatSinkBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
        initParams();
    }

    public Component placementRule;

    public HeatSinkBlock(Properties reactorBlocksProperties, HeatSinkDef heatSinkDef) {
        super(reactorBlocksProperties);
        type = heatSinkDef.name;
        def = heatSinkDef;
        heat = def.getHeat();

    }

    public Component getPlacementRule()
    {
        if(placementRule == null) {
            List<String> lines = new ArrayList<>();
            int i = 0;
            if (def.getValidator() instanceof HeatSinkDef.Validator) {
                for (String[] condition : def.getValidator().blockLines().keySet()) {
                    if (i > 0) {
                        lines.add(Component.translatable("heat_sink.and").getString());
                    }
                    String blocksLine = String.join(" "+Component.translatable("heat_sink.or").getString()+" ", getBlockNames(condition[2]));
                    switch (condition[0]) {
                        case ">":
                            lines.add(Component.translatable("heat_sink.atleast"+(condition[1].equals("1") ? "":"s") , condition[1], blocksLine).getString());
                            break;
                        case "-":
                            lines.add(Component.translatable("heat_sink.between", condition[1], blocksLine).getString());
                            break;
                        case "=":
                            lines.add(Component.translatable("heat_sink.exact"+(condition[1].equals("1") ? "":"s"), condition[1], blocksLine).getString());
                            break;
                        case "<":
                            lines.add(Component.translatable("heat_sink.less_than", condition[1], blocksLine).getString());
                            break;
                    }
                    i++;
                }
                placementRule = Component.translatable("heat_sink.placement.rule", String.join(" ", lines));
            } else {
                placementRule = Component.translatable("heat_sink.placement.error");
            }
        }
        return placementRule;
    }




    private List<String> getBlockNames(String rawLine) {

        List<String> names = new ArrayList<>();
        String[] conditionParts = rawLine.split("=|-|>|<");
        String[] blocks = conditionParts[0].split("\\|");

        for(String code: blocks) {
            String id = code;
            if(!id.contains(":")) {
                id = MODID+":"+id;
                Block block = Registry.BLOCK.get(new ResourceLocation(id));
                names.add(block.getName().getString());
            } else {
                names.add(convertToName(id.split(":")[1]));
            }
        }
        return names;
    }

    private void initParams() {
        if(this.asItem().equals(Items.AIR)) return;
        type = asItem().toString().replace("_heat_sink", "");
        def = FissionBlocks.heatsinks.get(type);
        heat = def.getHeat();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        def.getValidator();
        return NCBlocks.MULTIBLOCK_BE.get("fission_be").get().create(pPos, pState);
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof NCProcessor tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof NCProcessor tile) {
                tile.tickServer();
            }
        };
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        if(asItem().toString().contains("empty") || this.asItem().equals(Items.AIR)) return;
        list.add(TextUtils.applyFormat(Component.translatable("heat_sink.heat.descr", TextUtils.numberFormat(heat)), ChatFormatting.GOLD));

        if(SHIFT_PRESSED) {
            list.add(TextUtils.applyFormat(getPlacementRule(), ChatFormatting.AQUA));
        }
    }

}
