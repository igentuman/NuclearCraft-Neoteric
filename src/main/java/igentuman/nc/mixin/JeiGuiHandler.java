package igentuman.nc.mixin;

import igentuman.nc.client.gui.processor.NCProcessorScreen;
import igentuman.nc.handler.event.client.TickHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.library.load.registration.GuiHandlerRegistration;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;

@Mixin(GuiHandlerRegistration.class)
public abstract class JeiGuiHandler {
/*
    @Shadow
    public abstract <T extends AbstractContainerScreen<?>> void addGuiContainerHandler(Class<? extends T> guiClass, IGuiContainerHandler<T> guiHandler);

    @Inject(method = "addRecipeClickArea", at = @At("HEAD"), cancellable = true, remap = false)
    private <T extends AbstractContainerScreen<?>> void  addRecipeClickArea(Class<? extends T> containerScreenClass, int xPos, int yPos, int width, int height, RecipeType<?>[] recipeTypes, CallbackInfo ci) {
        if(containerScreenClass.equals(NCProcessorScreen.class)) {
            if(recipeTypes[0].getUid().getPath().equals(TickHandler.currentScreenCode)) {
                addGuiContainerHandler(containerScreenClass, new IGuiContainerHandler<T>() {
                    @Override
                    public Collection<IGuiClickableArea> getGuiClickableAreas(T containerScreen, double mouseX, double mouseY) {
                        IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(xPos, yPos, width, height, recipeTypes);
                        return List.of(clickableArea);
                    }
                });
            }
        }
    }*/
}
