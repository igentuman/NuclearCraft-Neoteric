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
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.event.client.InputEvents.DESCRIPTIONS_SHOW;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BE;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;
import static igentuman.nc.util.TextUtils.convertToName;

public class TurbineCoilBlock extends Block {

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

    public TextComponent placementRule;

/*
    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        if(def == null) initParams();
        def.getValidator();
        TurbineCoilBE be = (TurbineCoilBE) TURBINE_BE.get("turbine_coil").get().create(pPos, pState);
        be.setCoilDef(def);
        return be;
    }
*/

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

    public TextComponent getPlacementRule()
    {
        List<String> lines = new ArrayList<>();
        int i = 0;
        if (def.getValidator() instanceof CoilDef.Validator) {
            for (String[] condition : def.getValidator().blockLines().keySet()) {
                if (i > 0) {
                    lines.add(new TranslationTextComponent("heat_sink.and").getString());
                }
                String blocksLine = String.join(" "+new TranslationTextComponent("heat_sink.or").getString()+" ", getBlockNames(condition[2]));
                switch (condition[0]) {
                    case ">":
                        lines.add(new TranslationTextComponent("heat_sink.atleast"+(condition[1].equals("1") ? "":"s") , condition[1], blocksLine).getString());
                        break;
                    case "-":
                        lines.add(new TranslationTextComponent("heat_sink.between", condition[1], blocksLine).getString());
                        break;
                    case "=":
                        lines.add(new TranslationTextComponent("heat_sink.exact"+(condition[1].equals("1") ? "":"s"), condition[1], blocksLine).getString());
                        break;
                    case "<":
                        lines.add(new TranslationTextComponent("heat_sink.less_than", condition[1], blocksLine).getString());
                        break;
                    case "^":
                        lines.add(new TranslationTextComponent("heat_sink.in_corner", condition[1], blocksLine).getString());
                        break;
                }
                i++;
            }
            return new TranslationTextComponent("heat_sink.placement.rule", String.join(" ", lines));
        }
        return new TranslationTextComponent("heat_sink.placement.error");
    }


    @Override
    public void onNeighborChange(BlockState state, IWorldReader level, BlockPos pos, BlockPos neighbor){
        ((TurbineBE) Objects.requireNonNull(level.getBlockEntity(pos))).onNeighborChange(state,  pos, neighbor);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable IBlockReader pLevel, List<ITextComponent> list, ITooltipFlag pFlag) {
        initParams();
        if(DESCRIPTIONS_SHOW) {
            list.add(TextUtils.applyFormat(getPlacementRule(), TextFormatting.AQUA));
            list.add(TextUtils.applyFormat(
                    new TranslationTextComponent("tooltip.nc.description.efficiency", TextUtils.numberFormat(def.getEfficiency())),
                    TextFormatting.GOLD));
        } else {
            list.add(TextUtils.applyFormat(new TranslationTextComponent("tooltip.toggle_description_keys"), TextFormatting.GRAY));
        }
    }
}
