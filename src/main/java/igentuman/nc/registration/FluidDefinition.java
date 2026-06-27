package igentuman.nc.registration;

public class FluidDefinition {
    public int temperature;
    public int luminosity;
    public int density;
    public int viscosity;
    public boolean isGas;
    public boolean isMolten;
    public boolean isToxic;

    /**
     * Explicit registry name override. When set, the fluid (and its tags/lang/block/bucket)
     * use this name verbatim instead of the derived {@code molten_<material>} / {@code <material>_fluid}.
     * Used for standalone fluids (acids, gases) that must keep their bare original names.
     */
    public String fluidName;

    private MaterialFluid registeredFluid;

    public FluidDefinition() {
        this.density = 1000;
        this.viscosity = 1000;
    }

    public static FluidDefinition metal() {
        FluidDefinition metal = new FluidDefinition();
        metal.isMolten = true;
        metal.temperature = 600;
        metal.luminosity = 15;
        metal.density = 3000;
        metal.viscosity = 6000;
        return metal;
    }

    public static FluidDefinition liquid() {
        FluidDefinition liquid = new FluidDefinition();
        liquid.temperature = 300;
        liquid.luminosity = 0;
        liquid.density = 1000;
        liquid.viscosity = 1000;
        return liquid;
    }

    public static FluidDefinition gas() {
        return gas(300);
    }

    public static FluidDefinition gas(int temperature) {
        FluidDefinition gas = new FluidDefinition();
        gas.isGas = true;
        gas.temperature = temperature;
        gas.luminosity = 0;
        gas.density = -1000;
        gas.viscosity = 0;
        return gas;
    }

    public static FluidDefinition acid() {
        return acid(300);
    }

    public static FluidDefinition acid(int temperature) {
        FluidDefinition acid = new FluidDefinition();
        acid.isToxic = true;
        acid.temperature = temperature;
        acid.luminosity = 0;
        acid.density = 400;
        acid.viscosity = 1000;
        return acid;
    }

    /**
     * Resolves the registry name for this fluid: the explicit {@link #fluidName} when set,
     * otherwise the derived molten/fluid name based on {@code materialName}.
     */
    public String resolveName(String materialName) {
        if (fluidName != null) return fluidName;
        return isMolten ? "molten_" + materialName : materialName + "_fluid";
    }

    public FluidDefinition setName(String fluidName) {
        this.fluidName = fluidName;
        return this;
    }

    public FluidDefinition setTemperature(int temperature) {
        this.temperature = temperature;
        return this;
    }

    public FluidDefinition setLuminosity(int luminosity) {
        this.luminosity = luminosity;
        return this;
    }

    public FluidDefinition setDensity(int density) {
        this.density = density;
        return this;
    }

    public FluidDefinition setViscosity(int viscosity) {
        this.viscosity = viscosity;
        return this;
    }

    public FluidDefinition setIsGas(boolean isGas) {
        this.isGas = isGas;
        return this;
    }

    public FluidDefinition setIsMolten(boolean isMolten) {
        this.isMolten = isMolten;
        return this;
    }

    public FluidDefinition setIsToxic(boolean isToxic) {
        this.isToxic = isToxic;
        return this;
    }

    public MaterialFluid getRegisteredFluid() {
        return registeredFluid;
    }

    public void setRegisteredFluid(MaterialFluid registeredFluid) {
        this.registeredFluid = registeredFluid;
    }
}
