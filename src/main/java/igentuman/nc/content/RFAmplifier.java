package igentuman.nc.content;

import igentuman.nc.block.entity.ElectromagnetBE;
import igentuman.nc.block.entity.RFAmplifierBE;
import igentuman.nc.handler.config.CommonConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import java.util.*;

import static igentuman.nc.handler.config.FusionConfig.RF_AMPLIFIERS_CONFIG;

public class RFAmplifier {

    private static HashMap<String, RFAmplifierPrefab> all = new HashMap<>();
    private static HashMap<String, RFAmplifierPrefab> registered = new HashMap<>();

    public static HashMap<String, RFAmplifierPrefab> all() {
        if(all.isEmpty()) {
            all.put("basic_rf_amplifier", new RFAmplifierPrefab("basic_rf_amplifier",250, 300, 500000, 350000, 75));
            all.put("magnesium_diboride_rf_amplifier", new RFAmplifierPrefab("magnesium_diboride_rf_amplifier",500, 500, 1000000, 39000, 80));
            all.put("niobium_tin_rf_amplifier", new RFAmplifierPrefab("niobium_tin_rf_amplifier",750, 1140, 2000000, 18000, 90));
            all.put("niobium_titanium_rf_amplifier", new RFAmplifierPrefab("niobium_titanium_rf_amplifier",1500, 2260, 3000000, 10000, 95));
            all.put("bscco_rf_amplifier", new RFAmplifierPrefab("bscco_rf_amplifier",2500, 4500, 4000000, 104000, 99));
        }
        return all;
    }

    public static HashMap<String, RFAmplifierPrefab> registered() {
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

    public static Collection<Integer> initialMagneticField() {
        List<Integer> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(all().get(name).getVoltage());
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

    public static Collection<Integer> initialVoltage() {
        List<Integer> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(all().get(name).getVoltage());
        }
        return tmp;
    }

    public static class RFAmplifierPrefab {
        private boolean registered = true;
        private boolean initialized = false;
        private String name;
        protected int power = 0;
        protected int  voltage = 0;
        protected int heat = 0;
        protected int maxTemp = 0;
        protected int efficiency = 0;
        private TileEntityType<? extends TileEntity>  blockEntity;

        public RFAmplifierPrefab(String name, int energy, int heat, int voltage, int maxTemp, int efficiency) {
            this.power = energy;
            this.name = name;
            this.heat = heat;
            this.voltage = voltage;
            this.maxTemp = maxTemp;
            this.efficiency = efficiency;
          //  blockEntity = RFAmplifierBE::new;
        }

        public int getEfficiency() {
            return efficiency;
        }

        public int getMaxTemp() {
            return maxTemp;
        }

        public int getPower() {
            return power;
        }

        public RFAmplifierPrefab setPower(int power) {
            this.power = power;
            return this;
        }

        public RFAmplifierPrefab config()
        {
            if(!initialized) {
                if(!CommonConfig.isLoaded()) {
                    return this;
                }
                int id = Arrays.asList(RFAmplifier.all().keySet().stream().toArray()).indexOf(name);
                registered = RF_AMPLIFIERS_CONFIG.REGISTERED.get().get(id);
                power = RF_AMPLIFIERS_CONFIG.POWER.get().get(id);
                heat = RF_AMPLIFIERS_CONFIG.HEAT.get().get(id);
                voltage = RF_AMPLIFIERS_CONFIG.VOLTAGE.get().get(id);
                initialized = true;
            }
            return this;
        }
        public boolean isRegistered() {
            return  registered;
        }

        public TileEntityType<? extends TileEntity> getBlockEntity() {
            return blockEntity;
        }

        public RFAmplifierPrefab setBlockEntity(TileEntityType<? extends RFAmplifierBE>  blockEntity) {
            this.blockEntity = blockEntity;
            return this;
        }

        public int getVoltage() {
            return voltage;
        }

        public int getHeat() {
            return heat;
        }
    }
}
