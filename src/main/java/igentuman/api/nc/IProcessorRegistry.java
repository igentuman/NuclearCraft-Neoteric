package igentuman.api.nc;

import igentuman.nc.content.processors.ProcessorPrefab;

import java.util.HashMap;

public interface IProcessorRegistry {
    void registerProcessors(HashMap<String, ProcessorPrefab> registry);
}