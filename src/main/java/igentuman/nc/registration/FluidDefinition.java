package igentuman.nc.registration;

public class FluidDefinition {
    public int temperature;
    public int luminosity;
    public int density;
    public int viscosity;
    public boolean isGas;
    public boolean isMolten;
    public boolean isToxic;

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
        FluidDefinition gas = new FluidDefinition();
        gas.isGas = true;
        gas.temperature = 300;
        gas.luminosity = 0;
        gas.density = -1000;
        gas.viscosity = 200;
        return gas;
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
