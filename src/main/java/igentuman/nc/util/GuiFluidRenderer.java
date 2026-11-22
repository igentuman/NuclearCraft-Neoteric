package igentuman.nc.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.util.TextUtils.__;

/** Renders fluid tanks, sprites, and tooltips in GUI screens without depending on JEI. */
public class GuiFluidRenderer {

    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;

    private static Optional<FluidModel> fluidModel(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                .get(fluidStack.getFluid().defaultFluidState()));
    }

    /**
     * Returns the tint color (ARGB) for a given FluidStack. Alpha drives gas/acid translucency.
     */
    public static int getColorTint(FluidStack fluidStack) {
        return fluidModel(fluidStack)
                .map(FluidModel::fluidTintSource)
                .map((FluidTintSource tint) -> tint.colorAsStack(fluidStack))
                .orElse(0xFFFFFFFF);
    }

    /**
     * Returns the still texture sprite for a given FluidStack, if available.
     */
    public static Optional<TextureAtlasSprite> getStillFluidSprite(FluidStack fluidStack) {
        return fluidModel(fluidStack).map(model -> model.stillMaterial().sprite());
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
            Identifier registryName = BuiltInRegistries.FLUID.getKey(fluid);
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
     * Renders a fluid tank in a GUI. The fill grows bottom-up; partial tiles are cropped by a
     * scissor rectangle rather than stretched.
     *
     * @param guiGraphics the gui graphics context
     * @param x           the left x position
     * @param y           the top y position
     * @param width       the width of the tank area
     * @param height      the height of the tank area
     * @param fluidStack  the fluid to render
     * @param capacity    the maximum capacity of the tank (used to calculate fill level)
     */
    public static void renderFluidTank(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height,
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

        // The scissor is transformed by the current pose, so local coordinates are fine here.
        guiGraphics.enableScissor(x, startY, x + width, y + height);
        for (int tileX = 0; tileX < width; tileX += TEX_WIDTH) {
            for (int tileY = 0; tileY < scaledHeight; tileY += TEX_HEIGHT) {
                // Tiles are anchored at the bottom so the cropped tile is the top one, as before.
                int drawY = y + height - tileY - TEX_HEIGHT;
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + tileX, drawY,
                        TEX_WIDTH, TEX_HEIGHT, tintColor);
            }
        }
        guiGraphics.disableScissor();
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
    public static void renderFluid(GuiGraphicsExtractor guiGraphics, int x, int y, FluidStack fluidStack) {
        renderFluidTank(guiGraphics, x, y, TEX_WIDTH, TEX_HEIGHT, fluidStack, fluidStack.getAmount());
    }

    /**
     * Renders a tooltip for a fluid tank when the mouse is hovering over it. Mouse and tank
     * coordinates share the current pose's space; the tooltip is placed under the real cursor.
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
    public static void renderFluidTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY,
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
            tooltip.add(__("tooltip.fluid.flush").withStyle(ChatFormatting.GRAY));
            GuiTooltips.components(guiGraphics, tooltip, mouseX, mouseY);
        }
    }

    public static void renderTankBevel(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
        int shadow = 0xFF373737;
        int highlight = 0xFFFFFFFF;
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y, shadow);
        guiGraphics.fill(x - 1, y - 1, x, y + height + 1, shadow);
        guiGraphics.fill(x - 1, y + height, x + width + 1, y + height + 1, highlight);
        guiGraphics.fill(x + width, y - 1, x + width + 1, y + height + 1, highlight);
    }
}
