package igentuman.nc.block.fission;

import igentuman.nc.block.entity.fission.FissionHeatSinkBE;
import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.multiblock.fission.HeatSinkDef;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.event.client.InputEvents.DESCRIPTIONS_SHOW;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BE;
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
        List<String> lines = new ArrayList<>();
        int i = 0;
        if (def.getValidator() instanceof HeatSinkDef.Validator) {
            for (String[] condition : def.getValidator().blockLines().keySet()) {
                if (i > 0) {
                    lines.add(new TranslatableComponent("heat_sink.and").getString());
                }
                String blocksLine = String.join(" "+new TranslatableComponent("heat_sink.or").getString()+" ", getBlockNames(condition[2]));
                switch (condition[0]) {
                    case ">":
                        lines.add(new TranslatableComponent("heat_sink.atleast"+(condition[1].equals("1") ? "":"s") , condition[1], blocksLine).getString());
                        break;
                    case "-":
                        lines.add(new TranslatableComponent("heat_sink.between", condition[1], blocksLine).getString());
                        break;
                    case "=":
                        lines.add(new TranslatableComponent("heat_sink.exact"+(condition[1].equals("1") ? "":"s"), condition[1], blocksLine).getString());
                        break;
                    case "<":
                        lines.add(new TranslatableComponent("heat_sink.less_than", condition[1], blocksLine).getString());
                        break;
                    case "^":
                        lines.add(new TranslatableComponent("heat_sink.in_corner", condition[1], blocksLine).getString());
                        break;
                }
                i++;
            }
            return new TranslatableComponent("heat_sink.placement.rule", String.join(" ", lines));
        }
        return new TranslatableComponent("heat_sink.placement.error");
    }




    private List<String> getBlockNames(String rawLine) {

        List<String> names = new ArrayList<>();
        String[] conditionParts = rawLine.split("=|-|>|<|\\^");
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
        Item item = Item.byBlock(this);
        if(item.toString().isEmpty()) return;
        if(item.toString().contains("empty")) return;
        type = item.toString().replace("_heat_sink", "");
        def = FissionBlocks.heatsinks.get(type);
        heat = def.getHeat();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        if(asItem().toString().contains("empty")) return null;
        def.getValidator();
        BlockEntity be = FISSION_BE.get("fission_heat_sink").get().create(pPos, pState);
        ((FissionHeatSinkBE)be).setHeatSinkDef(def);
        return be;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(!player.getItemInHand(hand).isEmpty()) return InteractionResult.FAIL;
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if(be instanceof FissionHeatSinkBE) {
                int id = level.random.nextInt(10);
                if(((FissionHeatSinkBE) be).isValid(true)) {
                    player.sendMessage(new TranslatableComponent("message.heat_sink.valid"+id), UUID.randomUUID());
                } else {
                    player.sendMessage(new TranslatableComponent("message.heat_sink.invalid"+id), UUID.randomUUID());
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof FissionHeatSinkBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof FissionHeatSinkBE tile) {
                tile.tickServer();
            }
        };
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        if(asItem().toString().contains("empty")) return;
        initParams();
        list.add(TextUtils.applyFormat(new TranslatableComponent("heat_sink.heat.descr", TextUtils.numberFormat(heat)), ChatFormatting.GOLD));

        if(DESCRIPTIONS_SHOW) {
            list.add(TextUtils.applyFormat(getPlacementRule(), ChatFormatting.AQUA));
        } else {
            list.add(TextUtils.applyFormat(new TranslatableComponent("tooltip.toggle_description_keys"), ChatFormatting.GRAY));
        }
    }

}
