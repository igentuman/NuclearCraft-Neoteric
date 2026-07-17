package igentuman.nc.content.particles;

import igentuman.nc.compat.kubejs.NCKubeJsEvents;
import igentuman.nc.content.fuel.FuelDef;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.content.particles.Particles.*;
import static igentuman.nc.util.ModUtil.isKubeJsLoaded;

public class ParticleSources {

    public static final int moleAmount = 1000000;
    private static boolean initialized = false;
    public static void init(MinecraftServer server)
    {}
    public final static HashMap<String, ParticleStack> sources = new HashMap<>();
    public final static HashMap<String, ParticleStack> fluidSources = new HashMap<>();

    public static void init()
    {
        if(initialized) return;
        System.out.println("Initializing ParticleSources");
        initialized = true;
        sources.put("source_calcium_48", new ParticleStack(calcium_48_ion, 5 * moleAmount, 0,0));
        sources.put("source_iridium_192", new ParticleStack(positron, moleAmount, 0,0));
        sources.put("tungsten_filament", new ParticleStack(electron, 50 * moleAmount, 0,0));
        sources.put("antideuterium", new ParticleStack(antideuteron, 50 * moleAmount, 0,0));
        sources.put("antihelium", new ParticleStack(antialpha, 50 * moleAmount, 0,0));
        sources.put("antihelium3", new ParticleStack(antihelion, 50 * moleAmount, 0,0));
        sources.put("antihydrogen", new ParticleStack(antiproton, 50 * moleAmount, 0,0));
        sources.put("antitritium", new ParticleStack(antitriton, 50 * moleAmount, 0,0));
        sources.put("empty", new ParticleStack());
        sources.put("glueballs", new ParticleStack());
        sources.put("positronium", new ParticleStack());
        sources.put("tauonium", new ParticleStack());

        fluidSources.put("diborane", new ParticleStack(boron_ion, 50 * moleAmount, 0,0));
        fluidSources.put("hydrogen", new ParticleStack(proton, 50 * moleAmount, 0,0));
        fluidSources.put("deuterium", new ParticleStack(deuteron, 50 * moleAmount, 0,0));
        fluidSources.put("tritium", new ParticleStack(triton, 50 * moleAmount, 0,0));
        fluidSources.put("helium", new ParticleStack(alpha, 50 * moleAmount, 0,0));

    }

    public static void registerRuntimeSources() {
        //register custom item sources
        RegisterParticleSourceItemEvent itemSourcesEvent = new RegisterParticleSourceItemEvent();
        MinecraftForge.EVENT_BUS.post(itemSourcesEvent);

        if(isKubeJsLoaded()) {
            System.out.println("Posting RegisterParticleSourceItemEvent to KubeJS");
            NCKubeJsEvents.onParticleItemSourcesRegister(itemSourcesEvent);
        }
        //register custom fluid sources
        RegisterParticleSourceFluidEvent fluidSourcesEvent = new RegisterParticleSourceFluidEvent();
        MinecraftForge.EVENT_BUS.post(fluidSourcesEvent);

        if(isKubeJsLoaded()) {
            System.out.println("Posting RegisterParticleSourceFluidEvent to KubeJS");
            NCKubeJsEvents.onParticleFluidSourcesRegister(fluidSourcesEvent);
        }
    }

    public static ParticleStack getParticleFromItem(ItemStack stack) {
        if (sources.containsKey(stack.getItem().toString())) {
            initNBT(stack);
            return new ParticleStack(sources.get(stack.getItem().toString()).getParticle(), getCapacity(stack), 0, 0);
        }
        if (sources.containsKey(stack.getItemHolder().unwrap().left().get().location().toString())) {
            initNBT(stack);
            return new ParticleStack(sources.get(stack.getItemHolder().unwrap().left().get().location().toString()).getParticle(), getCapacity(stack), 0, 0);
        }
        return null;
    }

    private static void initNBT(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("particle_storage"))
        {
            CompoundTag storage = new CompoundTag();
            storage.putInt("particle_amount", ParticleSources.getCapacity(stack));
            storage.putInt("particle_capacity", ParticleSources.getCapacity(stack));
            tag.put("particle_storage", storage);
        }
    }

    public static class RegisterParticleSourceFluidEvent extends Event {

        public void addParticleSourceFluid(String name, ParticleStack stack) {
            ParticleSources.fluidSources.put(name, stack);
        }

        public RegisterParticleSourceFluidEvent() {
        }
    }

    public static class RegisterParticleSourceItemEvent extends Event {

        public void addParticleSourceItem(String name, ParticleStack stack) {
            ParticleSources.sources.put(name, stack);
        }

        public RegisterParticleSourceItemEvent() {

        }
    }

    public static void use(ItemStack stack, int amount) {
        CompoundTag tag = stack.getOrCreateTag();
        if(tag.contains("particle_storage")) {
            CompoundTag storage = tag.getCompound("particle_storage");
            storage.putInt("particle_amount", storage.getInt("particle_amount") - amount);
        }
    }

    public static int getAmountStored(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if(tag.contains("particle_storage")) {
            CompoundTag storage = tag.getCompound("particle_storage");
            return storage.getInt("particle_amount");
        }
        return 0;
    }

    public static int getCapacity(ItemStack stack) {
        if (sources.containsKey(stack.getItem().toString())) {
            return sources.get(stack.getItem().toString()).getAmount();
        }
        if (sources.containsKey(stack.getItemHolder().unwrap().left().get().location().toString())) {
            return sources.get(stack.getItemHolder().unwrap().left().get().location().toString()).getAmount();
        }
        return 0;
    }

    public static ParticleStack getParticleFromFluid(FluidStack fluidStack) {
        if (fluidStack == null || fluidStack.isEmpty()) {
            return null;
        }
        String fluidName = fluidStack.getFluid().builtInRegistryHolder().key().location().getPath();
        if (fluidSources.containsKey(fluidName)) {
            return fluidSources.get(fluidName).copy();
        }
        return null;
    }
}
