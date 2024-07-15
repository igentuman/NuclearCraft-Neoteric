package igentuman.nc.content.materials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Dusts  extends AbstractMaterial {

    protected static AbstractMaterial instance;

    public static Dusts get()
    {
        if(instance == null) {
            instance = new Dusts();
            instance.type = "dust";
            instance.items = Materials.dusts().values();
        }
        return (Dusts) instance;
    }
}