package igentuman.nc.datagen;

import igentuman.nc.registration.ArmorSetEntry;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ToolSetEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;

import static igentuman.nc.Main.MODID;

public class ModItemModelProvider  extends ItemModelProvider {
    ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MODID, existingFileHelper);
    }

    @Override
    public void registerModels() {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            // Block item models come from ModBlockStateProvider, which knows the special-ruled
            // model path (e.g. fission/controller); only standalone items are built here.
            if (entry.hasItem() && !entry.hasBlock()) {
                simpleItem(entry.item(), entry.name());
            }
            if (entry.toolSetEntry() instanceof ToolSetEntry toolSet) {
                buildItem(toolSet.sword(),   "tools/" + toolSet.name + "_sword",   "item/handheld");
                buildItem(toolSet.pickaxe(), "tools/" + toolSet.name + "_pickaxe", "item/handheld");
                buildItem(toolSet.axe(),     "tools/" + toolSet.name + "_axe",     "item/handheld");
                buildItem(toolSet.shovel(),  "tools/" + toolSet.name + "_shovel",  "item/handheld");
                buildItem(toolSet.hoe(),     "tools/" + toolSet.name + "_hoe",     "item/handheld");
            }
            if (entry.armorSetEntry() instanceof ArmorSetEntry armorSet) {
                buildItem(armorSet.helmet(),     "armor/" + armorSet.name + "_helmet",     "item/generated");
                buildItem(armorSet.chestplate(), "armor/" + armorSet.name + "_chestplate", "item/generated");
                buildItem(armorSet.leggings(),   "armor/" + armorSet.name + "_leggings",   "item/generated");
                buildItem(armorSet.boots(),      "armor/" + armorSet.name + "_boots",      "item/generated");
            }
            if (entry.materialEntry() instanceof MaterialEntry materialEntry) {
                if (materialEntry.hasBlock()) {
                    withExistingParent(materialEntry.name + "_block", modLoc("block/" + materialEntry.name + "_block"));
                }
                if (materialEntry.hasOre()) {
                    withExistingParent(materialEntry.name + "_ore", modLoc("block/" + materialEntry.name + "_ore"));
                }
                if (materialEntry.hasIngot()) {
                    simpleItem(materialEntry.ingot(), "material/ingot/" + materialEntry.name);
                }
                if (materialEntry.hasGem()) {
                    simpleItem(materialEntry.gem(), "material/gem/" + materialEntry.name);
                }
                if (materialEntry.hasRawOre()) {
                    simpleItem(materialEntry.rawOre(), "material/raw/" + materialEntry.name);
                }
                if (materialEntry.hasDust()) {
                    simpleItem(materialEntry.dust(), "material/dust/" + materialEntry.name);
                }
                if (materialEntry.hasPlate()) {
                    simpleItem(materialEntry.plate(), "material/plate/" + materialEntry.name);
                }
                if (materialEntry.hasNugget()) {
                    simpleItem(materialEntry.nugget(), "material/nugget/" + materialEntry.name);
                }
                if (materialEntry.hasFluid()) {
                    var fluid = materialEntry.materialFluid();
                    ResourceLocation bucketKey = BuiltInRegistries.ITEM.getKey(fluid.bucket().asItem());
                    withExistingParent(bucketKey.toString(), "neoforge:item/bucket")
                            .customLoader(DynamicFluidContainerModelBuilder::begin)
                            .fluid(fluid.source().get())
                            .flipGas(materialEntry.fluidDefinition.isGas)
                            .applyTint(true);
                }
            }
        }
        for (IsotopeEntry isotope : ModEntries.ISOTOPES.values()) {
            // Texture path keeps the slashed element/mass layout (e.g. isotope/uranium/238);
            // the model file itself is named after the registered item id (uranium_238).
            isotope.variants().forEach((suffix, item) ->
                    simpleItem(item, "material/isotope/" + isotope.name + suffix));
            for (MaterialEntry mat : isotope.fluids()) {
                var fluid = mat.materialFluid();
                ResourceLocation bucketKey = BuiltInRegistries.ITEM.getKey(fluid.bucket().asItem());
                withExistingParent(bucketKey.toString(), "neoforge:item/bucket")
                        .customLoader(DynamicFluidContainerModelBuilder::begin)
                        .fluid(fluid.source().get())
                        .flipGas(mat.fluidDefinition.isGas)
                        .applyTint(true);
            }
        }
        for (FissionFuelEntry fuel : ModEntries.FISSION_FUEL.values()) {
            String nm = fuel.name.replace("-", "_");
            fuel.fuelItems().forEach((variant, item) ->
                    simpleItem(item, "fuel/" + fuel.group + "/" + nm + variant));
            fuel.depletedItems().forEach((variant, item) ->
                    simpleItem(item, "fuel/" + fuel.group + "/depleted/" + nm + variant));
            for (MaterialEntry mat : fuel.fluids()) {
                var fluid = mat.materialFluid();
                ResourceLocation bucketKey = BuiltInRegistries.ITEM.getKey(fluid.bucket().asItem());
                withExistingParent(bucketKey.toString(), "neoforge:item/bucket")
                        .customLoader(DynamicFluidContainerModelBuilder::begin)
                        .fluid(fluid.source().get())
                        .flipGas(mat.fluidDefinition.isGas)
                        .applyTint(true);
            }
        }
    }

    private void simpleItem(DeferredItem<Item> deferredItem, String name) {
        buildItem(deferredItem, name, "item/generated");
    }

    private void buildItem(DeferredItem<Item> deferredItem, String name, String parent) {
        ResourceLocation item = BuiltInRegistries.ITEM.getKey(deferredItem.asItem());
        getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile(parent))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + name));
    }
}
