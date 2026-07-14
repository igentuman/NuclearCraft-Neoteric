package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.NuclearCraft;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.util.MultiblockStructure;
import igentuman.nc.util.StructurePreviewRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** EMI recipe rendering an interactive 3D preview of a multiblock's example structure. */
public class MultiblockExampleEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 140;
    private static final int HEIGHT = 100;

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final MultiblockEntry entry;
    private final MultiblockStructure structure;

    public MultiblockExampleEmiRecipe(EmiRecipeCategory category, MultiblockEntry entry, MultiblockStructure structure) {
        this.category = category;
        this.entry = entry;
        this.structure = structure;
        this.id = NuclearCraft.rl("multiblock_examples/" + entry.name());
    }

    @Override
    public EmiRecipeCategory getCategory() { return category; }

    @Override
    public @Nullable ResourceLocation getId() { return id; }

    @Override
    public List<EmiIngredient> getInputs() { return List.of(); }

    @Override
    public List<EmiStack> getOutputs() { return List.of(); }

    @Override
    public int getDisplayWidth() { return WIDTH; }

    @Override
    public int getDisplayHeight() { return HEIGHT; }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        Component name = Component.translatable("multiblock." + NuclearCraft.MODID + "." + entry.name());
        widgets.addText(name, 2, 2, 0xFFFFFFFF, false);

        widgets.addDrawable(0, 12, WIDTH, HEIGHT - 14, (graphics, mouseX, mouseY, delta) ->
                StructurePreviewRenderer.render(graphics, structure, 0, 0, WIDTH, HEIGHT - 14, mouseX, mouseY));

        Component dims = Component.literal(
                structure.getWidth() + "x" + structure.getHeight() + "x" + structure.getDepth());
        int w = Minecraft.getInstance().font.width(dims);
        widgets.addText(dims, WIDTH - w - 2, HEIGHT - 10, 0xFFAAAAAA, false);
    }
}
