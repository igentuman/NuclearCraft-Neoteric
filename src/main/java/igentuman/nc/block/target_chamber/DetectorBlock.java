package igentuman.nc.block.target_chamber;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.particle_chamber.DetectorDef;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;

import java.util.List;

import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.TARGET_CHAMBER_DETECTORS;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.math.Pos3D.getTaxiDistance;

public class DetectorBlock extends MultiblockBlock {

    public double efficiency = 0;
    public int power = 0;
    public int distance = 0;
    public String type = "";
    public DetectorDef def;

    public DetectorBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
    }

    public DetectorBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
        initParams();
    }

    public DetectorBlock(Properties reactorBlocksProperties, DetectorDef def) {
        super(reactorBlocksProperties);
        this.type = def.name;
        this.def = def;
        this.efficiency = def.efficiency;
        this.power = def.power;
        this.distance = def.distance;
    }

    private void initParams() {
        type = asItem().toString();
        def = TARGET_CHAMBER_DETECTORS.get(type);
        power = def.power;
        efficiency = def.efficiency;
        distance = def.distance;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        if(asItem().toString().contains("empty")) return;
        initParams();
        list.add(TextUtils.applyFormat(__("tooltip.detector.distance", distance), ChatFormatting.GOLD));
        list.add(TextUtils.applyFormat(__("tooltip.detector.power", power), ChatFormatting.GOLD));
        list.add(TextUtils.applyFormat(__("tooltip.detector.efficiency", efficiency*100), ChatFormatting.GOLD));
    }

    public boolean isValid(Level level, BlockPos pos, AbstractMultiblock multiblock) {
        return getTaxiDistance(multiblock.getCenterBlock(), pos) <= distance;
    }
}
