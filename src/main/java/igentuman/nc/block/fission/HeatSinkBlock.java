package igentuman.nc.block.fission;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
import igentuman.nc.multiblock.fission.HeatSinkDef;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.handler.event.client.InputEvents.DESCRIPTIONS_SHOW;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.heatsinks;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.convertToName;

public class HeatSinkBlock extends MultiblockBlock {

    public double heat = 0;
    public String type = "";
    public HeatSinkDef def;

    public HeatSinkBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
    }

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

    private List<String> getBlockNames(String rawLine) {

        List<String> names = new ArrayList<>();
        String[] conditionParts = rawLine.split("=|-|>|<|\\^");
        String[] blocks = conditionParts[0].split("\\|");

        for(String code: blocks) {
            String id = code;
            if(!id.contains(":")) {
                ResourceLocation res = rl(id);
                Block block = BuiltInRegistries.BLOCK.get(res);
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
        def = heatsinks.get(type);
        heat = def.getHeat();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(!stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide()) {
            Block block = level.getBlockState(pos).getBlock();
            if(block instanceof HeatSinkBlock) {
                int id = level.random.nextInt(10);
                AbstractMultiblock mb = MultiblockHandler.get(level.dimension()).getMultiblockByPos(pos);
                FissionReactorMultiblock fissionReactorMultiblock = null;
                if(mb instanceof FissionReactorMultiblock fsmb) {
                    fissionReactorMultiblock = fsmb;
                    fissionReactorMultiblock.validateInner(true);
                }
                if(isValid(level, pos, fissionReactorMultiblock)) {
                    player.sendSystemMessage(__("message.heat_sink.valid"+id));
                } else {
                    player.sendSystemMessage(__("message.heat_sink.invalid"+id));
                }
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> list, TooltipFlag pFlag) {
        if(asItem().toString().contains("empty")) return;
        initParams();
        list.add(TextUtils.applyFormat(__("heat_sink.heat.descr", TextUtils.numberFormat(heat)), ChatFormatting.GOLD));

        if(DESCRIPTIONS_SHOW) {
            list.add(TextUtils.applyFormat(getPlacementRule(), ChatFormatting.AQUA));
            if(isActive()) {
                list.add(TextUtils.applyFormat(__("tooltip.active_heatsink"), ChatFormatting.YELLOW));
            }
        } else {
            list.add(TextUtils.applyFormat(__("tooltip.toggle_description_keys"), ChatFormatting.GRAY));
        }
    }

    public boolean isValid(Level level, BlockPos pos, FissionReactorMultiblock multiblock) {
        return def.getValidator().isValid(level, pos, multiblock);
    }

    public boolean isActive() {
        return def.name.contains("active");
    }
}
