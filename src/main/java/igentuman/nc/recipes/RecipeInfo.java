package igentuman.nc.recipes;

import igentuman.nc.client.NcClient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.NoSuchElementException;


public class RecipeInfo <RECIPE extends AbstractRecipe> implements INBTSerializable<CompoundNBT> {
    public int ticks = 0;
    public double ticksProcessed = 0;
    public double energy = 0;
    public double heat = 0;
    public double radiation = 0;
    public boolean stuck = false;
    public RECIPE recipe;
    public TileEntity be;
    private String recipeId;

    public void setRecipe(RECIPE recipe) {
        this.recipe = recipe;
        recipeId = recipe.getId().toString();
    }

    public boolean isCompleted() {
        return ticksProcessed >= ticks && ticks != 0;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT data = new CompoundNBT();
        data.putInt("ticks", ticks);
        data.putDouble("ticksProcessed", ticksProcessed);
        data.putDouble("energy", energy);
        data.putDouble("heat", heat);
        data.putDouble("radiation", radiation);
        data.putBoolean("stuck", stuck);
        if(recipe != null) {
            data.putString("recipe", recipeId);
        }
        return data;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if(nbt instanceof CompoundNBT) {
            ticks = ((CompoundNBT) nbt).getInt("ticks");
            ticksProcessed = ((CompoundNBT) nbt).getDouble("ticksProcessed");
            energy = ((CompoundNBT) nbt).getDouble("energy");
            heat = ((CompoundNBT) nbt).getDouble("heat");
            radiation = ((CompoundNBT) nbt).getDouble("radiation");
            stuck = ((CompoundNBT) nbt).getBoolean("stuck");
            recipeId = ((CompoundNBT) nbt).getString("recipe");
            recipe = null;
            if(!recipeId.isEmpty()) {
                recipe = getRecipeFromTag(recipeId);
            }
        }
    }

    private World getLevel()
    {
        if(be != null) return be.getLevel();
        return DistExecutor.safeRunForDist(
                () -> NcClient::tryGetClientWorld,
                () -> () -> ServerLifecycleHooks.getCurrentServer().overworld());
    }

    private RECIPE getRecipeFromTag(String recipe) {
        ResourceLocation id = new ResourceLocation(recipe);
        if(getLevel() == null) return null;
        try {
            return (RECIPE) getLevel().getRecipeManager().byKey(id).get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public double getProgress() {
        if(ticks > 0) {
            return ticksProcessed/ticks;
        }
        return 0;
    }

    public boolean process(double multiplier) {
        if(isStuck()) return false;
        ticksProcessed+=1*Math.abs(multiplier);
        ticksProcessed = Math.min(ticks, ticksProcessed);
        return true;
    }

    public void clear() {
        recipe = null;
        recipeId = "";
        ticks = 0;
        heat = 0;
        energy = 0;
        radiation = 0;
        stuck = false;
        ticksProcessed = 0;
    }

    public boolean isStuck() {
        return stuck;
    }

    public RECIPE recipe() {
        if(recipe == null && recipeId != null && !recipeId.isEmpty()) {
            recipe = getRecipeFromTag(recipeId);
        }
        return recipe;
    }
}
