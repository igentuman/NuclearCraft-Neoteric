package igentuman.nc.content.materials;

import java.util.Collection;

public class Chunks extends AbstractMaterial {
    protected static String type = "chunk";
    protected static AbstractMaterial instance;
    public static Chunks get()
    {
        if(instance == null) {
            instance = new Chunks();
            instance.items = Materials.chunks().values();
        }
        return (Chunks) instance;
    }

}