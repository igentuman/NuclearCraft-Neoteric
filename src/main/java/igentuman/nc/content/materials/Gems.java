package igentuman.nc.content.materials;

import java.util.Collection;

public class Gems extends AbstractMaterial {
    protected static String type = "gem";
    protected static AbstractMaterial instance;
    public static Gems get()
    {
        if(instance == null) {
            instance = new Gems();
            instance.items = Materials.gems().values();
        }
        return (Gems) instance;
    }
}