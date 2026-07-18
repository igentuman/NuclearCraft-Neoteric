package igentuman.nc.handler.event.client;

import igentuman.nc.block.accelerator.entity.AcceleratorBeamPortBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.util.Units;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.util.StackUtils.isMultiTool;
import static igentuman.nc.util.TextUtils.__;

public class BeamParticleOverlay {

    public static final IGuiOverlay BEAM_PARTICLE_INFO = (gui, poseStack, partialTick, width, height) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!isMultiTool(mc.player.getMainHandItem()) && !isMultiTool(mc.player.getOffhandItem())) return;
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) return;

        BlockEntity be = mc.level.getBlockEntity(bhr.getBlockPos());
        ParticleStack stack;
        if (be instanceof AcceleratorBeamPortBE port) {
            stack = port.clientParticle;
        } else if (be instanceof TargetChamberBeamPortBE port) {
            stack = port.clientParticle;
        } else {
            return;
        }
        if (stack == null || stack.getParticle() == null) return;

        DecimalFormat df = new DecimalFormat("#.####");
        List<Component> text = new ArrayList<>();
        text.add(stack.getParticle().getLocalizedName());
        text.add(__("tooltip.nuclearcraft.particlestack.amount", Units.getSIFormat(stack.getAmount(), "pu")).withStyle(ChatFormatting.GRAY));
        text.add(__("tooltip.nuclearcraft.particlestack.mean_energy", Units.getParticleEnergy(stack.getMeanEnergy())).withStyle(ChatFormatting.GRAY));
        text.add(__("tooltip.nuclearcraft.particlestack.focus", df.format(stack.getFocus())).withStyle(ChatFormatting.GRAY));

        net.minecraft.client.gui.Font f = Minecraft.getInstance().font;
        int tx = width / 2 + 8;
        int ty = height / 2 + 8;
        for (net.minecraft.network.chat.Component line : text) {
            f.drawShadow(poseStack, line, tx, ty, 0xFFFFFF);
            ty += f.lineHeight + 2;
        }
    };
}
