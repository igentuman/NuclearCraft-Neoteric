package igentuman.nc.compat.jei.ingredient;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.util.Units;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;

/**
 * source https://github.com/Lach01298/QMD
 */
@SuppressWarnings("removal")
public class ParticleStackRenderer  implements IIngredientRenderer<ParticleStack>
{

	private static final int TEX_WIDTH = 16;
	private static final int TEX_HEIGHT = 16;
	
	private final int amount;
	private final long energy;
	private final double focus;
	private final int width;
	private final int height;
	
	@Nullable
	private final IDrawable overlay;
	
	public ParticleStackRenderer()
	{
		this(0,0,0, TEX_WIDTH, TEX_HEIGHT, null);
	}
	
	public ParticleStackRenderer(int amount, long energy, double focus, int width, int height, @Nullable IDrawable overlay)
	{
		this.amount = amount;
		this.energy = energy;
		this.focus = focus;
		//this.tooltipMode = tooltipMode;
		this.width = width;
		this.height = height;
		this.overlay = overlay;
	}


	@Override
	public void render(GuiGraphics guiGraphics, ParticleStack particleStack) {
		this.render(guiGraphics,particleStack, 0, 0);
	}

	@Override
	public void render(GuiGraphics graphics, @Nullable ParticleStack particleStack, int posX, int posY)
	{
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		graphics.pose().pushPose();
		graphics.pose().translate((float)posX, (float)posY, 0.0F);
		drawParticle(graphics, 0, 0, particleStack);
		if (overlay != null) {
			graphics.pose().pushPose();
			graphics.pose().translate(0, 0, 200);
			overlay.draw(graphics, 0, 0);
			graphics.pose().popPose();
		}
		graphics.pose().popPose();
		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
	}

	@Override
	public List<Component> getTooltip(ParticleStack particleStack, TooltipFlag tooltipFlag) {
		return List.of();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, ParticleStack ingredient, TooltipFlag tooltipFlag) {
		IIngredientRenderer.super.getTooltip(tooltip, ingredient, tooltipFlag);
		tooltip.add(__(ingredient.getParticle().getUnlocalizedName()));
		tooltip.add(Component.literal("nuclearcraft:particle/"+ingredient.getParticle().getName()).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(__("tooltip.nuclearcraft.particlestack.amount", Units.getSIFormat(ingredient.getAmount(),"pu")).withStyle(ChatFormatting.GRAY));
		if(ingredient.getMeanEnergy() > 0) {
			tooltip.add(__("tooltip.nuclearcraft.particlestack.mean_energy", Units.getParticleEnergy(ingredient.getMeanEnergy())).withStyle(ChatFormatting.GRAY));
		}
		if(ingredient.getFocus() > 0) {
			tooltip.add(__("tooltip.nuclearcraft.particlestack.focus", Units.getSIFormat(ingredient.getFocus(), "")).withStyle(ChatFormatting.GRAY));
		}
	}

	private void drawParticle(GuiGraphics graphics, final int xPosition, final int yPosition,
			@Nullable ParticleStack particleStack)
	{
		if (particleStack == null)
		{
			return;
		}
		Particle particle = particleStack.getParticle();
		if (particle == null)
		{
			return;
		}
		
		// Set up proper rendering state
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		
		// Draw the texture
		graphics.blit(particleStack.getParticle().getTexture(), xPosition, yPosition, 0, 0, 16, 16, 16, 16);
	}
}