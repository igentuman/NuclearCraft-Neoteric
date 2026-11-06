package igentuman.nc.recipes;

import igentuman.nc.block.crafter.entity.EngineersCrafterBE;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.client.NcClient;
import igentuman.nc.handler.crafter.ProcessorRecipes;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.recipes.type.NcRecipe;
import java.util.NoSuchElementException;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.server.ServerLifecycleHooks;

import static igentuman.nc.util.NcUtils.rlFromString;

public class RecipeInfo implements INBTSerializable<Tag> {

    private ProcessingOrder craftingOrder;
    public int ticks = 0;
    public int parallelProcessing = 1;
    public double ticksProcessed = 0;
    public double energy = 0;
    public double heat = 0;
    public double radiation = 0;
    public boolean stuck = false;
    public NcRecipe recipe;
    public BlockEntity be;
    public String recipeId;
    private SidedContentHandler contentHandler;

    public boolean canAcceptCraftingOrder() {
        return craftingOrder == null && recipe == null && !stuck && ticks == 0;
    }

    public boolean reserveCrafting(UUID id, BlockPos crafter, String selectedRecipe, int operations) {
        if (!canAcceptCraftingOrder() || operations <= 0 || !(be instanceof NCProcessorBE proc)
                || !ProcessorRecipes.isAllowed(proc.getName())) return false;
        NcRecipe selected = ProcessorRecipes.byId(be.getLevel().getRecipeManager(), ResourceLocation.tryParse(selectedRecipe));
        if (selected == null || !selected.getCodeId().equals(proc.getName())) return false;
        craftingOrder = new ProcessingOrder(id, crafter, selectedRecipe, operations);
        be.setChanged();
        return true;
    }

    public boolean hasCraftingOrder() { return craftingOrder != null; }
    public ProcessingOrder craftingOrder() { return craftingOrder; }
    public boolean isReservedBy(UUID id) { return craftingOrder != null && craftingOrder.jobId().equals(id); }
    public boolean isCraftingComplete(UUID id) { return isReservedBy(id) && craftingOrder.isComplete(); }
    public void releaseCrafting(UUID id) {
        if (isReservedBy(id)) {
            craftingOrder = null;
            if (be != null) be.setChanged();
        }
    }

    public boolean validateCraftingOwner() {
        if (craftingOrder == null) return true;
        if (be == null || be.getLevel() == null || !be.getLevel().isLoaded(craftingOrder.crafter())) return false;
        if (!(be.getLevel().getBlockEntity(craftingOrder.crafter()) instanceof EngineersCrafterBE crafter)
                || !crafter.ownsProcessor(craftingOrder.jobId(), be.getBlockPos())) releaseCrafting(craftingOrder.jobId());
        return true;
    }

    public void setRecipe(NcRecipe recipe) {
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
        data.putInt("parallelProcessing", parallelProcessing);
        if (craftingOrder != null) data.put("CraftingOrder", craftingOrder.save());
        if(recipe != null) {
            data.putString("recipe", recipeId);
        }
        return data;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if(nbt instanceof CompoundTag) {
            craftingOrder = ((CompoundTag) nbt).contains("CraftingOrder") ? ProcessingOrder.load(((CompoundTag) nbt).getCompound("CraftingOrder")) : null;
            parallelProcessing = Math.max(1, ((CompoundTag) nbt).getInt("parallelProcessing"));
            ticks = ((CompoundTag) nbt).getInt("ticks");
            ticksProcessed = ((CompoundTag) nbt).getDouble("ticksProcessed");
            energy = ((CompoundTag) nbt).getDouble("energy");
            heat = ((CompoundTag) nbt).getDouble("heat");
            radiation = ((CompoundTag) nbt).getDouble("radiation");
            stuck = ((CompoundTag) nbt).getBoolean("stuck");
            recipeId = ((CompoundTag) nbt).getString("recipe");
            recipe = null;
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

    private NcRecipe getRecipeFromTag(String recipe) {
        ResourceLocation id = rlFromString(recipe);
        if(getLevel() == null) return null;
        try {
            return (NcRecipe) getLevel().getRecipeManager().byKey(id).get();
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
        if(isStuck() && isCompleted()) return false;
        //todo this is stupid hack
        if(Double.isNaN(ticksProcessed)) {
            ticksProcessed = 0;
        }
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

    public NcRecipe recipe() {
        if(recipe == null && recipeId != null && !recipeId.isEmpty()) {
            recipe = getRecipeFromTag(recipeId);
        }
        return recipe;
    }

    public void setParallelProcessing(int i) {
        this.parallelProcessing = howMuchICanProcess(i);
    }

    private int howMuchICanProcess(int i) {
        if(i == 1 || recipe == null) return 1;
        SidedContentHandler handler = getContentHandler();
        if(handler == null) return 1;

        int actualAmount = 1;
        // Validate how many parallel recipes can actually be processed based on available inputs
        // Check from the requested amount down to find the maximum feasible parallel processing
        for(int attempt = i; attempt > 1; attempt--) {
            if(recipe.hasEnoughToConsume(handler, attempt) && recipe.hasRoomForOutputs(handler, attempt)) {
                actualAmount = attempt;
                break;
            }
        }
        return Math.min(actualAmount, i);
    }

    private SidedContentHandler getContentHandler() {
        if(be instanceof NuclearCraftBE ncbe) {
            return ncbe.contentHandler();
        }
        return contentHandler;
    }

    public boolean consumeInputs(SidedContentHandler contentHandler) {
        if(recipe != null) {
            return recipe.consumeInputs(contentHandler, parallelProcessing);
        }
        return false;
    }

    public boolean handleOutputs(SidedContentHandler contentHandler) {
        if(recipe != null) {
            for (int i = 0; i < parallelProcessing; i++) {
                boolean result = recipe.handleOutputs(contentHandler);
                //at this point we only care if first output was successful
                if (i == 0 && !result) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setContentHandler(SidedContentHandler sidedContentHandler) {
        this.contentHandler = sidedContentHandler;
    }
}
