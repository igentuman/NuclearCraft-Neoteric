package igentuman.nc.recipes;

import igentuman.nc.client.NcClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;


public class RecipeInfo <RECIPE extends NcRecipe> implements INBTSerializable<Tag> {
    public int ticks = 0;
    public double ticksProcessed = 0;
    public double energy = 0;
    public double heat = 0;
    public double radiation = 0;
    public RECIPE recipe;
    public BlockEntity be;
    public ItemStack[] inputItems;
    public ItemStack[] outputItems;

    public void setRecipe(RECIPE recipe) {
        this.recipe = recipe;
    }

    public boolean isCompleted() {
        return ticksProcessed >= ticks && ticks != 0;
    }

    @Override
    public Tag serializeNBT() {
        CompoundTag data = new CompoundTag();
        data.putInt("ticks", ticks);
        data.putDouble("ticksProcessed", ticksProcessed);
        data.putDouble("energy", energy);
        data.putDouble("heat", heat);
        data.putDouble("radiation", radiation);
        if(recipe != null) {
            data.putString("recipe", recipe.getId().toString());
            //data.putString("recipeType", recipe.getType().toString());
        }
        return data;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if(nbt instanceof CompoundTag) {
            ticks = ((CompoundTag) nbt).getInt("ticks");
            ticksProcessed = ((CompoundTag) nbt).getDouble("ticksProcessed");
            energy = ((CompoundTag) nbt).getDouble("energy");
            heat = ((CompoundTag) nbt).getDouble("heat");
            radiation = ((CompoundTag) nbt).getDouble("radiation");
            String recipeId = ((CompoundTag) nbt).getString("recipe");
            //String recipeType = ((CompoundTag) nbt).getString("recipeType");
            if(!recipeId.isEmpty()) {
                recipe = getRecipeFromTag(recipeId);

            }
        }
    }

    private Level getLevel()
    {
        return DistExecutor.unsafeRunForDist(
                () -> NcClient::tryGetClientWorld,
                () -> () -> ServerLifecycleHooks.getCurrentServer().overworld());
    }

    private RECIPE getRecipeFromTag(String recipe) {
        ResourceLocation id = new ResourceLocation(recipe);
        return (RECIPE) getLevel().getRecipeManager().byKey(id).get();
    }

    public double getProgress() {
        if(ticks > 0) {
            return ticksProcessed/ticks;
        }
        return 0;
    }

    public void process(double multiplier) {
        ticksProcessed+=1*Math.abs(multiplier);
        ticksProcessed = Math.min(ticks, ticksProcessed);
    }

    public void clear() {
        this.recipe = null;
        ticks = 0;
        heat = 0;
        energy = 0;
        radiation = 0;
        ticksProcessed = 0;
    }
}
