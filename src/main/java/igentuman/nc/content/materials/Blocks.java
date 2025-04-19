package igentuman.nc.content.materials;

public class Blocks  extends AbstractMaterial {

    protected String type = "block";
    protected static AbstractMaterial instance;

    public static Blocks get()
    {
        if(instance == null) {
            instance = new Blocks();
            instance.type="block";
            instance.items = Materials.blocks().values();
        }
        return (Blocks) instance;
    }
}