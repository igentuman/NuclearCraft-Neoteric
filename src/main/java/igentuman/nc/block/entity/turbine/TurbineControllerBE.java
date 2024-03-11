package igentuman.nc.block.entity.turbine;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.multiblock.turbine.TurbineMultiblock;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.RecipeInfo;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import igentuman.nc.compat.cc.NCTurbinePeripheral;

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.handler.config.TurbineConfig.TURBINE_CONFIG;
import static igentuman.nc.setup.registration.NCSounds.FISSION_REACTOR;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class TurbineControllerBE<RECIPE extends TurbineControllerBE.Recipe> extends TurbineBE {

    public static String NAME = "turbine_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage = createEnergy();

    protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);
    public BlockPos errorBlockPos = BlockPos.ZERO;
    @NBTField
    public boolean isCasingValid = false;
    @NBTField
    public boolean isInternalValid = false;
    @NBTField
    public int height = 1;
    @NBTField
    public int width = 1;
    @NBTField
    public int depth = 1;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public double efficiency = 0;
    @NBTField
    public boolean powered = false;
    @NBTField
    protected boolean forceShutdown = false;
    @NBTField
    public int activeCoils = 0;
    @NBTField
    public int flow = 0;

    public ValidationResult validationResult = ValidationResult.INCOMPLETE;
    public RecipeInfo recipeInfo = new RecipeInfo();
    public boolean controllerEnabled = false;
    protected Direction facing;
    public RECIPE recipe;
    public HashMap<String, RECIPE> cachedRecipes = new HashMap<>();
    @Override
    public String getName() {
        return NAME;
    }

    private List<FluidStack> allowedInputs;


    public TurbineControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
        multiblock = new TurbineMultiblock(this);
        contentHandler = new SidedContentHandler(
                0, 0,
                1, 1, 1000, 10000);
        contentHandler.fluidCapability.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler.fluidCapability.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler.setBlockEntity(this);
    }

    @Override
    public ItemCapabilityHandler getItemInventory()
    {
        return contentHandler.itemHandler;
    }

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(100000000, 0, 100000000) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    private void addToCache(RECIPE recipe) {
        String key = contentHandler.getCacheKey();
        if(cachedRecipes.containsKey(key)) {
            cachedRecipes.replace(key, recipe);
        } else {
            cachedRecipes.put(key, recipe);
        }
    }
    public RECIPE getRecipe() {
        if(contentHandler.fluidCapability.tanks.get(0).isEmpty()) return null;
        RECIPE cachedRecipe = getCachedRecipe();
        if(cachedRecipe != null) return cachedRecipe;
        if(!NcRecipeType.ALL_RECIPES.containsKey(getName())) return null;
        for(AbstractRecipe recipe: NcRecipeType.ALL_RECIPES.get(getName()).getRecipeType().getRecipes(getLevel())) {
            if(recipe.test(contentHandler)) {
                addToCache((RECIPE)recipe);
                return (RECIPE)recipe;
            }
        }
        return null;
    }

    public RECIPE getCachedRecipe() {
        String key = contentHandler.getCacheKey();
        if(cachedRecipes.containsKey(key)) {
            if(cachedRecipes.get(key).test(contentHandler)) {
                return cachedRecipes.get(key);
            }
        }
        return null;
    }

    private LazyOptional<NCTurbinePeripheral> peripheralCap;

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new NCTurbinePeripheral(this));
        }
        return peripheralCap.cast();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return contentHandler.getFluidCapability(null);
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        }
        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return getPeripheral(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }

    protected void playRunningSound() {
        if(isRemoved() || (currentSound != null && !currentSound.getLocation().equals(FISSION_REACTOR.get().getLocation()))) {
            SoundHandler.stopTileSound(getBlockPos());
            currentSound = null;
        }
        if((currentSound == null || !Minecraft.getInstance().getSoundManager().isActive(currentSound))) {
            if(currentSound != null && currentSound.getLocation().equals(FISSION_REACTOR.get().getLocation())) {
                return;
            }

            playSoundCooldown = 20;
            currentSound = SoundHandler.startTileSound(FISSION_REACTOR.get(), SoundSource.BLOCKS, 0.2f, level.getRandom(), getBlockPos());
        }
    }

    public void tickClient() {
        if(!isCasingValid || !isInternalValid) {
            stopSound();
            return;
        }
        if(isProcessing()) {
            playRunningSound();
        }
    }
    protected int reValidateCounter = 0;



    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            return;
        }
        changed = false;
        super.tickServer();
        boolean wasPowered = powered;
        handleValidation();
        trackChanges(wasPowered, powered);
        controllerEnabled = (hasRedstoneSignal() || controllerEnabled) && multiblock().isFormed();
        controllerEnabled = !forceShutdown && controllerEnabled;

        if (multiblock().isFormed()) {
            trackChanges(contentHandler.tick());
            if(controllerEnabled) {
                powered = processRecipe();
                trackChanges(powered);
            } else {
                powered = false;
            }
            handleMeltdown();
            contentHandler.setAllowedInputFluids(0, getAllowedInputFluids());
        }
        refreshCacheFlag = !multiblock().isFormed();
        if(refreshCacheFlag || changed) {
            try {
                assert level != null;
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_ALL);
            } catch (NullPointerException ignored) {}
        }

        controllerEnabled = false;
    }

    public List<FluidStack> getAllowedInputFluids()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for(NcRecipe recipe: NcRecipeType.ALL_RECIPES.get(getName()).getRecipeType().getRecipes(getLevel())) {
                for(FluidStackIngredient ingredient: recipe.getInputFluids()) {
                    allowedInputs.addAll(ingredient.getRepresentations());
                }
            }
        }
        return allowedInputs;
    }

    @Override
    public TurbineMultiblock multiblock() {
        if(multiblock == null) {
            multiblock = new TurbineMultiblock(this);
        }
        return multiblock;
    }


    private void handleValidation() {
        if(multiblock == null) return;
        multiblock().tick();
        ValidationResult wasResult = validationResult;
        boolean wasFormed = multiblock().isFormed();
        if (!wasFormed || !isInternalValid || !isCasingValid) {
            activeCoils = 0;
            efficiency = 0;
            flow = 0;
            reValidateCounter++;
            if(reValidateCounter < 40) {
                return;
            }
            reValidateCounter = 0;
            multiblock().validate();
            isCasingValid = multiblock().isOuterValid();
            if(isCasingValid) {
                isInternalValid = multiblock().isInnerValid();
            }
            powered = false;
            changed = true;
        }
        validationResult = multiblock().validationResult;
        if(validationResult.id != wasResult.id) {
            changed = true;
        }
        if(activeCoils != multiblock().activeCoils) {
            changed = true;
            activeCoils = multiblock().activeCoils;
            efficiency = multiblock().coilsEfficiency;
        }

        if(flow != multiblock().flow) {
            changed = true;
            flow = multiblock().flow;
        }
        height = multiblock().height();
        width = multiblock().width();
        depth = multiblock().depth();
        trackChanges(wasFormed, multiblock().isFormed());
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    private void handleMeltdown() {

    }

    public void setRemoved() {
        super.setRemoved();
        if(getLevel().isClientSide()) {
            return;
        }
        if(multiblock() != null) {
            multiblock().onControllerRemoved();
        }
    }

    private boolean processRecipe() {
        if(recipeInfo.recipe != null && recipeInfo.isCompleted()) {
            if(contentHandler.fluidCapability.getFluidInSlot(0).isEmpty()) {
                recipeInfo.clear();
            }
        }
        if (!hasRecipe()) {
            updateRecipe();
        }
        if (hasRecipe()) {
            return process();
        }
        return false;
    }

    private boolean process() {
        recipeInfo.process(1);

        energyStorage.addEnergy(calculateEnergy());

        handleRecipeOutput();

        efficiency = calculateEfficiency();
        return true;
    }

    private void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo.isCompleted()) {
            if(recipe == null) {
                recipe = (RECIPE) recipeInfo.recipe();
            }
            if (recipe.handleOutputs(contentHandler)) {
                recipeInfo.clear();
                if(contentHandler.fluidCapability.getFluidInSlot(0).isEmpty()) {
                    recipe = null;
                }
            } else {
                recipeInfo.stuck = true;
            }
            setChanged();
        }
    }

    public int getRealFlow()
    {
        return Math.min(flow*TURBINE_CONFIG.BLADE_FLOW.get(), getFluidTank(0).getFluidAmount());
    }

    private int calculateEnergy() {
        energyPerTick = (int)(getRealFlow()*TURBINE_CONFIG.ENERGY_GEN.get());
        return energyPerTick;
    }


    private void updateRecipe() {
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo.setRecipe(recipe);
            recipeInfo.ticks = ((RECIPE) recipeInfo.recipe()).getBaseTime();
            recipeInfo.energy = recipeInfo.recipe.getEnergy();
            recipeInfo.be = this;
            recipe.consumeInputs(contentHandler);
        }
    }

    public boolean recipeIsStuck() {
        return recipeInfo.isStuck();
    }

    public boolean hasRecipe() {
        return recipeInfo.recipe() != null;
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(tag.get("Energy"));
        }
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            readTagData(infoTag);
            if (infoTag.contains("recipeInfo")) {
                recipeInfo.deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            if (!isCasingValid || !isInternalValid) {
                errorBlockPos = BlockPos.of(infoTag.getLong("erroredBlock"));
                validationResult = ValidationResult.byId(infoTag.getInt("validationId"));
            } else {
                validationResult = ValidationResult.VALID;
            }
        }
        if (tag.contains("Content")) {
            contentHandler.deserializeNBT(tag.getCompound("Content"));
        }
        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        tag.put("Energy", energyStorage.serializeNBT());
        tag.put("Content", contentHandler.serializeNBT());
        infoTag.put("recipeInfo", recipeInfo.serializeNBT());
        infoTag.putInt("validationId", validationResult.id);
        infoTag.putLong("erroredBlock", errorBlockPos.asLong());
        saveTagData(infoTag);
        tag.put("Info", infoTag);
    }

    public Direction getFacing() {
        if (facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }

    @Override
    public void loadClientData(CompoundTag tag) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            if (infoTag.contains("recipeInfo")) {
                recipeInfo.deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            energyStorage.setEnergy(infoTag.getInt("energy"));
            readTagData(infoTag);
            if (!isCasingValid || !isInternalValid) {
                errorBlockPos = BlockPos.of(infoTag.getLong("erroredBlock"));
                validationResult = ValidationResult.byId(infoTag.getInt("validationId"));
            } else {
                validationResult = ValidationResult.VALID;
            }
            if (tag.contains("Content")) {
                contentHandler.deserializeNBT(tag.getCompound("Content"));
            }
        }
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        tag.put("Info", infoTag);
        infoTag.putInt("energy", energyStorage.getEnergyStored());
        saveTagData(infoTag);
        infoTag.put("recipeInfo", recipeInfo.serializeNBT());
        infoTag.putInt("validationId", validationResult.id);
        infoTag.putLong("erroredBlock", errorBlockPos.asLong());
        tag.put("Content", contentHandler.serializeNBT());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        int oldEnergy = energyStorage.getEnergyStored();

        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);

        if (oldEnergy != energyStorage.getEnergyStored()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public double calculateEfficiency() {
        return (double) calculateEnergy() / (recipeInfo.energy / 100);
    }

    public int getDepth() {
        return depth;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }

    public void forceShutdown() {
        forceShutdown = true;
    }

    public void disableForceShutdown() {
        forceShutdown = false;
    }

    public boolean isProcessing() {
        return hasRecipe() && recipeInfo.ticksProcessed > 0 && !recipeInfo.isCompleted();
    }

    public int getActiveCoils() {
        return activeCoils;
    }

    public int getFlow() {
        return flow;
    }

    public FluidTank getFluidTank(int i) {
        return contentHandler.fluidCapability.tanks.get(i);
    }
    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, rarity);
            CATALYSTS.put(TurbineControllerBE.NAME, List.of(getToastSymbol()));
        }

        @Override
        public String getCodeId() {
            return TurbineControllerBE.NAME;
        }

        @Override
        public @NotNull String getGroup() {
            return TurbineControllerBE.NAME;
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(TurbineRegistration.TURBINE_BLOCKS.get(getCodeId()).get());
        }

        public int getBaseTime() {
            return (int) Math.max(1, timeModifier);
        }

        public double getEnergy() { return Math.max(1, powerModifier); }

        public double ratio = 1D;
        @Override
        public void consumeInputs(SidedContentHandler contentHandler) {
            TurbineControllerBE<?> be = (TurbineControllerBE<?>)contentHandler.blockEntity;
            int flow = be.getRealFlow();
            ratio = (double)flow/(double)getInputFluids(0).get(0).getAmount();
            FluidStack holded = contentHandler.fluidCapability.getFluidInSlot(0).copy();
            holded.setAmount(flow);
            contentHandler.fluidCapability.holdedInputs.add(holded);
            contentHandler.fluidCapability.tanks.get(0).drain(flow, EXECUTE);
        }

        @Override
        public boolean handleOutputs(SidedContentHandler contentHandler) {
            FluidStack outputFluid = outputFluids[0].getRepresentations().get(0);
            FluidStack toOutput = outputFluid.copy();
            int toPush = (int) (outputFluid.getAmount()*ratio);
            toOutput.setAmount(toPush);
            return contentHandler.fluidCapability.insertFluidInternal(1, toOutput, false).getAmount() != toPush;
        }
    }
}
