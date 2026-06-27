package igentuman.nc.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.util.TextUtils.__;

/**
 * Utility class for rendering fluids in GUI screens without depending on JEI.
 * Provides methods for getting fluid textures, tint colors, tooltips, and
 * rendering fluid tanks in container screens.
 */
public class GuiFluidRenderer {

    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;

    /**
     * Returns the tint color (ARGB) for a given FluidStack.
     */
    public static int getColorTint(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions renderProps = IClientFluidTypeExtensions.of(fluid);
        return renderProps.getTintColor(fluidStack);
    }

    /**
     * Returns the still texture sprite for a given FluidStack, if available.
     */
    public static Optional<TextureAtlasSprite> getStillFluidSprite(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions renderProps = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation stillTexture = renderProps.getStillTexture(fluidStack);
        if (stillTexture == null) {
            return Optional.empty();
        }

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(stillTexture);

        return Optional.of(sprite);
    }

    /**
     * Returns the display name for a given FluidStack.
     */
    public static Component getDisplayName(FluidStack fluidStack) {
        return fluidStack.getHoverName();
    }

    /**
     * Builds a tooltip list for a given FluidStack.
     *
     * @param fluidStack  the fluid stack
     * @param tooltipFlag the tooltip flag (normal or advanced)
     * @return list of tooltip components
     */
    public static List<Component> getTooltip(FluidStack fluidStack, TooltipFlag tooltipFlag) {
        List<Component> tooltip = new ArrayList<>();
        Fluid fluid = fluidStack.getFluid();
        if (fluid.isSame(Fluids.EMPTY)) {
            return tooltip;
        }

        tooltip.add(getDisplayName(fluidStack));

        long amount = fluidStack.getAmount();
        if (amount > 0) {
            MutableComponent amountText = Component.literal(amount + " mB")
                    .withStyle(ChatFormatting.GRAY);
            tooltip.add(amountText);
        }

        if (tooltipFlag.isAdvanced()) {
            ResourceLocation registryName = BuiltInRegistries.FLUID.getKey(fluid);
            if (registryName != null) {
                MutableComponent advancedId = Component.literal(registryName.toString())
                        .withStyle(ChatFormatting.DARK_GRAY);
                tooltip.add(advancedId);
            }
        }

        return tooltip;
    }

    /**
     * Returns the bucket volume constant (1000 mB).
     */
    public static int bucketVolume() {
        return FluidType.BUCKET_VOLUME;
    }

    /**
     * Renders a fluid tank in a GUI.
     *
     * @param guiGraphics the gui graphics context
     * @param x           the left x position
     * @param y           the top y position
     * @param width       the width of the tank area
     * @param height      the height of the tank area
     * @param fluidStack  the fluid to render
     * @param capacity    the maximum capacity of the tank (used to calculate fill level)
     */
    public static void renderFluidTank(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                       FluidStack fluidStack, int capacity) {
        if (fluidStack.isEmpty() || capacity <= 0) {
            return;
        }

        Optional<TextureAtlasSprite> spriteOpt = getStillFluidSprite(fluidStack);
        if (spriteOpt.isEmpty()) {
            return;
        }

        TextureAtlasSprite sprite = spriteOpt.get();
        int tintColor = getColorTint(fluidStack);

        int amount = fluidStack.getAmount();
        int scaledHeight = (int) ((long) height * amount / capacity);
        if (amount > 0 && scaledHeight < 1) {
            scaledHeight = 1;
        }
        if (scaledHeight > height) {
            scaledHeight = height;
        }

        int startY = y + height - scaledHeight;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, startY, 0);

        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();

        float alpha = ((tintColor >> 24) & 0xFF) / 255.0f;
        float red = ((tintColor >> 16) & 0xFF) / 255.0f;
        float green = ((tintColor >> 8) & 0xFF) / 255.0f;
        float blue = (tintColor & 0xFF) / 255.0f;

        int drawHeight = scaledHeight;
        int drawWidth = width;

        int tileX = 0;
        while (tileX < drawWidth) {
            int currentWidth = Math.min(TEX_WIDTH, drawWidth - tileX);
            int tileY = 0;
            while (tileY < drawHeight) {
                int currentHeight = Math.min(TEX_HEIGHT, drawHeight - tileY);
                drawTiledSprite(guiGraphics, tileX, tileY, currentWidth, currentHeight,
                        sprite, red, green, blue, alpha);
                tileY += TEX_HEIGHT;
            }
            tileX += TEX_WIDTH;
        }

        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();
    }

    /**
     * Renders a fluid stack at the given position with a fixed 16x16 size.
     * Useful for rendering fluid "items" in slots.
     *
     * @param guiGraphics the gui graphics context
     * @param x           the left x position
     * @param y           the top y position
     * @param fluidStack  the fluid to render
     */
    public static void renderFluid(GuiGraphics guiGraphics, int x, int y, FluidStack fluidStack) {
        renderFluidTank(guiGraphics, x, y, TEX_WIDTH, TEX_HEIGHT, fluidStack, fluidStack.getAmount());
    }

    /**
     * Renders a tooltip for a fluid tank when the mouse is hovering over it.
     *
     * @param guiGraphics the gui graphics context
     * @param mouseX      mouse x position
     * @param mouseY      mouse y position
     * @param tankX       the left x of the tank
     * @param tankY       the top y of the tank
     * @param tankWidth   the width of the tank
     * @param tankHeight  the height of the tank
     * @param fluidStack  the fluid in the tank
     * @param capacity    the capacity of the tank
     */
    public static void renderFluidTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                          int tankX, int tankY, int tankWidth, int tankHeight,
                                          FluidStack fluidStack, int capacity) {
        if (mouseX >= tankX && mouseX < tankX + tankWidth
                && mouseY >= tankY && mouseY < tankY + tankHeight) {
            List<Component> tooltip = new ArrayList<>();
            if (!fluidStack.isEmpty()) {
                tooltip.addAll(getTooltip(fluidStack, TooltipFlag.NORMAL));
                tooltip.add(Component.literal(TextUtils.formatLiquid(fluidStack.getAmount()) + " / " + TextUtils.formatLiquid(capacity))
                        .withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(__("tooltip.fluid.empty").withStyle(ChatFormatting.GRAY));
            }
            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    private static void drawTiledSprite(GuiGraphics guiGraphics, int xOffset, int yOffset,
                                        int width, int height, TextureAtlasSprite sprite,
                                        float red, float green, float blue, float alpha) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Adjust UV for partial tiles
        float uWidth = u1 - u0;
        float vHeight = v1 - v0;
        float adjustedU1 = u0 + uWidth * ((float) width / TEX_WIDTH);
        float adjustedV1 = v0 + vHeight * ((float) height / TEX_HEIGHT);

        Matrix4f matrix = guiGraphics.pose().last().pose();

        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.addVertex(matrix, xOffset, yOffset + height, 0)
                .setUv(u0, adjustedV1)
                .setColor(red, green, blue, alpha);
        builder.addVertex(matrix, xOffset + width, yOffset + height, 0)
                .setUv(adjustedU1, adjustedV1)
                .setColor(red, green, blue, alpha);
        builder.addVertex(matrix, xOffset + width, yOffset, 0)
                .setUv(adjustedU1, v0)
                .setColor(red, green, blue, alpha);
        builder.addVertex(matrix, xOffset, yOffset, 0)
                .setUv(u0, v0)
                .setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }
}
