package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.content.particles.ParticleStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class TargetChamberPeripheral implements IPeripheral {
    private final TargetChamberControllerBE controller;

    public TargetChamberPeripheral(TargetChamberControllerBE be)
    {
        this.controller = be;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "nc_target_chamber";
    }


    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof TargetChamberPeripheral && ((TargetChamberPeripheral) other).controller == controller;
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
    public final Object[] getInputItem()
    {
        return controller.getInputItem();
    }

    @LuaFunction
    public final Object[] getInputFluid()
    {
        return controller.getInputFluid();
    }

    @LuaFunction
    public Object getInputParticleInfo()
    {
        if(!isFormed() || !controller.hasParticle || controller.getParticleStorage().getClientParticleStack() == null) {
            return null;
        }
        Map<String, Object> particle = new HashMap<String, Object>();
        particle.put("energy", controller.getParticleStorage().getClientParticleStack().getMeanEnergy());
        particle.put("focus", controller.getParticleStorage().getClientParticleStack().getFocus());
        particle.put("amount", controller.getParticleStorage().getClientParticleStack().getAmount());
        particle.put("particle", controller.getParticleStorage().getClientParticleStack().getParticle().getName());
        return particle;
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

    @LuaFunction(mainThread = true)
    public boolean setBeamPortMode(int id, String mode) {
        if (!isFormed()) {
            return false;
        }
        return controller.setBeamPortMode(id, mode);
    }
}
