package igentuman.nc.content.energy;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.energy.BatteryBE;
import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.handler.config.CommonConfig;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.ENERGY_STORAGE;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;

public class BatteryBlocks {

    private static final HashMap<String, BatteryBlockPrefab> all = new HashMap<>();
    private static final HashMap<String, BatteryBlockPrefab> registered = new HashMap<>();

    public static HashMap<String, BatteryBlockPrefab> all() {
        if(all.isEmpty()) {
            all.put("basic_voltaic_pile", new BatteryBlockPrefab("basic_voltaic_pile",1_600_000, 1));
            all.put("advanced_voltaic_pile", new BatteryBlockPrefab("advanced_voltaic_pile",6_400_000, 2));
            all.put("du_voltaic_pile", new BatteryBlockPrefab("du_voltaic_pile",25_600_000, 3));
            all.put("elite_voltaic_pile", new BatteryBlockPrefab("elite_voltaic_pile",102_400_000, 4));

            all.put("basic_lithium_ion_battery", new BatteryBlockPrefab("basic_lithium_ion_battery",32_000_000, 3));
            all.put("advanced_lithium_ion_battery", new BatteryBlockPrefab("advanced_lithium_ion_battery",128_000_000, 4));
            all.put("du_lithium_ion_battery", new BatteryBlockPrefab("du_lithium_ion_battery",512_000_000, 5));
            all.put("elite_lithium_ion_battery", new BatteryBlockPrefab("elite_lithium_ion_battery", 2_048_000_000, 6));
        }
        return all;
    }

    public static HashMap<String, BatteryBlockPrefab> registered() {
        if(registered.isEmpty()) {
            for(String name: all().keySet()) {
                if (all().get(name).config().isRegistered())
                    registered.put(name,all().get(name));
            }
        }
        return registered;
    }

    public static List<Boolean> initialRegistered() {
        List<Boolean> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(true);
        }
        return tmp;
    }

    public static List<Integer> initialPower() {
        List<Integer> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(all().get(name).getStorage());
        }
        return tmp;
    }

    public static class BatteryBlockPrefab {
        private boolean registered = true;
        private boolean initialized = false;
        private final String name;
        protected int storage = 0;
        protected int tier = 0;


        public BatteryBlockPrefab(String name, int storage, int tier) {
            this.storage = storage;
            blockEntity = BatteryBE::new;
            this.name = name;
            this.tier = tier;
        }

        public int getStorage() {
            return storage;
        }

        public BatteryBlockPrefab setStorage(int capacity) {
            this.storage = storage;
            return this;
        }

        public BatteryBlockPrefab config()
        {
            if(!initialized) {
                try {
                    int id = BatteryBlocks.all().keySet().stream().toList().indexOf(name);
                    registered = ENERGY_STORAGE.REGISTER_ENERGY_BLOCK.get().get(id);
                    storage = ENERGY_STORAGE.ENERGY_BLOCK_STORAGE.get().get(id);
                    initialized = true;
                } catch (Exception e) {
                    NuclearCraft.LOGGER.error("Error while loading config for " + name + "!");
                }
            }
            return this;
        }
        public boolean isRegistered() {
            return  registered;
        }

        public BlockEntityType.BlockEntitySupplier<? extends NCEnergy>  getBlockEntity() {
            return blockEntity;
        }

        public BatteryBlockPrefab setBlockEntity(BlockEntityType.BlockEntitySupplier<? extends NCEnergy>  blockEntity) {
            this.blockEntity = blockEntity;
            return this;
        }

        private BlockEntityType.BlockEntitySupplier<? extends NCEnergy>  blockEntity;
        public CommonConfig.GTCEUCompatibilityConfig.GTCEUTier getEnergyTier() {
            return CommonConfig.GTCEUCompatibilityConfig.GTCEUTier.values()[tier];
        }
    }
}
