package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.collision_chamber.entity.CollisionChamberControllerBE;
import igentuman.nc.block.decay_chamber.entity.DecayChamberControllerBE;
import igentuman.nc.content.particles.ParticleStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class CollisionChamberPeripheral implements IPeripheral {
    private final CollisionChamberControllerBE controller;

    public CollisionChamberPeripheral(CollisionChamberControllerBE be)
    {
        this.controller = be;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "nc_decay_chamber";
    }


    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof CollisionChamberPeripheral && ((CollisionChamberPeripheral) other).controller == controller;
    }

    @LuaFunction
    public final boolean isFormed()
    {
        return controller.isCasingValid && controller.isInternalValid;
    }

    @LuaFunction
    public final String getName() {
        return controller.getName();
    }

    @LuaFunction
    public final boolean hasRecipe() {
        return controller.hasRecipe();
    }

    @LuaFunction
    public final int getRecipeProgress()
    {
        return (int) (controller.getRecipeProgress()*100);
    }

    @LuaFunction
    public final void enableController()
    {
        controller.disableForceShutdown();
    }

    @LuaFunction
    public final void disableController()
    {
        controller.forceShutdown();
    }

    @LuaFunction
    public final int getEnergyPerTick()
    {
        return controller.energyPerTick;
    }

    @LuaFunction
    public final int getEnergyStored()
    {
        return controller.energyStorage().getEnergyStored();
    }


    @LuaFunction
    public Object getInputParticleInfo()
    {
        if(!isFormed() || !controller.hasParticle || controller.getParticleStorage().getClientParticleStack() == null) {
            return null;
        }
        Map<String, Object> set = new HashMap<String, Object>();
        Map<String, Object> a = new HashMap<String, Object>();
        a.put("energy", controller.getParticleStorage().getClientParticleStack().getMeanEnergy());
        a.put("focus", controller.getParticleStorage().getClientParticleStack().getFocus());
        a.put("amount", controller.getParticleStorage().getClientParticleStack().getAmount());
        a.put("particle", controller.getParticleStorage().getClientParticleStack().getParticle().getName());
        Map<String, Object> b = new HashMap<String, Object>();
        a.put("energy", controller.getParticleStorage().getClientParticleStackB().getMeanEnergy());
        a.put("focus", controller.getParticleStorage().getClientParticleStackB().getFocus());
        a.put("amount", controller.getParticleStorage().getClientParticleStackB().getAmount());
        a.put("particle", controller.getParticleStorage().getClientParticleStackB().getParticle().getName());
        set.put("particle_1", a);
        set.put("particle_2", b);
        return set;
    }

    @LuaFunction
    public Object getOutputParticlesInfo()
    {
        if(!isFormed() || !controller.hasParticle || controller.getParticleStorage().getClientParticleStack() == null) {
            return null;
        }
        Map<String, Object> set = new HashMap<String, Object>();
        for(ParticleStack output: controller.getParticleStorage().outputParticles) {
            if (output.getParticle() == null || output.isEmpty()) {
                continue;
            }
            Map<String, Object> particle = new HashMap<String, Object>();
            particle.put("energy", output.getMeanEnergy());
            particle.put("focus", output.getFocus());
            particle.put("amount", output.getAmount());
            particle.put("particle", output.getParticle().getName());
            set.put(output.getParticle().getName(), particle);
        }

        return set;
    }

    @LuaFunction(mainThread = true)
    public Object getBeamPortsInfo() {
        if (!isFormed()) {
            return null;
        }
        return controller.getBeamPortsInfo();
    }
}
