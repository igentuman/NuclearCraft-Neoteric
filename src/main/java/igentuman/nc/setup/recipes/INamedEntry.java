package igentuman.nc.setup.recipes;

public interface INamedEntry {

    /**
     * Used for retrieving the path/name of a registry object before the registry object has been fully initialized
     */
    String getInternalRegistryName();
}