package igentuman.nc.recipes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import static igentuman.nc.recipes.NcRecipe.getRecipeFromTag;

public class RecipeInfo implements INBTSerializable<Tag> {
    public int ticks = 0;
    public double ticksProcessed = 0;
    public double energy = 0;
    public double heat = 0;
    public double radiation = 0;

    public void setRecipe(INCRecipe recipe) {
        this.recipe = recipe;
    }

    public INCRecipe recipe;

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
            data.put("recipe", recipe.serialize());
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
            recipe = getRecipeFromTag((CompoundTag) ((CompoundTag) nbt).get("recipe"));

        }
    }

    public double getProgress() {
        if(ticks > 0) {
            return ((double)ticksProcessed)/ticks;
        }
        return 0;
    }

    public void process(double multiplier) {
        ticksProcessed+=1*multiplier;
        ticksProcessed = Math.min(ticks, ticksProcessed);
    }

    public void reset() {
        this.recipe = null;
        ticks = 0;
        heat = 0;
        energy = 0;
        radiation = 0;
        ticksProcessed = 0;
    }
}
