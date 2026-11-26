package igentuman.nc.compat;

import igentuman.nc.recipe.particle.TargetChamberRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.Locale;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatParticleEnergy;
import static igentuman.nc.util.TextUtils.numberFormat;

public final class TargetChamberRecipeLayout {

    private static final ResourceLocation TEXTURE = rl("textures/gui/accelerators/target_chamber_controller.png");
    private static final int TEXTURE_HEIGHT = 107;
    private static final int TEXT_COLOR = 0x404040;

    public static final int WIDTH = 160;
    public static final int HEIGHT = 117;
    public static final int INPUT_X = 43;
    public static final int OUTPUT_X = 101;
    public static final int ITEM_Y = 28;
    public static final int FLUID_Y = 45;
    public static final int SLOT_SPACING = 18;
    public static final Vec2 BEAM_POSITION = new Vec2(8, 36);
    public static final List<Vec2> BYPRODUCT_POSITIONS = List.of(
            new Vec2(76, 5), new Vec2(136, 36), new Vec2(76, 68));

    private TargetChamberRecipeLayout() {
    }

    public static void draw(GuiGraphics graphics, TargetChamberRecipe recipe) {
        graphics.blit(TEXTURE, 0, 0, 10, 10, WIDTH, TEXTURE_HEIGHT);
        // Reuse the blank strip below the diagram to extend the background past the inventory crop.
        graphics.blit(TEXTURE, 0, TEXTURE_HEIGHT, 10, 107, WIDTH, HEIGHT - TEXTURE_HEIGHT);

        var font = Minecraft.getInstance().font;
        var input = recipe.particleInput();
        graphics.drawString(font, __("tooltip.nuclearcraft.particlestack.focus", numberFormat(input.minimumFocus())),
                2, 87, TEXT_COLOR, false);
        graphics.drawString(font, __("label.nuclearcraft.cross_section",
                String.format(Locale.ROOT, "%.1f", recipe.crossSection() * 100)), 2, 97, TEXT_COLOR, false);

        String minimumEnergy = formatParticleEnergy(input.minimumEnergyKeV());
        var energyLabel = input.minimumEnergyKeV() == input.maximumEnergyKeV()
                ? __("label.nuclearcraft.energy", minimumEnergy)
                : __("label.nuclearcraft.energy_range", minimumEnergy, formatParticleEnergy(input.maximumEnergyKeV()));
        graphics.drawString(font, energyLabel, 2, 107, TEXT_COLOR, false);
    }
}
