package igentuman.nc.block.turbine;

import igentuman.nc.block.turbine.entity.TurbineCoilBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.turbine.CoilDef;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.event.client.InputEvents.DESCRIPTIONS_SHOW;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;
import static igentuman.nc.util.NcUtils.rlFromString;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.convertToName;

public class TurbineCoilBlock extends Block implements EntityBlock {

    public TurbineCoilBlock() {
        this(Properties.of().sound(SoundType.METAL));
    }

    public TurbineCoilBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
    }

    public double efficiency = 0;
    public String type = "";
    public CoilDef def;

    private void initParams() {
        Item item = Item.byBlock(this);
        if(item.toString().isEmpty()) return;
        String name = item.toString();
        if(name.contains(":")) name = name.substring(name.indexOf(':') + 1);
        type = name.replaceAll("_coil|turbine_", "");
        def = TurbineRegistration.coils.get(type);
        if(def == null) return;
        efficiency = def.getEfficiency();
    }

    public Component placementRule;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        if(def == null) initParams();
        def.getValidator();
        TurbineCoilBE be = (TurbineCoilBE) TURBINE_BE.get("turbine_coil").get().create(pPos, pState);
        be.setCoilDef(def);
        return be;
    }

    private List<String> getBlockNames(String rawLine) {

        List<String> names = new ArrayList<>();
        String[] conditionParts = rawLine.split("=|-|>|<|\\^");
        String[] blocks = conditionParts[0].split("\\|");

        for(String code: blocks) {
            String id = code;
            if(!id.contains(":")) {
                id = MODID+":"+id;
                Block block = BuiltInRegistries.BLOCK.get(rlFromString(id));
                names.add(block.getName().getString());
            } else {
                names.add(convertToName(id.split(":")[1]));
            }
        }
        return names;
    }

    public Component getPlacementRule()
    {
        if(placementRule == null) {
            List<String> lines = new ArrayList<>();
            int i = 0;
            if (def.getValidator() instanceof CoilDef.Validator) {
                for (String[] condition : def.getValidator().blockLines().keySet()) {
                    if (i > 0) {
                        lines.add(__("heat_sink.and").getString());
                    }
                    String blocksLine = String.join(" "+__("heat_sink.or").getString()+" ", getBlockNames(condition[2]));
                    switch (condition[0]) {
                        case ">":
                            lines.add(__("heat_sink.atleast"+(condition[1].equals("1") ? "":"s") , condition[1], blocksLine).getString());
                            break;
                        case "-":
                            lines.add(__("heat_sink.between", condition[1], blocksLine).getString());
                            break;
                        case "=":
                            lines.add(__("heat_sink.exact"+(condition[1].equals("1") ? "":"s"), condition[1], blocksLine).getString());
                            break;
                        case "<":
                            lines.add(__("heat_sink.less_than", condition[1], blocksLine).getString());
                            break;
                        case "^":
                            lines.add(__("heat_sink.in_corner", condition[1], blocksLine).getString());
                            break;
                    }
                    i++;
                }
                placementRule = __("heat_sink.placement.rule", String.join(" ", lines));
            } else {
                placementRule = __("heat_sink.placement.error");
            }
        }
        return placementRule;
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof TurbineCoilBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof TurbineCoilBE tile) {
                tile.tickServer();
            }
        };
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        if(level.isClientSide()) return;
        MultiblockHandler.get(((Level)level).dimension()).trackBlockChange(neighbor);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);
        MultiblockHandler.get(pLevel.dimension()).trackBlockChange(pPos, true);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if(level.isClientSide()) return;
        MultiblockHandler.get(level.dimension()).trackBlockChange(pos, true);
    }


    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> list, TooltipFlag pFlag) {
        initParams();
        if(def == null) return;
        if(DESCRIPTIONS_SHOW) {
            list.add(TextUtils.applyFormat(getPlacementRule(), ChatFormatting.AQUA));
            list.add(TextUtils.applyFormat(
                    __("tooltip.nc.description.efficiency", TextUtils.numberFormat(def.getEfficiency())),
                    ChatFormatting.GOLD));
        } else {
            list.add(TextUtils.applyFormat(__("tooltip.toggle_description_keys"), ChatFormatting.GRAY));
        }
    }
}
