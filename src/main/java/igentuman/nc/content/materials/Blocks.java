package igentuman.nc.content.materials;


import java.util.Collection;

public class Blocks  extends AbstractMaterial {
    protected String type = "block";
    protected static AbstractMaterial instance;
    public static Blocks get()
    {
        if(instance == null) {
            instance = new Blocks();
            instance.items = Materials.blocks().values();
        }
        return (Blocks) instance;
    }
}