package igentuman.nc.compat.jei.ingredient;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.util.Units;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
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
	public void render(PoseStack poseStack, @Nullable ParticleStack particleStack) {
		this.render(poseStack, particleStack, 0, 0);
	}

	public void render(PoseStack poseStack, @Nullable ParticleStack particleStack, int posX, int posY)
	{
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		poseStack.pushPose();
		poseStack.translate((float)posX, (float)posY, 0.0F);
		drawParticle(poseStack, 0, 0, particleStack);
		if (overlay != null) {
			poseStack.pushPose();
			poseStack.translate(0, 0, 200);
			overlay.draw(poseStack, 0, 0);
			poseStack.popPose();
		}
		poseStack.popPose();
		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
	}

	@Override
	public List<Component> getTooltip(ParticleStack ingredient, TooltipFlag tooltipFlag) {
		// 1.19.2 JEI: IIngredientRenderer only has getTooltip returning List<Component>; no ITooltipBuilder variant
		List<Component> list = new java.util.ArrayList<>();
		list.add(__(ingredient.getParticle().getUnlocalizedName()));
		list.add(Component.literal("nuclearcraft:particle/"+ingredient.getParticle().getName()).withStyle(ChatFormatting.DARK_GRAY));
		list.add(__("tooltip.nuclearcraft.particlestack.amount", Units.getSIFormat(ingredient.getAmount(),"pu")).withStyle(ChatFormatting.GRAY));
		if(ingredient.getMeanEnergy() > 0) {
			list.add(__("tooltip.nuclearcraft.particlestack.mean_energy", Units.getParticleEnergy(ingredient.getMeanEnergy())).withStyle(ChatFormatting.GRAY));
		}
		if(ingredient.getFocus() > 0) {
			list.add(__("tooltip.nuclearcraft.particlestack.focus", Units.getSIFormat(ingredient.getFocus(), "")).withStyle(ChatFormatting.GRAY));
		}
		return list;
	}

	private void drawParticle(PoseStack poseStack, final int xPosition, final int yPosition,
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
		RenderSystem.setShaderTexture(0, particleStack.getParticle().getTexture());
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Draw the texture
		GuiComponent.blit(poseStack, xPosition, yPosition, 0, 0, 16, 16, 16, 16);
	}
}