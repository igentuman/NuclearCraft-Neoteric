package igentuman.nc.block.entity.processor;

import igentuman.api.nc.Processor;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.compat.cc.ProcessorPeripheral;
import igentuman.nc.compat.oc2.ProcessorDevice;
import igentuman.nc.handler.CatalystHandler;
import igentuman.nc.handler.UpgradesHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.content.processors.ProcessorPrefab;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.block.ProcessorBlock.ACTIVE;
import static igentuman.nc.compat.oc2.ProcessorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.handler.config.ProcessorsConfig.PROCESSOR_CONFIG;
import static igentuman.nc.setup.registration.NCItems.NC_ITEMS;
import static igentuman.nc.util.ModUtil.*;

public class NCProcessorBE extends NuclearCraftBE implements Processor {

    public String NAME;
    public final SidedContentHandler contentHandler;
    protected final CustomEnergyStorage energyStorage;
    public final HashMap<String, NcRecipe> cachedRecipes = new HashMap<>();
    public final UpgradesHandler upgradesHandler;
    protected final LazyOptional<IItemHandler> handler;
    public final CatalystHandler catalystHandler;
    public int manualUpdateCounter = 40;
    protected int skippedTicks = 1;
    protected LazyOptional<ProcessorPeripheral> peripheralCap;
    protected final LazyOptional<IEnergyStorage> energy;
    protected long lastTickTime = 0;

    @NBTField
    public int speedMultiplier = 1;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public int energyMultiplier = 1;
    @NBTField
    public int redstoneMode = 0;
    @NBTField
    public boolean isActive = false;

    protected List<ItemStack> allowedInputItems;
    protected List<FluidStack> allowedInputFluids;
    protected List<FluidStack> allowedOutputFluids;
    protected ParticleOptions particle1 = ParticleTypes.SMOKE;
    protected ProcessorPrefab<?,?> prefab;

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    public NCProcessorBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(NCProcessors.PROCESSORS_BE.get(name).get(), pPos, pBlockState);
        NAME = name;
        prefab = Processors.all().get(name);
        contentHandler = new SidedContentHandler(
                prefab().getSlotsConfig().getInputItems(), prefab().getSlotsConfig().getOutputItems(),
                prefab().getSlotsConfig().getInputFluids(), prefab().getSlotsConfig().getOutputFluids());
        contentHandler().setBlockEntity(this);
        energyStorage = createEnergy();
        energyStorage.setInputEnergyTier(GTCEU_CONFIG.PROCESSOR_ENERGY_TIER.get().ordinal());
        energyStorage.setOutputEnergyTier(GTCEU_CONFIG.PROCESSOR_ENERGY_TIER.get().ordinal());
        energy = LazyOptional.of(() -> energyStorage);
        upgradesHandler = createUpgradesHandler();
        handler = LazyOptional.of(() -> upgradesHandler);
        catalystHandler = createCatalystHandler();
        contentHandler().setAllowedInputItems(this::getAllowedInputItems);
        for(int i = 0; i < prefab().getSlotsConfig().getInputFluids(); i++) {
            contentHandler().setAllowedInputFluids(i, this::getAllowedInputFluids);
        }
        recipeInfo().setContentHandler(contentHandler());
    }

    public void handleOverVoltage() {
        if(GTCEU_CONFIG.OVERCHARGE_EXPLOSIONS.get()) {
            level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 2F, true, Explosion.BlockInteraction.BREAK);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public SidedContentHandler contentHandler() {
        return contentHandler;
    }

    @Override
    public UpgradesHandler upgradesHandler() {
        return upgradesHandler;
    }

    @Override
    public CatalystHandler catalystHandler() {
        return catalystHandler;
    }

    @Override
    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }

    public ProcessorPrefab<?,?> prefab() {
        if(prefab == null) {
            prefab = Processors.all().get(getName());
        }
        return prefab;
    }

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new ProcessorPeripheral(this));
        }
        return peripheralCap.cast();
    }

    @Override
    public ItemCapabilityHandler getItemInventory() {
        return contentHandler().itemHandler;
    }

    public void updateRecipe() {
        //check if last recipe is still valid
        if(recipe != null) {
            if(recipe.test(contentHandler())) {
                recipeInfo().setContentHandler(contentHandler());
                recipeInfo().ticksProcessed = 0;
                recipeInfo().setParallelProcessing(parallelRecipes());
                if (recipeInfo().consumeInputs(contentHandler())) {
                    return;
                }
                recipe = null;
                recipeInfo().clear();
            } else {
                recipeInfo().clear();
            }
        }
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo().setRecipe(recipe);
            recipeInfo().ticks = (int) (getBaseProcessTime() * recipe.getTimeModifier());
            recipeInfo().energy = getBasePower() * recipe.getEnergy();
            recipeInfo().radiation = recipeInfo().recipe.getRadiation();
            recipeInfo().be = this;
            recipeInfo().setParallelProcessing(parallelRecipes());
            if (!recipeInfo().consumeInputs(contentHandler())) {
                recipe = null;
                recipeInfo().clear();
            }
        } else {
            recipeInfo().clear();
        }
    }

    protected void addToCache(NcRecipe recipe) {
        String key = contentHandler().getCacheKey();
        if(cachedRecipes.containsKey(key)) {
            cachedRecipes.replace(key, recipe);
        } else {
            cachedRecipes.put(key, recipe);
        }
    }

    public NcRecipe getRecipe() {
        if(isInputEmpty()) return null;
        NcRecipe cachedRecipe = getCachedRecipe();
        if(cachedRecipe != null) return cachedRecipe;
        if(!NcRecipeType.ALL_RECIPES.containsKey(getName())) return null;
        for(NcRecipe recipe: NcRecipeType.getAllRecipesFor(getName(), getLevel())) {
            if(recipe.test(contentHandler())) {
                addToCache(recipe);
                return recipe;
            }
        }
        return null;
    }

    private boolean isInputEmpty() {
        return contentHandler().isInputEmpty();
    }

    public NcRecipe getCachedRecipe() {
        String key = contentHandler().getCacheKey();
        if(cachedRecipes.containsKey(key)) {
            if(cachedRecipes.get(key).test(contentHandler())) {
                return cachedRecipes.get(key);
            }
        }
        return null;
    }

    public int getBaseProcessTime() {
        return prefab().config().getTime();
    }

    public int getBasePower() {
        return prefab().config().getPower();
    }

    public void handleRecipeOutput() {
        if (hasRecipe() && (recipeInfo().isCompleted() || recipeInfo().isStuck())) {
            if (recipeInfo().handleOutputs(contentHandler())) {
                recipeInfo().stuck = false;
                updateRecipe();
            } else {
                recipeInfo().stuck = true;
            }
        }
    }

    public int parallelRecipes()
    {
        if(!prefab().supportSpeedUpgrade) return 1;
        int id = prefab().supportEnergyUpgrade ? 1 : 0;
        ItemStack upgrade = upgradesHandler().getStackInSlot(id);
        if(upgrade.isEmpty()) return 1;
        if (upgrade.is(NC_ITEMS.get("upgrade_stack").get())) {
            return (int) (Math.min(32, Math.ceil(upgrade.getCount() / 4D)));
        }
        if (upgrade.is(NC_ITEMS.get("upgrade_quantum").get())) {
            return upgrade.getCount();
        }
        return 1;
    }

    public double speedMultiplier()
    {
        if(!prefab().supportSpeedUpgrade) return 1;
        int id = prefab().supportEnergyUpgrade ? 1 : 0;
        speedMultiplier = upgradesHandler().getStackInSlot(id).getCount()+1;
        if (upgradesHandler().getStackInSlot(id).is(NC_ITEMS.get("upgrade_quantum").get())) {
            return speedMultiplier*5;
        }
        return speedMultiplier;
    }

    public int energyPerTick()
    {
        double energy = recipe == null ? 1 : recipe.getEnergy();
        energyPerTick = (int) (energy*energyMultiplier()*prefab().config().getPower());
        return energyPerTick;
    }

    public boolean recipeIsStuck() {
        if (recipeInfo().isCompleted() || recipeInfo().recipe == null) {
            handleRecipeOutput();
        }
        return false;
    }

    public boolean hasRecipe() {
        return recipeInfo().recipe != null;
    }

    public int getEnergyCapacity()
    {
        return prefab().config().getPower()*5000;
    }

    protected CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(getEnergyCapacity(), 1000000000, 0) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    public CatalystHandler createCatalystHandler() {
        return new CatalystHandler(this);
    }

    public UpgradesHandler createUpgradesHandler() {
        return new UpgradesHandler(this);
    }

    protected boolean gtEUSupported()
    {
        return PROCESSOR_CONFIG.GT_SUPPORT.get() == 2 || PROCESSOR_CONFIG.GT_SUPPORT.get() == 1;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY && PROCESSOR_CONFIG.GT_SUPPORT.get() != 2) {
            if(prefab().config().getPower() > 0) {
                return getEnergy().cast();
            }
            return LazyOptional.empty();
        }

        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return getPeripheral(cap, side);
            }
        }

        if(isOC2Loaded()) {
            if(cap == DEVICE_CAPABILITY) {
                return getOCDevice(cap, side);
            }
        }

        return super.getCapability(cap, side);
    }

    private <T> LazyOptional<T> getOCDevice(Capability<T> cap, Direction side) {
        return LazyOptional.of(() -> ProcessorDevice.createDevice(this)).cast();
    }

    public void tickClient() {
        if(isActive && level.getRandom().nextInt(50) < 5) {
            BlockPos pos = worldPosition;
            Direction direction = getFacing();
            Direction.Axis direction$axis = direction.getAxis();
            double d0 = (double)pos.getX() + 0.5D;
            double d1 = (double)pos.getY();
            double d2 = (double)pos.getZ() + 0.5D;
            double d3 = 0.52D;
            double d4 = level.getRandom().nextDouble() * 0.6D - 0.3D;
            double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52D : d4;
            double d6 = level.getRandom().nextDouble() * 6.0D / 16.0D;
            double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52D : d4;
            level.addParticle(particle1, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0, 0.0D);
            level.addParticle(DustParticleOptions.REDSTONE, d0 + d5, d1 + d6, d2 + d7, 0, 0, 0);
        }
    }

    public List<ItemStack> getAllowedInputItems()
    {
        if(allowedInputItems == null) {
            allowedInputItems = new ArrayList<>();
            for(AbstractRecipe recipe: NcRecipeType.getAllRecipesFor(getName(), getLevel())) {
                for(Ingredient ingredient: recipe.getItemIngredients()) {
                    allowedInputItems.addAll(List.of(ingredient.getItems()));
                }
            }
        }
        return allowedInputItems;
    }

    protected int howMuchICanSkip()
    {
        if(energyPerTick() == 0) {
            return PROCESSOR_CONFIG.SKIP_TICKS.get();
        }
        return Math.min(((int)(energyStorage().getEnergyStored() / energyPerTick)),PROCESSOR_CONFIG.SKIP_TICKS.get());
    }

    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        if(redstoneMode == 1 && level.getGameTime() % 5 == 0 && !hasRedstoneSignal()) return;
        if(howMuchICanSkip() >= skippedTicks) {
            skippedTicks++;
            return;
        }
        boolean updated = forceUpdate();
        boolean wasActive = isActive;
        //if no recipe - tick only 5 times per second
        if(!hasRecipe() && level.getGameTime() % 5 == 0) {
           return;
        }
        processRecipe();
        if(lastTickTime == level.getGameTime()) {
            //prevent double tick in case of block boosters like torcherino
            //but we allow recipe progression boost
            return;
        }
        lastTickTime = level.getGameTime();
        handleRecipeOutput();
        updated = updated || contentHandler().tick();
        if(updated || wasUpdated) {
            energyStorage().setMaxCapacity(getEnergyCapacity() * Math.max(1, getEnergyUpgrades()/10));
            if (wasActive != isActive) {
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ACTIVE, isActive));
            }
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(ACTIVE, isActive), Block.UPDATE_NEIGHBORS);
        }
        skippedTicks = 1;
    }

    public List<FluidStack> getAllowedInputFluids()
    {
        if(allowedInputFluids == null) {
            allowedInputFluids = new ArrayList<>();
            for(NcRecipe recipe: NcRecipeType.getAllRecipesFor(getName(), getLevel())) {
                for(FluidStackIngredient ingredient: recipe.getInputFluids()) {
                    allowedInputFluids.addAll(ingredient.getRepresentations());
                }
            }
        }
        return allowedInputFluids;
    }

    private boolean forceUpdate() {
        if(lastTickTime == level.getGameTime()) return false;
        if(manualUpdateCounter > 0) {
            manualUpdateCounter--;
            return false;
        }
        manualUpdateCounter = 80;
        saveSideMapFlag = true;
        energyStorage().wasUpdated = true;
        upgradesHandler().wasUpdated = true;
        catalystHandler().wasUpdated = true;
        contentHandler().setAllowedInputItems(this::getAllowedInputItems);
        for(int i = 0; i < prefab().getSlotsConfig().getInputFluids(); i++) {
            contentHandler().setAllowedInputFluids(i, this::getAllowedInputFluids);
        }
        for(int i = prefab().getSlotsConfig().getInputFluids(); i < prefab().getSlotsConfig().getOutputFluids() + prefab().getSlotsConfig().getInputFluids(); i++) {
            contentHandler().setAllowedInputFluids(i, this::getAllowedOutputFluids);
        }
        return true;
    }

    private List<FluidStack> getAllowedOutputFluids() {
        if(allowedOutputFluids == null) {
            allowedOutputFluids = new ArrayList<>();
            for(NcRecipe recipe: NcRecipeType.getAllRecipesFor(getName(), getLevel())) {
                allowedOutputFluids.addAll(recipe.getOutputFluids());
            }
        }
        return allowedOutputFluids;
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }

    public void processRecipe() {
        if(!hasRecipe()) {
            updateRecipe();
        }
        if(!hasRecipe()) {
            isActive = false;
            return;
        }

        if(energyStorage().getEnergyStored() < energyPerTick*skippedTicks) {
            isActive = false;
            return;
        }
        if(!canProcessRecipe()) {
            return;
        }
        recipeInfo().process(speedMultiplier()*skippedTicks);
        if(recipeInfo().radiation != 1D) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), (recipeInfo().radiation/1000000)*speedMultiplier()*skippedTicks, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
        isActive = true;
        setChanged();
        if(!recipeInfo().isCompleted() && hasRecipe()) {
            energyStorage().consumeEnergy(energyPerTick*skippedTicks);
        }
    }

    protected boolean canProcessRecipe() {
        return true;
    }

    public int getEnergyUpgrades()
    {
        if(!prefab().supportEnergyUpgrade) return 1;
        return upgradesHandler().getStackInSlot(0).getCount()+1;
    }

    public int energyMultiplier() {
        double speedMult = Math.min(speedMultiplier() + ((parallelRecipes()-1) / 2D), 100);
        energyMultiplier = (int) Math.max(speedMult, Math.pow(speedMult-1, 2)+speedMult-Math.pow(getEnergyUpgrades(),2));
        return energyMultiplier;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        contentHandler().invalidate();
        energy.invalidate();
    }

    public double getProgress() {
        return recipeInfo().getProgress();
    }

    public int toggleSideConfig(int slotId, int direction) {
        setChanged();
        saveSideMapFlag = true;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        return contentHandler().toggleSideConfig(slotId, direction);
    }

    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public SlotModePair.SlotMode getSlotMode(int direction, int slotId) {
        return contentHandler().getSlotMode(direction, slotId);
    }

    public FluidTank getFluidTank(int i) {
        return contentHandler().fluidHandler.tanks.get(i);
    }

    public void toggleRedstoneMode() {
        redstoneMode++;
        if (redstoneMode > 1) redstoneMode = 0;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public CompoundTag getTagForStack() {
        CompoundTag data = new CompoundTag();
        contentHandler().saveSideMap();
        data.put("Content", contentHandler().serializeNBT());
        data.put("Energy", energyStorage().serializeNBT());
        CompoundTag infoTag = new CompoundTag();
        saveFullTagData(infoTag);
        infoTag.put("upgrades", upgradesHandler().serializeNBT());
        infoTag.put("catalyst", catalystHandler().serializeNBT());
        infoTag.put("recipeInfo", recipeInfo().serializeNBT());
        infoTag.putInt("energy", energyStorage().getEnergyStored());
        data.put("Info", infoTag);
        return data;
    }

    public List<Item> getAllowedCatalysts() {
        return List.of();
    }

    public int getRecipeProgress() {
        if(hasRecipe()) {
            return (int) (recipeInfo().getProgress()*100);
        }
        return 0;
    }

    public int getSlotsCount() {
        return prefab().getSlotsConfig().slotsCount();
    }

    public void voidSlotContent(int id) {
        if(id < 0 || id >= getSlotsCount()) return;
        contentHandler().voidSlot(id);
    }

    public Object[] getSlotContent(int id) {
        if(id < 0 || id >= getSlotsCount()) return new Object[]{};
        return contentHandler().getSlotContent(id);
    }

    public void voidFluidSlot(int slotId) {
        if(contentHandler() != null) {
            contentHandler().voidFluidSlot(slotId);
        }
    }

    public void handleFluidItemClick(int slotId, ItemStack stack, ServerPlayer player) {
        if(contentHandler() != null) {
            contentHandler().handleFluidItemClick(slotId, stack, player);
        }
    }

    public boolean isInputAllowed(ItemStack stack) {
        for(ItemStack allowed: getAllowedInputItems()) {
            if(ItemStack.isSame(allowed, stack)) {
                return true;
            }
        }
        return false;
    }

    public List<Item> getAllowedItems(int idx) {
        if(contentHandler().itemHandler.validItemsForSlot.containsKey(idx)) {
            return contentHandler().itemHandler.validItemsForSlot.get(idx);
        }
        List<Item> allowedItems = new ArrayList<>();
        for(ItemStack stack: getAllowedInputItems()) {
            allowedItems.add(stack.getItem());
        }
        return allowedItems;
    }

    public void upgradesUpdated() {
        int tier = GTCEU_CONFIG.PROCESSOR_ENERGY_TIER.get().ordinal()+(getEnergyUpgrades()-1)/GTCEU_CONFIG.ENERGY_UPGRADES_NEEDED_TO_NEXT_TIER.get();
        energyStorage().setInputEnergyTier(tier).setOutputEnergyTier(tier);
        setChanged();
    }

    public int getTier() {
        return GTCEU_CONFIG.PROCESSOR_ENERGY_TIER.get().ordinal()+(getEnergyUpgrades()-1)/GTCEU_CONFIG.ENERGY_UPGRADES_NEEDED_TO_NEXT_TIER.get();
    }


}
