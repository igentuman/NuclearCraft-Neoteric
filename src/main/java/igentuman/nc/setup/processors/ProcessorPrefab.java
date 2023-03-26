package igentuman.nc.setup.processors;

import igentuman.nc.block.entity.NCProcessor;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.gui.NCProcessorScreen;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.setup.processors.config.ProcessorSlots;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.lang.reflect.Constructor;


public class ProcessorPrefab <M extends NCProcessorContainer, U extends Screen & MenuAccess<M>> {

    public BlockEntityType.BlockEntitySupplier<? extends NCProcessor>  getBlockEntity() {
        return blockEntity;
    }

    public ProcessorPrefab setBlockEntity(BlockEntityType.BlockEntitySupplier<? extends NCProcessor>  blockEntity) {
        this.blockEntity = blockEntity;
        return this;
    }

    private BlockEntityType.BlockEntitySupplier<? extends NCProcessor>  blockEntity;

    public MenuScreens.ScreenConstructor<M, U> getScreenConstructor() {
        return screenConstructor;
    }

    public void setScreenConstructor(MenuScreens.ScreenConstructor<M, U> screenConstructor) {
        this.screenConstructor = screenConstructor;
    }

    public void setContainer(Class container) {
        this.container = container;
    }

    private  Class  container;
    private  MenuScreens.ScreenConstructor<M, U>  screenConstructor;

    private boolean initialized;
    private Boolean registered = true;

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

    protected String name;

    protected boolean supportSpeedUpgrade = true;

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

    protected int power = 200;
    protected int time = 200;

    protected boolean supportEnergyUpgrade = true;
    
    protected boolean supportCatalyst = false;
    
    protected Class recipeManager;
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
}
