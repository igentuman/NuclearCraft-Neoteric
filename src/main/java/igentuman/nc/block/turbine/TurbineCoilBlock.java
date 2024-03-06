package igentuman.nc.block.turbine;

import igentuman.nc.block.entity.fission.FissionHeatSinkBE;
import igentuman.nc.block.entity.turbine.TurbineBE;
import igentuman.nc.block.entity.turbine.TurbineBladeBE;
import igentuman.nc.block.entity.turbine.TurbineCoilBE;
import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.multiblock.fission.HeatSinkDef;
import igentuman.nc.multiblock.turbine.CoilDef;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.event.client.InputEvents.DESCRIPTIONS_SHOW;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BE;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;
import static igentuman.nc.util.TextUtils.convertToName;

public class TurbineCoilBlock extends Block implements EntityBlock {

    public TurbineCoilBlock() {
        this(Properties.of(Material.METAL).sound(SoundType.METAL));
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
        type = item.toString().replaceAll("_coil|turbine_", "");
        def = TurbineRegistration.coils.get(type);
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
                Block block = Registry.BLOCK.get(new ResourceLocation(id));
                names.add(block.getName().getString());
            } else {
                names.add(convertToName(id.split(":")[1]));
            }
        }
        return names;
    }

    public Component getPlacementRule()
    {
        List<String> lines = new ArrayList<>();
        int i = 0;
        if (def.getValidator() instanceof CoilDef.Validator) {
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
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor){
        ((TurbineBE) Objects.requireNonNull(level.getBlockEntity(pos))).onNeighborChange(state,  pos, neighbor);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        initParams();
        if(DESCRIPTIONS_SHOW) {
            list.add(TextUtils.applyFormat(getPlacementRule(), ChatFormatting.AQUA));
            list.add(TextUtils.applyFormat(
                    new TranslatableComponent("tooltip.nc.description.efficiency", TextUtils.numberFormat(def.getEfficiency())),
                    ChatFormatting.GOLD));
        } else {
            list.add(TextUtils.applyFormat(new TranslatableComponent("tooltip.toggle_description_keys"), ChatFormatting.GRAY));
        }
    }
}
