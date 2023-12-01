package igentuman.nc.content;

import igentuman.nc.block.entity.RFAmplifierBE;
import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.handler.config.CommonConfig;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import igentuman.nc.block.entity.ElectromagnetBE;

import static igentuman.nc.handler.config.CommonConfig.ELECTROMAGNETS_CONFIG;

public class Electromagnets {

    private static HashMap<String, MagnetPrefab> all = new HashMap<>();
    private static HashMap<String, MagnetPrefab> registered = new HashMap<>();

    public static HashMap<String, MagnetPrefab> all() {
        if(all.isEmpty()) {
            all.put("basic_electromagnet", new MagnetPrefab("basic_electromagnet",500, 300, 0.2D, 350000, 50));
            all.put("magnesium_diboride_electromagnet", new MagnetPrefab("magnesium_diboride_electromagnet",1000, 500, 0.5D, 39000, 80));
            all.put("niobium_tin_electromagnet", new MagnetPrefab("niobium_tin_electromagnet",1500, 1140, 1D, 18000, 90));
            all.put("niobium_titanium_electromagnet", new MagnetPrefab("niobium_titanium_electromagnet",2000, 2260, 2D, 10000, 95));
            all.put("bscco_electromagnet", new MagnetPrefab("bscco_electromagnet",3000, 4500, 4D, 104000, 99));
        }
        return all;
    }

    public static HashMap<String, MagnetPrefab> registered() {
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
            tmp.add(all().get(name).getPower());
        }
        return tmp;
    }

    public static Collection<Double> initialMagneticField() {
        List<Double> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(all().get(name).getMagneticField());
        }
        return tmp;
    }

    public static Collection<Integer> initialHeat() {
        List<Integer> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(all().get(name).getHeat());
        }
        return tmp;
    }

    public static class MagnetPrefab {
        private boolean registered = true;
        private boolean initialized = false;
        private String name;
        protected int power = 0;
        protected double magneticField = 0;
        protected int heat = 0;
        protected int maxTemp = 0;
        protected int efficiency = 0;
        private BlockEntityType.BlockEntitySupplier<? extends BlockEntity>  blockEntity = ElectromagnetBE::new;

        public MagnetPrefab(String name, int energy, int heat, double magneticField, int maxTemp, int efficiency) {
            this.power = energy;
            this.name = name;
            this.heat = heat;
            this.magneticField = magneticField;
            this.maxTemp = maxTemp;
            this.efficiency = efficiency;
            blockEntity = ElectromagnetBE::new;

        }

        public int getPower() {
            return power;
        }

        public MagnetPrefab setPower(int power) {
            this.power = power;
            return this;
        }

        public MagnetPrefab config()
        {
            if(!initialized) {
                if(!CommonConfig.isLoaded()) {
                    return this;
                }
                int id = Electromagnets.all().keySet().stream().toList().indexOf(name);
                registered = ELECTROMAGNETS_CONFIG.REGISTERED.get().get(id);
                power = ELECTROMAGNETS_CONFIG.POWER.get().get(id);
                heat = ELECTROMAGNETS_CONFIG.HEAT.get().get(id);
                magneticField = ELECTROMAGNETS_CONFIG.MAGNETIC_FIELD.get().get(id);
                initialized = true;
            }
            return this;
        }
        public boolean isRegistered() {
            return  registered;
        }

        public BlockEntityType.BlockEntitySupplier<? extends BlockEntity>  getBlockEntity() {
            return blockEntity;
        }

        public MagnetPrefab setBlockEntity(BlockEntityType.BlockEntitySupplier<? extends ElectromagnetBE>  blockEntity) {
            this.blockEntity = blockEntity;
            return this;
        }

        public double getMagneticField() {
            return magneticField;
        }

        public int getHeat() {
            return heat;
        }

        public int getEfficiency() {
            return efficiency;
        }

        public int getMaxTemp() {
            return maxTemp;
        }
    }
}
