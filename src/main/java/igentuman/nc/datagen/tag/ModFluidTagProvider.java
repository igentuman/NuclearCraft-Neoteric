package igentuman.nc.datagen.tag;

import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

/** Generates fluid tags for material, isotope, and fission-fuel fluids under mod and common namespaces. */
public class ModFluidTagProvider extends FluidTagsProvider {
    public ModFluidTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() instanceof MaterialEntry mat && mat.hasFluid()) {
                var materialFluid = mat.materialFluid();
                String fluidName = mat.fluidDefinition.resolveName(mat.name);

                // Tag both source and flowing under a mod-namespaced tag
                var fluidTag = net.minecraft.tags.FluidTags.create(
                        rl(fluidName)
                );
                tag(fluidTag)
                        .add(materialFluid.source().get())
                        .add(materialFluid.flowing().get());

                // Common tags (c: namespace)
                // e.g. c:molten_silver for molten metals, c:silver for non-molten fluids
                var commonFluidTag = net.minecraft.tags.FluidTags.create(
                        Identifier.fromNamespaceAndPath("c", fluidName)
                );
                tag(commonFluidTag)
                        .add(materialFluid.source().get())
                        .add(materialFluid.flowing().get());

                // Also add a base material tag (c:silver) for molten fluids
                if (mat.fluidDefinition.isMolten) {
                    var commonMaterialTag = net.minecraft.tags.FluidTags.create(
                            Identifier.fromNamespaceAndPath("c", mat.name)
                    );
                    tag(commonMaterialTag)
                            .add(materialFluid.source().get())
                            .add(materialFluid.flowing().get());
                }
            }
        }

        for (FissionFuelEntry fuel : ModEntries.FISSION_FUEL.values()) {
            for (MaterialEntry mat : fuel.fluids()) {
                var materialFluid = mat.materialFluid();
                String fluidName = mat.fluidDefinition.resolveName(mat.name);
                tag(net.minecraft.tags.FluidTags.create(rl(fluidName)))
                        .add(materialFluid.source().get())
                        .add(materialFluid.flowing().get());
                tag(net.minecraft.tags.FluidTags.create(Identifier.fromNamespaceAndPath("c", fluidName)))
                        .add(materialFluid.source().get())
                        .add(materialFluid.flowing().get());
            }
        }

        for (igentuman.nc.registration.IsotopeEntry isotope : ModEntries.ISOTOPES.values()) {
            for (MaterialEntry mat : isotope.fluids()) {
                var materialFluid = mat.materialFluid();
                String fluidName = mat.fluidDefinition.resolveName(mat.name);
                tag(net.minecraft.tags.FluidTags.create(rl(fluidName)))
                        .add(materialFluid.source().get())
                        .add(materialFluid.flowing().get());
                tag(net.minecraft.tags.FluidTags.create(Identifier.fromNamespaceAndPath("c", fluidName)))
                        .add(materialFluid.source().get())
                        .add(materialFluid.flowing().get());
            }
        }
    }
}
