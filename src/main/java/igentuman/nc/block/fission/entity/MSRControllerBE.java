package igentuman.nc.block.fission.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.MultiblockPortBE;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.multiblock.fission.MSRMultiblock;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.block.fission.MSRControllerBlock.POWERED;
import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;

public class MSRControllerBE extends MultiblockControllerBE {

    public static final String NAME = "msr_reactor_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    protected final LazyOptional<IEnergyStorage> energy;

    @NBTField
    public double maxHeat = FISSION_CONFIG.HEAT_CAPACITY.getDefault();
    @NBTField
    public double heat = 0;
    @NBTField
    public boolean powered = false;
    @NBTField
    public double heatPerTick = 0;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public double efficiency = 0;
    @NBTField
    public boolean enabledByController = false;
    @NBTField
    public boolean hasRedstoneSignal = false;
    @NBTField
    public int connectedPorts = 0;
    @NBTField
    public boolean forceShutdown = false;
    @NBTField
    public int fuelCellsCount = 0;
    
    private boolean portsInitialized = false;
    private long lastTickTime = -1L;
    private boolean changed = false;

    public MSRControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(FissionReactorRegistration.FISSION_BE.get(NAME).get(), pPos, pBlockState);
        contentHandler = new SidedContentHandler(
                1, 1,
                2, 2);
        contentHandler().setBlockEntity(this);
        contentHandler().fluidHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().fluidHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler().fluidHandler.tanks.get(0).setCapacity(10000);
        contentHandler().fluidHandler.tanks.get(1).setCapacity(10000);
        
        energyStorage = createEnergy();
        energyStorage
                .setInputEnergyTier(0)
                .setOutputEnergyTier(getBaseGTEnergyTier())
                .setInputAmperage(0)
                .setOutputAmperage(16);
        energy = LazyOptional.of(() -> energyStorage);
    }

    public void initializePorts() {
        if(portsInitialized) return;
        portsInitialized = true;
        for(MultiblockPortBE port: getMultiblock().getPorts()) {
            port.pushPull();
        }
    }

    @Override
    public int getBaseGTEnergyTier() {
        return 0;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public SidedContentHandler contentHandler() {
        return contentHandler;
    }

    @Override
    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(100000000, 0, 100000000) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    @Override
    public void tickClient() {
        super.tickClient();
        if(!isCasingValid || !isInternalValid) {
            return;
        }
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            controllerEnabled = false;
            return;
        }

        // Disallow boosters like torcherino
        if(lastTickTime == level.getGameTime()) {
            return;
        }
        lastTickTime = level.getGameTime();
        changed = false;

        super.tickServer();

        boolean wasFormed = getMultiblock().isFormed();
        boolean wasEnabled = controllerEnabled;
        boolean wasPowered = powered;

        handleValidation();

        // MSR controller logic
        if (getMultiblock().isFormed()) {
            initializePorts();
            trackChanges(contentHandler().tick());

            // Check if reactor should run
            controllerEnabled = hasRedstoneSignal() && !forceShutdown;

            if (controllerEnabled) {
                powered = true;
                // Process fuel and generate energy/heat
                processFuel();
            } else {
                powered = false;
                // Cool down slowly
                coolDown();
            }
        }

        changed = powered != wasPowered || changed;
        refreshCacheFlag = !getMultiblock().isFormed();

        if(refreshCacheFlag || changed || currentTick % 40 == 0) {
            try {
                assert level != null;
                setChanged();
                if(powered != wasPowered) {
                    level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
                }
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_ALL);
            } catch (NullPointerException ignored) {}
        }
    }

    private void processFuel() {
        if(energyPerTick <= 0 || heatPerTick <= 0) {
            return;
        }

        // Add energy
        if(energyPerTick > 0) {
            energyStorage.addEnergy(energyPerTick);
        }

        // Add heat
        if(heatPerTick > 0) {
            heat += heatPerTick;
            
            // Apply passive cooling
            double cooling = getPassiveCooling();
            heat -= cooling;
            
            // Ensure heat stays within bounds
            heat = Math.max(0, Math.min(heat, maxHeat));
            
            // Overheat check
            if(heat >= maxHeat * 0.9) {
                powered = false;
                forceShutdown = true;
            }
        }
    }

    private void coolDown() {
        if(heat > 0) {
            double cooling = getPassiveCooling() * 2.0; // Cool twice as fast when shut down
            heat -= cooling;
            heat = Math.max(0, heat);
        }

        if(heat < maxHeat * 0.7) {
            forceShutdown = false;
        }
    }

    private double getPassiveCooling() {
        // Passive cooling based on available heat capacity
        return maxHeat * 0.01;
    }

    protected void trackChanges(boolean changed) {
        this.changed = this.changed || changed;
    }

    @Override
    public MSRMultiblock getMultiblock() {
        if(getLevel().isClientSide()) {
            debugLog("Trying to access multiblock from client");
            return null;
        }
        if(multiblock == null) {
            multiblock = new MSRMultiblock(this);
        }
        return (MSRMultiblock) multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    public boolean hasRedstoneSignal() {
        if(currentTick % 10 == 0) {
            hasRedstoneSignal = getLevel().hasNeighborSignal(getBlockPos());
        }
        return enabledByController || hasRedstoneSignal;
    }
}