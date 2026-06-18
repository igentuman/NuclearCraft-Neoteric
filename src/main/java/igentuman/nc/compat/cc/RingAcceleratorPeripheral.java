package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.accelerator.entity.RingAcceleratorControllerBE;

import java.util.HashMap;
import java.util.Map;

public class RingAcceleratorPeripheral extends LinearAcceleratorPeripheral {
    private final RingAcceleratorControllerBE controller;

    public RingAcceleratorPeripheral(RingAcceleratorControllerBE controller)
    {
        super(controller);
        this.controller = controller;
    }

    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof RingAcceleratorPeripheral && ((RingAcceleratorPeripheral) other).controller == controller;
    }

    @LuaFunction
    public Object getStats()
    {
        Map<String, Object> statsData = new HashMap<String, Object>();
        statsData.put("accelerating_voltage", isMultiblockAssembled() ? controller.acceleratingVoltage : 0);
        statsData.put("dipole_strength", isMultiblockAssembled() ? controller.dipoleStrength : 0);
        statsData.put("quadrupole_strength", isMultiblockAssembled() ? controller.quadStrength : 0);
        statsData.put("input_particle_min_energy", isMultiblockAssembled() ? controller.getMinEnergy() : 0);

        return statsData;
    }

    @Override
    public String getAcceleratorType() {
        return "ring_accelerator";
    }
}