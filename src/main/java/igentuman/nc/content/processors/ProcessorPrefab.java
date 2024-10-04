package igentuman.nc.content.processors;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.content.processors.config.ProcessorSlots;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.recipes.AbstractRecipe;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static igentuman.nc.handler.config.ProcessorsConfig.PROCESSOR_CONFIG;

public class ProcessorPrefab <M extends NCProcessorContainer, U extends Screen & MenuAccess<M>> {

    public int progressBar = 0;
    public Supplier<RecipeSerializer<? extends AbstractRecipe>> recipeSerializerSupplier;
    public boolean supportsCatalyst;
    private  Class  container;
    private  MenuScreens.ScreenConstructor<M, U>  screenConstructor;
    private boolean initialized;
    private boolean has_recipes = true;
    private Boolean registered = true;

    public String name;

    public boolean supportSpeedUpgrade = true;
    protected int power = 50;
    protected int time = 200;

    public List<Integer> hiddenSlots = new ArrayList<>();

    public boolean supportEnergyUpgrade = true;


    protected Class recipeManager;


    public BlockEntityType.BlockEntitySupplier<? extends NCProcessorBE>  getBlockEntity() {
        return blockEntity;
    }

    public ProcessorPrefab<M, U> setBlockEntity(BlockEntityType.BlockEntitySupplier<? extends NCProcessorBE>  blockEntity) {
        this.blockEntity = blockEntity;
        return this;
    }
    private BlockEntityType.BlockEntitySupplier<? extends NCProcessorBE>  blockEntity;

    public MenuScreens.ScreenConstructor<M, U> getScreenConstructor() {
        return screenConstructor;
    }

    public void setScreenConstructor(MenuScreens.ScreenConstructor<M, U> screenConstructor) {
        this.screenConstructor = screenConstructor;
    }

    public boolean isSlotHidden(int slot)
    {
        return hiddenSlots.contains(slot);
    }

    public void setContainer(Class container) {
        this.container = container;
    }

    public ProcessorPrefab(String name)
    {
        this.name = name;
        slotsConfig = new ProcessorSlots();
    }

    public ProcessorPrefab(String name, int inFluids, int inItems, int outFluids, int outItems)
    {
        this(name);
        slotsConfig
                .setInputFluids(inFluids)
                .setInputItems(inItems)
                .setOutputFluids(outFluids)
                .setOutputItems(outItems);
    }


    public int getPower() {
        return power;
    }

    public ProcessorPrefab power(int power) {
        this.power = power;
        return this;
    }

    public int getTime() {
        return time;
    }

    public ProcessorPrefab<M, U> time(int time) {
        this.time = time;
        return this;
    }


    public ProcessorSlots getSlotsConfig() {
        return slotsConfig;
    }

    public int getTotalItemSlots()
    {
        return getSlotsConfig().getInputItems()
                +getSlotsConfig().getOutputItems()
                +(supportSpeedUpgrade ? 1 : 0)
                +(supportEnergyUpgrade ? 1 : 0);
    }

    protected ProcessorSlots slotsConfig;

    public ProcessorPrefab<M, U> config()
    {
        if(!initialized) {
            if(!CommonConfig.isLoaded()) {
                return this;
            }
            int id = Processors.all().keySet().stream().toList().indexOf(name);
            registered = PROCESSOR_CONFIG.REGISTER_PROCESSOR.get().get(id);
            power = PROCESSOR_CONFIG.PROCESSOR_POWER.get().get(id);
            time = PROCESSOR_CONFIG.PROCESSOR_TIME.get().get(id);
            initialized = true;
        }
        return this;
    }

    public boolean isRegistered() {
        return  registered;
    }

    public Constructor<M> getContainerConstructor() {
        try {
            return container.getConstructor(int.class, BlockPos.class, Inventory.class, Player.class, String.class);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public int getUpgradesSlots() {
        return (supportSpeedUpgrade ? 1 : 0) + (supportEnergyUpgrade ? 1 : 0);
    }

    public boolean hasRecipes() {
        return has_recipes;
    }

    public Supplier<RecipeSerializer<? extends AbstractRecipe>> getRecipeSerializer() {
        return recipeSerializerSupplier;
    }

}
