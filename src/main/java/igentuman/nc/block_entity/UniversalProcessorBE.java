package igentuman.nc.block_entity;

import igentuman.nc.block.UniversalProcessorBlock;
import igentuman.nc.block_entity.catalyst.Catalyst;
import igentuman.nc.block_entity.catalyst.CatalystDef;
import igentuman.nc.block_entity.catalyst.CatalystRegistry;
import igentuman.nc.block_entity.catalyst.CatalystType;
import igentuman.nc.block_entity.catalyst.EnergyCatalyst;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import igentuman.nc.util.caps.ItemCapDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Block entity for single-block processing machines; runs recipes with redstone control and catalyst modifiers. */
public class UniversalProcessorBE extends GlobalBlockEntity implements MenuProvider {

    @NBTField(syncToClient = true)
    public int redstoneMode = 0;
    @NBTField(syncToClient = true)
    public double speedMultiplier = 1;
    @NBTField(syncToClient = true)
    public long enrgyPerTick = 1;
    protected double energyMultiplier = 1;

    private final Map<CatalystType, Catalyst> activeCatalysts = new EnumMap<>(CatalystType.class);
    private boolean validatorsInstalled = false;
    private boolean catalystListenerInstalled = false;
    private boolean catalystsDirty = true;

    public UniversalProcessorBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
        if (contentHandler.hasItemCapability()) {
            contentHandler.getItemHandler().setSideInsertLocked(true);
        }
    }

    @Override
    public void serverTick() {
        if (name != null && !ModEntries.isEnabled(name)) return;
        installSlotValidators();
        installCatalystListener();
        if (redstoneMode == 1 && level != null && !level.hasNeighborSignal(worldPosition)) return;
        refreshCatalysts();
        recipeInfo.resetCatalystModifiers();
        for (Catalyst c : activeCatalysts.values()) c.preTick();
        updateUpgradeMultipliers();
        if (recipeInfo.multiplier > 0) {
            recipeInfo.energyPerTick = (int) (recipeInfo.energyPerTick * energyMultiplier / recipeInfo.multiplier);
        }
        enrgyPerTick = (long) recipeInfo.energyPerTick * recipeInfo.multiplier;
        super.serverTick();
        for (Catalyst c : activeCatalysts.values()) c.postTick();
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
        if (level == null || level.isClientSide) return;
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
        if(!catalystsDirty) return;
        catalystsDirty = false;
        ModEntry entry = ModEntries.get(name);
        if (entry == null || !entry.hasCatalysts()) return;
        if (!contentHandler.hasItemCapability()) return;
        ItemCapDefinition cap = entry.itemCap();
        if (cap == null) return;

        var handler = contentHandler.getItemHandler();
        Set<CatalystType> supported = entry.supportedCatalysts();
        int base = cap.inputSlots + cap.outputSlots + cap.globalSlots;
        int i = 0;
        for (CatalystType type : supported) {
            int slot = base + i;
            i++;
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                activeCatalysts.remove(type);
                continue;
            }
            CatalystDef def = findDef(type, stack);
            if (def == null) {
                activeCatalysts.remove(type);
                continue;
            }
            Catalyst catalyst = activeCatalysts.get(type);
            if (catalyst == null || catalyst.item != stack.getItem()) {
                catalyst = def.factory().create(this);
                catalyst.item = stack.getItem();
                activeCatalysts.put(type, catalyst);
            }
            catalyst.power = stack.getCount();
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
        if (validatorsInstalled || level == null) return;
        ModEntry entry = ModEntries.get(name);
        if (entry == null) return;
        if (entry.hasRecipes() && recipeInfo.getRecipes().isEmpty()) return;

        if (contentHandler.hasItemCapability() && entry.itemCap() != null) {
            ItemCapDefinition cap = entry.itemCap();
            ItemCapabilityHandler handler = contentHandler.getItemHandler();
            List<Item> allowedInputs = collectAllowedInputItems();
            for (int i = 0; i < cap.inputSlots; i++) {
                handler.setSlotValidator(i, stack -> allowedInputs.isEmpty() || allowedInputs.contains(stack.getItem()));
            }
            if (entry.hasCatalysts()) {
                int base = cap.inputSlots + cap.outputSlots + cap.globalSlots;
                int i = 0;
                for (CatalystType type : entry.supportedCatalysts()) {
                    List<Item> catItems = catalystItems(type);
                    handler.setSlotValidator(base + i, stack -> catItems.contains(stack.getItem()));
                    i++;
                }
            }
        }

        if (contentHandler.hasFluidCapability() && entry.fluidCap() != null) {
            FluidCapabilityHandler fh = contentHandler.getFluidHandler();
            int inputTanks = entry.fluidCap().inputTanks.size();
            List<Fluid> allowedFluids = collectAllowedInputFluids();
            for (int t = 0; t < inputTanks; t++) {
                fh.getInternalHandler().setTankValidator(t,
                        fs -> allowedFluids.isEmpty() || allowedFluids.contains(fs.getFluid()));
            }
        }

        if (contentHandler.hasItemCapability()) {
            contentHandler.getItemHandler().setSideInsertLocked(false);
        }
        validatorsInstalled = true;
    }

    private List<Item> collectAllowedInputItems() {
        List<Item> items = new ArrayList<>();
        for (Recipe<?> r : recipeInfo.getRecipes().values()) {
            if (!(r instanceof UniversalProcessorRecipe recipe)) continue;
            for (SizedIngredient si : recipe.getItemInputs()) {
                for (ItemStack st : si.ingredient().getItems()) {
                    if (!items.contains(st.getItem())) items.add(st.getItem());
                }
            }
        }
        return items;
    }

    private List<Fluid> collectAllowedInputFluids() {
        List<Fluid> fluids = new ArrayList<>();
        for (Recipe<?> r : recipeInfo.getRecipes().values()) {
            if (!(r instanceof UniversalProcessorRecipe recipe)) continue;
            for (SizedFluidIngredient sfi : recipe.getFluidInputs()) {
                for (FluidStack fs : sfi.getFluids()) {
                    if (!fluids.contains(fs.getFluid())) fluids.add(fs.getFluid());
                }
            }
        }
        return fluids;
    }

    private List<Item> catalystItems(CatalystType type) {
        List<Item> items = new ArrayList<>();
        for (CatalystDef def : CatalystRegistry.byType(type)) {
            items.add(def.item().get());
        }
        return items;
    }

    private CatalystDef findDef(CatalystType type, ItemStack stack) {
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
