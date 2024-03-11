package igentuman.nc.content.processors;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.client.gui.processor.NCProcessorScreen;
import igentuman.nc.content.processors.config.ProcessorSlots;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.serializers.NcRecipeSerializer;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.function.Supplier;


public class ProcessorBuilder <M extends NCProcessorContainer, U extends Screen>{
    public ProcessorPrefab processor;
    private ProcessorBuilder() { }

    public static ProcessorBuilder make(String name)
    {
        ProcessorBuilder builder = new ProcessorBuilder();
        builder.processor = new ProcessorPrefab(name);
        return builder;
    }

    public static ProcessorBuilder<?, ?> make(String name, int inFluids, int inItems, int outFluids, int outItems)
    {
        ProcessorBuilder<?, ?> builder = new ProcessorBuilder<>();
        builder.processor = new ProcessorPrefab(name, inFluids, inItems, outFluids, outItems);
        builder.container(NCProcessorContainer.class);
        if(FMLEnvironment.dist.isClient()){
          //  builder.setScreen(NCProcessorScreen::new);
        }
        return builder;
    }



    public ProcessorBuilder<M, U> container(Class container) {
        processor.setContainer(container);
        return this;
    }

    public ProcessorBuilder blockEntity(Supplier<? extends NCProcessorBE<?>> be)
    {
        processor.setBlockEntity(be);
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public ProcessorBuilder setScreen(NCProcessorScreen screenConstructor)
    {
        processor.setScreenConstructor(screenConstructor);
        return this;
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    public ProcessorBuilder screen(Object screenConstructor)
    {
        return this;
    }

    public ProcessorPrefab build()
    {
        return processor;
    }


    public ProcessorBuilder<?, ?> slotsConfig(ProcessorSlots config)
    {
        processor.slotsConfig = config;
        return this;
    }

    public ProcessorBuilder<?, ?> progressBar(int i) {
        processor.progressBar = i;
        return this;
    }

    public ProcessorBuilder<?, ?> recipeSerializer(Supplier<NcRecipeSerializer<? extends NcRecipe>> sup) {
        processor.recipeSerializerSupplier = sup;
        return this;
    }

    public ProcessorBuilder<?, ?> recipe(NcRecipeSerializer.IFactory<? extends NcRecipe> factory) {
        processor.recipeSerializerSupplier = () -> new NcRecipeSerializer<>(factory);

        return this;
    }

    public ProcessorBuilder<?, ?> upgrades(boolean energy, boolean speed) {
        processor.supportEnergyUpgrade = energy;
        processor.supportSpeedUpgrade = speed;
        return this;
    }

    public ProcessorBuilder<?, ?> withCatalyst() {
        processor.supportsCatalyst = true;
        return this;
    }

    public ProcessorBuilder<?, ?> setHiddenSlots(Integer... i) {
        for(int id: i) {
            processor.hiddenSlots.add(id);
        }
        return this;
    }

    public ProcessorBuilder<?, ?> power(int i) {
        processor.power = i;
        return this;
    }
}
