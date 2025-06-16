package igentuman.nc.client.gui.element;


import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GuiParticle
{
	private int width = 16;
	private int height = 16;
	private GuiGraphics graphics;

	
	public GuiParticle(GuiGraphics graphics)
	{
		this.graphics = graphics;
	}

	public GuiParticle() {
	}

	public void drawParticleStack(@NotNull GuiGraphics graphics, ParticleStack particleStack, int x, int y)
	{
		this.graphics = graphics;
		if(particleStack == null)
		{
			return;
		}
		if(particleStack.getParticle() == null)
		{
			return;
		}

		RenderSystem.setShaderTexture(0,particleStack.getParticle().getTexture());
		this.graphics.pose().pushPose();
		this.graphics.blit(particleStack.getParticle().getTexture(), x, y, 0, 0, 16, 16, 16, 16);
		this.graphics.pose().popPose();
	}

	private void drawToolTip(ParticleStack stack,int mouseX, int mouseY, boolean showFocus)
	{
		List<String> text = new ArrayList<String>();
		/*text.add(applyStyle(stack.getParticle().getLocalizedName()));
		text.add(TextFormatting.GRAY + Lang.localize("gui.qmd.particlestack.amount",Units.getSIFormat(stack.getAmount(),"pu")));
		text.add(TextFormatting.GRAY + Lang.localize("gui.qmd.particlestack.mean_energy",Units.getParticleEnergy(stack.getMeanEnergy())));
		if(showFocus)
		{
			DecimalFormat df = new DecimalFormat("#.####");
			text.add(TextFormatting.GRAY + Lang.localize("gui.qmd.particlestack.focus",df.format(stack.getFocus())));
		}
		screen.drawHoveringText(text, mouseX, mouseY);*/
		
	}
	
	public void drawToolTipBox(ParticleStack particleStack, int x, int y,int mouseX, int mouseY)
	{
		if (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height)
		{
			
			drawToolTip(particleStack, mouseX, mouseY, false);
		}
	}
	
	public void drawToolTipBoxwithFocus(ParticleStack particleStack, int x, int y,int mouseX, int mouseY)
	{
		if(particleStack != null)
		{
			if(particleStack.getParticle() != null)
			{
				if (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height)
				{
					
					drawToolTip(particleStack, mouseX, mouseY, true);
				}
			}
		}
		
	}
}
