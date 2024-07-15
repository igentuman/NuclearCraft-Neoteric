package igentuman.nc.content.materials;

import java.util.Collection;

public class Gems extends AbstractMaterial {

    protected static AbstractMaterial instance;
    public static Gems get()
    {
        if(instance == null) {
            instance = new Gems();
            instance.type = "gem";
            instance.items = Materials.gems().values();
        }
        return (Gems) instance;
    }
}