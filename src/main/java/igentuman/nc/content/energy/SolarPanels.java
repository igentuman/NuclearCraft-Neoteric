package igentuman.nc.content.energy;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.block.entity.energy.solar.AdvancedSolarBE;
import igentuman.nc.block.entity.energy.solar.BasicSolarBE;
import igentuman.nc.block.entity.energy.solar.DuSolarBE;
import igentuman.nc.block.entity.energy.solar.EliteSolarBE;
import net.minecraft.tileentity.TileEntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static igentuman.nc.handler.config.CommonConfig.ENERGY_GENERATION;

public class SolarPanels {

    private static HashMap<String, SolarPanelPrefab> all = new HashMap<>();
    private static HashMap<String, SolarPanelPrefab> registered = new HashMap<>();

    public static HashMap<String, SolarPanelPrefab> all() {
        if(all.isEmpty()) {
            all.put("basic", new SolarPanelPrefab("basic",25)/*.setBlockEntity(BasicSolarBE::new)*/);
            all.put("advanced", new SolarPanelPrefab("advanced",100)/*.setBlockEntity(AdvancedSolarBE::new)*/);
            all.put("du", new SolarPanelPrefab("du",400)/*.setBlockEntity(DuSolarBE::new)*/);
            all.put("elite", new SolarPanelPrefab("elite",1500)/*.setBlockEntity(EliteSolarBE::new)*/);
        }
        return all;
    }

    public static HashMap<String, SolarPanelPrefab> registered() {
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
            tmp.add(all().get(name).getGeneration());
        }
        return tmp;
    }

    public static String getCode(String name) {
        for(String code: all().keySet()) {
            if(name.equals("solar_panel_"+code)) {
                return "solar_panel/"+code;
            }
        }
        return "";
    }

    public static class SolarPanelPrefab {
        private boolean registered = true;
        private boolean initialized = false;
        private final String name;
        protected int generation = 0;

        public SolarPanelPrefab(String name, int generation) {
            this.generation = generation;
            this.name = name;
        }

        public int getGeneration() {
            return generation;
        }

        public SolarPanelPrefab setGeneration(int generation) {
            this.generation = generation;
            return this;
        }

        public SolarPanelPrefab config()
        {
            if(!initialized) {
                try {
                    int id = Arrays.asList(SolarPanels.all().keySet().stream().toArray()).indexOf(name);
                    registered = ENERGY_GENERATION.REGISTER_SOLAR_PANELS.get().get(id);
                    generation = ENERGY_GENERATION.SOLAR_PANELS_GENERATION.get().get(id);
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

        public Supplier<TileEntityType<? extends NCEnergy>> getBlockEntity() {
            return blockEntity;
        }

        public SolarPanelPrefab setBlockEntity(Supplier<TileEntityType<? extends NCEnergy>> blockEntity) {
            this.blockEntity = blockEntity;
            return this;
        }
        private Supplier<TileEntityType<? extends NCEnergy>>  blockEntity;
    }
}
