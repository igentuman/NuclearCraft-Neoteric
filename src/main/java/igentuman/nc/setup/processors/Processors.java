package igentuman.nc.setup.processors;


import igentuman.nc.block.entity.ManufactoryBE;
import igentuman.nc.gui.ManufactoryScreen;
import igentuman.nc.gui.NCProcessorScreen;
import igentuman.nc.handler.config.CommonConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Processors {

    private static HashMap<String, ProcessorPrefab> all = new HashMap<>();
    private static HashMap<String, ProcessorPrefab> registered = new HashMap<>();

    public static HashMap<String, ProcessorPrefab> all() {
        if(all.isEmpty()) {
            all.put(
                    "manufactory",
                    ProcessorBuilder
                            .make("manufactory", 0, 1, 0, 1)
                            .blockEntity(ManufactoryBE::new)
                            .build()
            );
        }
        return all;
    }

    public static HashMap<String, ProcessorPrefab> registered() {
        if(registered.isEmpty()) {
            for(String name: all().keySet()) {
                if (all().get(name).isRegistered())
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
            tmp.add(all().get(name).power);
        }
        return tmp;
    }

    public static List<Integer> initialTime() {
        List<Integer> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(all().get(name).time);
        }
        return tmp;
    }
}
