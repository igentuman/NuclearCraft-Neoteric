package igentuman.nc.recipes;

import igentuman.nc.client.NcClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.server.ServerLifecycleHooks;


public class RecipeInfo <RECIPE extends AbstractRecipe> implements INBTSerializable<Tag> {
    public int ticks = 0;
    public double ticksProcessed = 0;
    public double energy = 0;
    public double heat = 0;
    public double radiation = 0;
    public boolean stuck = false;
    public RECIPE recipe;
    public BlockEntity be;
    private String recipeId;

    public void setRecipe(RECIPE recipe) {
        this.recipe = recipe;
        recipeId = recipe.getId().toString();
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
        data.putBoolean("stuck", stuck);
        if(recipe != null) {
            data.putString("recipe", recipeId);
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
            stuck = ((CompoundTag) nbt).getBoolean("stuck");
            recipeId = ((CompoundTag) nbt).getString("recipe");
            if(!recipeId.isEmpty()) {
                recipe = getRecipeFromTag(recipeId);
            }
        }
    }

    private Level getLevel()
    {
        if(be != null) return be.getLevel();
        return DistExecutor.unsafeRunForDist(
                () -> NcClient::tryGetClientWorld,
                () -> () -> ServerLifecycleHooks.getCurrentServer().overworld());
    }

    private RECIPE getRecipeFromTag(String recipe) {
        ResourceLocation id = new ResourceLocation(recipe);
        if(getLevel() == null) return null;
        return (RECIPE) getLevel().getRecipeManager().byKey(id).get();
    }

    public double getProgress() {
        if(ticks > 0) {
            return ticksProcessed/ticks;
        }
        return 0;
    }

    public void process(double multiplier) {
        if(isStuck()) return;
        ticksProcessed+=1*Math.abs(multiplier);
        ticksProcessed = Math.min(ticks, ticksProcessed);
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
        if(recipe == null && !recipeId.isEmpty()) {
            recipe = getRecipeFromTag(recipeId);
        }
        return recipe;
    }
}
