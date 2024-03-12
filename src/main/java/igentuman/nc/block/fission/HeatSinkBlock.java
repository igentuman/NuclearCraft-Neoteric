package igentuman.nc.block.fission;

import igentuman.nc.block.entity.fission.FissionHeatSinkBE;
import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.multiblock.fission.HeatSinkDef;
import igentuman.nc.util.TextUtils;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.block.Block;

import net.minecraft.block.BlockState;

import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.event.client.InputEvents.DESCRIPTIONS_SHOW;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BE;
import static igentuman.nc.util.TextUtils.convertToName;

public class HeatSinkBlock extends Block {

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

    public TextComponent placementRule;

    public HeatSinkBlock(Properties reactorBlocksProperties, HeatSinkDef heatSinkDef) {
        super(reactorBlocksProperties);
        type = heatSinkDef.name;
        def = heatSinkDef;
        heat = def.getHeat();

    }

    public TextComponent getPlacementRule()
    {
        List<String> lines = new ArrayList<>();
        int i = 0;
        if (def.getValidator() instanceof HeatSinkDef.Validator) {
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


/*    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        if(asItem().toString().contains("empty")) return null;
        def.getValidator();
        TileEntity be = FISSION_BE.get("fission_heat_sink").get().create(pPos, pState);
        ((FissionHeatSinkBE)be).setHeatSinkDef(def);
        return be;
    }*/

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if(!player.getItemInHand(hand).isEmpty()) return ActionResultType.FAIL;
        if (!level.isClientSide()) {
            TileEntity be = level.getBlockEntity(pos);
            if(be instanceof FissionHeatSinkBE) {
                int id = level.random.nextInt(10);
                if(((FissionHeatSinkBE) be).isValid(true)) {
                    player.sendMessage(new TranslationTextComponent("message.heat_sink.valid"+id), player.getUUID());
                } else {
                    player.sendMessage(new TranslationTextComponent("message.heat_sink.invalid"+id), player.getUUID());
                }
            }
        }
        return ActionResultType.SUCCESS;
    }
    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable IBlockReader pLevel, List<ITextComponent> list, ITooltipFlag pFlag) {
        if(asItem().toString().contains("empty")) return;
        initParams();
        list.add(TextUtils.applyFormat(new TranslationTextComponent("heat_sink.heat.descr", TextUtils.numberFormat(heat)), TextFormatting.GOLD));

        if(DESCRIPTIONS_SHOW) {
            list.add(TextUtils.applyFormat(getPlacementRule(), TextFormatting.AQUA));
        } else {
            list.add(TextUtils.applyFormat(new TranslationTextComponent("tooltip.toggle_description_keys"), TextFormatting.GRAY));
        }
    }

}
