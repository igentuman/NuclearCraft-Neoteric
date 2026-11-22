package igentuman.nc.block_entity;

import igentuman.nc.api.catalyst.Catalyst;
import igentuman.nc.api.catalyst.CatalystDef;
import igentuman.nc.api.catalyst.CatalystRegistry;
import igentuman.nc.api.catalyst.CatalystType;
import igentuman.nc.block.UniversalProcessorBlock;
import igentuman.nc.block_entity.catalyst.EnergyCatalyst;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.recipe.RecipeSources;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import igentuman.nc.util.caps.ItemCapDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

/** Block entity for single-block processing machines; runs recipes with redstone control and catalyst modifiers. */
public class UniversalProcessorBE extends GlobalBlockEntity implements MenuProvider {

    @NBTField(syncToClient = true)
    public int redstoneMode = 0;
    @NBTField(syncToClient = true)
    public double speedMultiplier = 1;
    @NBTField(syncToClient = true)
    public long enrgyPerTick = 1;
    protected double energyMultiplier = 1;

    private final SequencedMap<CatalystType, Catalyst> activeCatalysts = new LinkedHashMap<>();
    private RecipeMap validatorRecipes;
    private boolean catalystListenerInstalled = false;
    private boolean catalystsDirty = true;

    public UniversalProcessorBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
        if (contentHandler.hasFluidCapability()) {
            contentHandler.getFluidHandler().getInternalHandler().setFixedCapacities();
        }
        if (contentHandler.hasItemCapability()) {
            var handler = contentHandler.getItemHandler();
            handler.setSideInsertLocked(true);
            ModEntry entry = ModEntries.get(name);
            ItemCapDefinition cap = entry.itemCap();
            int slot = cap.inputSlots + cap.outputSlots + cap.globalSlots;
            for (CatalystType type : entry.supportedCatalysts()) {
                handler.setSlotValidator(slot++, stack -> findDef(type, stack) != null);
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        drops();
    }

    @Override
    public void serverTick() {
        if (name != null && !ModEntries.isEnabled(name)) return;
        installSlotValidators();
        installCatalystListener();
        if (redstoneMode == 1 && level != null && !level.hasNeighborSignal(worldPosition)) return;
        refreshCatalysts();
        recipeInfo.resetCatalystModifiers();
        for (Catalyst catalyst : activeCatalysts.values()) catalyst.preTick();
        updateUpgradeMultipliers();
        if (recipeInfo.multiplier > 0) {
            recipeInfo.energyPerTick = (int) (recipeInfo.energyPerTick * energyMultiplier / recipeInfo.multiplier);
        }
        enrgyPerTick = (long) recipeInfo.energyPerTick * recipeInfo.multiplier;
        super.serverTick();
        for (Catalyst catalyst : activeCatalysts.values()) catalyst.postTick();
        updatePoweredState();
    }

    @Override
    public void clientTick() {
        installSlotValidators();
    }

    public void toggleRedstoneMode() {
        redstoneMode = (redstoneMode + 1) % 2;
        setChanged();
    }

    protected void updatePoweredState() {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        if (!state.hasProperty(UniversalProcessorBlock.POWERED)) return;
        boolean powered = recipeInfo.active;
        if (state.getValue(UniversalProcessorBlock.POWERED) != powered) {
            level.setBlock(worldPosition, state.setValue(UniversalProcessorBlock.POWERED, powered), Block.UPDATE_ALL);
        }
    }

    private void installCatalystListener() {
        if (catalystListenerInstalled || level == null) return;
        ModEntry entry = ModEntries.get(name);
        if (entry == null) return;
        if (!entry.hasCatalysts() || !contentHandler.hasItemCapability()) {
            catalystListenerInstalled = true;
            return;
        }
        ItemCapDefinition cap = entry.itemCap();
        if (cap == null) return;
        int base = cap.inputSlots + cap.outputSlots + cap.globalSlots;
        contentHandler.getItemHandler().setSlotChangeListener(slot -> {
            if (slot >= base) catalystsDirty = true;
        });
        catalystListenerInstalled = true;
    }

    /** Rebuilds {@link #activeCatalysts} from the per-type catalyst slots and refreshes each power. */
    private void refreshCatalysts() {
        if (!catalystsDirty) return;
        catalystsDirty = false;
        ModEntry entry = ModEntries.get(name);
        if (entry == null || !entry.hasCatalysts()) return;
        if (!contentHandler.hasItemCapability()) return;
        ItemCapDefinition cap = entry.itemCap();
        if (cap == null) return;

        var handler = contentHandler.getItemHandler();
        int slot = cap.inputSlots + cap.outputSlots + cap.globalSlots;
        for (CatalystType type : entry.supportedCatalysts()) {
            ItemStack stack = handler.getStackInSlot(slot++);
            CatalystDef def = findDef(type, stack);
            if (def == null) {
                activeCatalysts.remove(type);
                continue;
            }
            Catalyst catalyst = activeCatalysts.get(type);
            if (catalyst == null || catalyst.item != stack.getItem()) {
                catalyst = def.factory().create(this);
                catalyst.item = stack.getItem();
            }
            catalyst.power = stack.getCount();
            activeCatalysts.putLast(type, catalyst);
        }
        Catalyst energy = activeCatalysts.get(CatalystType.ENERGY);
        int energyPower = energy == null ? 0 : energy.power;
        if (energyStorage != null && entry.energyCap() != null) {
            long baseCapacity = entry.energyCap().getCapacity();
            long bonus = baseCapacity * ((long) energyPower * EnergyCatalyst.CAPACITY_PERCENT_PER_POWER) / 100;
            energyStorage.setCapacity(baseCapacity + bonus);
        }
    }

    private void updateUpgradeMultipliers() {
        speedMultiplier = recipeInfo.multiplier;
        Catalyst energy = activeCatalysts.get(CatalystType.ENERGY);
        int energyPower = energy == null ? 0 : energy.power;
        double effectiveSpeed = Math.min(speedMultiplier + (recipeInfo.parallelLimit - 1) / 2.0, 100);
        double energyUpgrades = energyPower + 1;
        energyMultiplier = Math.max(effectiveSpeed,
                Math.pow(effectiveSpeed - 1, 2) + effectiveSpeed - Math.pow(energyUpgrades, 2));
    }

    private void installSlotValidators() {
        if (level == null) return;
        RecipeMap current = RecipeSources.recipes(level);
        if (validatorRecipes == current) return;
        ModEntry entry = ModEntries.get(name);
        if (entry == null) return;
        List<SizedIngredient> itemInputs = new ArrayList<>();
        List<SizedFluidIngredient> fluidInputs = new ArrayList<>();
        for (var value : recipeInfo.getRecipes().values()) {
            if (value instanceof UniversalProcessorRecipe recipe) {
                itemInputs.addAll(recipe.getItemInputs());
                fluidInputs.addAll(recipe.getFluidInputs());
            }
        }

        if (contentHandler.hasItemCapability() && entry.itemCap() != null) {
            ItemCapDefinition cap = entry.itemCap();
            ItemCapabilityHandler handler = contentHandler.getItemHandler();
            for (int i = 0; i < cap.inputSlots; i++) {
                handler.setSlotValidator(i, stack -> !entry.hasRecipes()
                        || itemInputs.stream().anyMatch(input -> input.ingredient().test(stack)));
            }
        }

        if (contentHandler.hasFluidCapability() && entry.fluidCap() != null) {
            FluidCapabilityHandler fh = contentHandler.getFluidHandler();
            int inputTanks = entry.fluidCap().inputTanks.size();
            for (int t = 0; t < inputTanks; t++) {
                fh.getInternalHandler().setTankValidator(t,
                        stack -> !entry.hasRecipes()
                                || fluidInputs.stream().anyMatch(input -> input.ingredient().test(stack)));
            }
        }

        if (contentHandler.hasItemCapability()) {
            contentHandler.getItemHandler().setSideInsertLocked(false);
        }
        validatorRecipes = current;
    }

    @Nullable
    private static CatalystDef findDef(CatalystType type, ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (CatalystDef def : CatalystRegistry.byType(type)) {
            if (def.item().get() == stack.getItem()) return def;
        }
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.nuclearcraft." + name);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new UniversalProcessorContainer(containerId, playerInventory, this, containerData);
    }

    public long getEnergyPerTick() {
        return enrgyPerTick;
    }
}
