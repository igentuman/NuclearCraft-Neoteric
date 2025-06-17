package igentuman.nc.client.gui.element;


import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.util.Units;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.util.TextUtils.__;

public class GuiParticle extends NCGuiElement {

	public int x;
	public int y;

	public GuiParticle(int pX, int pY) {
		super(pX, pY, 16, 16, Component.empty());
		this.x = pX;
		this.y = pY;
		this.width = 16;
		this.height = 16;
	}

	public void drawParticleStack(@NotNull GuiGraphics graphics, ParticleStack particleStack)
	{
		if(particleStack == null || particleStack.getParticle() == null) {
			return;
		}

		RenderSystem.setShaderTexture(0,particleStack.getParticle().getTexture());
		graphics.pose().pushPose();
		graphics.pose().translate(RELATIVE_X, RELATIVE_Y, 0);
		graphics.blit(particleStack.getParticle().getTexture(), x, y, 0, 0, 16, 16, 16, 16);
		graphics.pose().popPose();
	}

	public void renderTooltip(GuiGraphics graphics, ParticleStack particleStack, int pMouseX, int pMouseY) {
		if(particleStack == null || particleStack.getParticle() == null) {
			return;
		}
		List<Component> text = new ArrayList<>();
		text.add(particleStack.getParticle().getLocalizedName());
		text.add(__("tooltip.nuclearcraft.particlestack.amount",Units.getSIFormat(particleStack.getAmount(),"pu")).withStyle(ChatFormatting.GRAY));
		text.add(__("tooltip.nuclearcraft.particlestack.mean_energy", Units.getParticleEnergy(particleStack.getMeanEnergy())).withStyle(ChatFormatting.GRAY));
		DecimalFormat df = new DecimalFormat("#.####");
		text.add(__("tooltip.nuclearcraft.particlestack.focus",df.format(particleStack.getFocus())).withStyle(ChatFormatting.GRAY));
		graphics.renderTooltip(Minecraft.getInstance().font, text, Optional.empty(), pMouseX, pMouseY);
	}

	@Override
	public boolean isMouseOver(double pMouseX, double pMouseY) {
		return pMouseX >= (double)x && pMouseY >= (double)y && pMouseX < (double)(x + width) && pMouseY < (double)(y + height);
	}

}


