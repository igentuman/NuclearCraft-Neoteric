package igentuman.nc.setup.processors;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.handler.ItemToItemRecipeHandler;
import igentuman.nc.recipes.lookup.IRecipeLookupHandler;
import igentuman.nc.setup.processors.config.ProcessorSlots;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class ProcessorPrefab <M extends NCProcessorContainer, U extends Screen & MenuAccess<M>> {

    public int progressBar = 0;
    private  Class  container;
    private  MenuScreens.ScreenConstructor<M, U>  screenConstructor;
    private boolean initialized;
    private Boolean registered = true;

    public String name;

    public boolean supportSpeedUpgrade = true;
    protected int power = 20;
    protected int time = 200;

    public boolean supportEnergyUpgrade = true;

    protected boolean supportCatalyst = false;

    protected Class recipeManager;
    private Class recipeLookupHandler = ItemToItemRecipeHandler.class;


    public BlockEntityType.BlockEntitySupplier<? extends NCProcessorBE>  getBlockEntity() {
        return blockEntity;
    }

    public ProcessorPrefab setBlockEntity(BlockEntityType.BlockEntitySupplier<? extends NCProcessorBE>  blockEntity) {
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

    public ProcessorPrefab time(int time) {
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

    public ProcessorPrefab config()
    {
        if(!initialized) {
            if(!CommonConfig.isLoaded()) {
                return this;
            }
            int id = Processors.all().keySet().stream().toList().indexOf(name);
            registered = CommonConfig.ProcessorConfig.REGISTER_PROCESSOR.get().get(id);
            power = CommonConfig.ProcessorConfig.PROCESSOR_POWER.get().get(id);
            time = CommonConfig.ProcessorConfig.PROCESSOR_TIME.get().get(id);
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

    public <RECIPE extends NcRecipe> IRecipeLookupHandler getRecipeLookupHandler(NCProcessorBE<RECIPE> recipencProcessorBE) {
        try {
            return (IRecipeLookupHandler) recipeLookupHandler.getConstructor(NuclearCraftBE.class).newInstance(recipencProcessorBE);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
