package igentuman.nc.compat.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.NuclearCraft;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.NC_FOOD;
import static igentuman.nc.util.TextUtils.__;

@SuppressWarnings("removal")
public class MultiblockStructureCategory implements IRecipeCategory<MultiblockStructureRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MODID, "multiblock_structure");
    public static final RecipeType<MultiblockStructureRecipe> TYPE = RecipeType.create(MODID, "multiblock_structure", MultiblockStructureRecipe.class);
    private boolean isMouseDragging = false;
    private double lastMouseX = 0;
    private float manualRotationAngle = 0;
    private long mouseReleaseTime = 0;
    private float autoRotationSpeed = (float)(Math.PI * 0.1);
    private double lastMouseY = 0;
    private float manualTiltAmount = 0.20f;
    private boolean sliceMode = false;
    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    private final MultiblockRenderer renderer;

    public MultiblockStructureCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 120);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(NC_FOOD.get("moresmore").get()));
        this.title = Component.translatable("jei.category." + MODID + ".multiblock_structure");
        this.renderer = new MultiblockRenderer();
    }
    
    @Override
    public RecipeType<MultiblockStructureRecipe> getRecipeType() {
        return TYPE;
    }
    
    @Override
    public Component getTitle() {
        return title;
    }
    
    @Override
    public IDrawable getBackground() {
        return background;
    }
    
    @Override
    public IDrawable getIcon() {
        return icon;
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MultiblockStructureRecipe recipe, IFocusGroup focuses) {
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredients(recipe.getIngredients());
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addIngredients(recipe.getIngredients());
    }
    
    @Override
    public void draw(MultiblockStructureRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        // Draw structure name
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, __(recipe.getName()), 5, 2, 0xFFFFFFFF);
        long window = Minecraft.getInstance().getWindow().getWindow();
        boolean leftMouseDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT)
                == GLFW.GLFW_PRESS;
        long currentTime = System.currentTimeMillis();
        if (mouseReleaseTime == 0) {
            mouseReleaseTime = currentTime;
        }
        float angle;
        if (isMouseDragging) {
            angle = manualRotationAngle;
        } else {
            float timeDiff = (currentTime - mouseReleaseTime) / 1000.0f;
            angle = manualRotationAngle + (timeDiff * autoRotationSpeed);
            if (angle > Math.PI * 2) {
                angle %= (2.0f * (float)Math.PI);
                manualRotationAngle = angle;
                mouseReleaseTime = currentTime;
            }
        }

        if (leftMouseDown && !isMouseDragging && isMouseInRotationArea(mouseX, mouseY)) {
            isMouseDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            manualRotationAngle = angle;
        }
        // Render multiblock structure
        graphics.pose().pushPose();
        graphics.pose().translate(80, 75, 100);
        float scale = 70.0f;
        graphics.pose().scale(scale, -scale, scale);

        if (isMouseDragging && !leftMouseDown) {
            isMouseDragging = false;
            mouseReleaseTime = currentTime;
        }
        if(isMouseDragging) {
            float sensitivity = 0.02f;
            float delta = (float) (mouseX - lastMouseX) * sensitivity;
            manualRotationAngle += delta;

            float tiltSensitivity = 0.02f;
            float tiltDelta = (float) (mouseY - lastMouseY) * tiltSensitivity;
            manualTiltAmount += tiltDelta;
            manualTiltAmount = Math.max(-0.5f, Math.min(0.5f, manualTiltAmount));

            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        graphics.pose().mulPose(new Quaternionf().rotationY(angle));
        float xTilt = manualTiltAmount * (float)Math.cos(angle);
        float zTilt = manualTiltAmount * (float)Math.sin(angle);
        graphics.pose().mulPose(new Quaternionf().rotationX(xTilt));
        graphics.pose().mulPose(new Quaternionf().rotationZ(zTilt));

        if(sliceMode) {
            recipe.slice();
            sliceMode = false;
        }
        renderer.render(recipe.getStructure(), graphics.pose(), recipe.currentLayer);

        graphics.pose().popPose();
    }
    
    // Inner class to handle rendering of the multiblock structure
    private static class MultiblockRenderer {

        public void render(MultiblockStructure structure, PoseStack stack) {
            render(structure, stack, Integer.MAX_VALUE);
        }

        public void render(MultiblockStructure structure, PoseStack stack, int maxLayer) {
            Minecraft minecraft = Minecraft.getInstance();
            BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();

            // Get all blocks and calculate structure center for better positioning
            Map<BlockPos, BlockState> blocks = structure.getBlocks();
            if (blocks.isEmpty()) return;

            // Calculate structure dimensions for scaling
            int width = structure.getWidth();
            int height = structure.getHeight();
            int depth = structure.getDepth();
            float scale = 1.0f / Math.max(Math.max(width, height), depth);

            // Apply scaling to fit the structure in view
            stack.scale(scale, scale, scale);

            // Center the structure
            float centerX = structure.getMinX() + width / 2.0f;
            float centerY = structure.getMinY() + height / 2.0f;
            float centerZ = structure.getMinZ() + depth / 2.0f;
            stack.translate(-centerX, -centerY, -centerZ);

            // Set up rendering
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            RandomSource random = RandomSource.create();

            // Render each block
            for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                BlockPos pos = entry.getKey();
                if (pos.getY() > maxLayer) {
                    continue;
                }
                BlockState state = entry.getValue();

                stack.pushPose();
                stack.translate(pos.getX(), pos.getY(), pos.getZ());

                blockRenderer.renderSingleBlock(
                        state,
                        stack,
                        bufferSource,
                        15728880,
                        OverlayTexture.NO_OVERLAY,
                        ModelData.EMPTY,
                        null
                );

                stack.popPose();
            }

            // Finish rendering
            bufferSource.endBatch();
        }
    }

    @Override
    public boolean handleInput(MultiblockStructureRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
        if(input.getType().equals(InputConstants.Type.MOUSE)) {
            mouseClicked(mouseX, mouseY, input.getValue());
            return true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && isMouseInRotationArea(mouseX, mouseY)) {
            sliceMode = true;
            return true;
        }
        return false;
    }


    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isMouseDragging && button == 0) {
            // Convert mouse movement to rotation (horizontal drag = rotation)
            float sensitivity = 0.01f;
            float delta = (float) (mouseX - lastMouseX) * sensitivity;
            manualRotationAngle += delta;
            lastMouseX = mouseX;
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseDragging) {
            isMouseDragging = false;
            return true;
        }
        return false;
    }

    private boolean isMouseInRotationArea(double mouseX, double mouseY) {
        // Define an area where rotation control is active
        // This uses the center of the display area
        double centerX = 80;
        double centerY = 75;
        double radius = 50;
        return Math.pow(mouseX - centerX, 2) + Math.pow(mouseY - centerY, 2) <= Math.pow(radius, 2);
    }
}