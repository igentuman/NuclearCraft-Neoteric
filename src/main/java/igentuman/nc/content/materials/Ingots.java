package igentuman.nc.content.materials;

public class Ingots extends AbstractMaterial {
    protected static String type = "ingot";
    protected static AbstractMaterial instance;
    public static Ingots get()
    {
        if(instance == null) {
            instance = new Ingots();
            instance.items = Materials.ingots().values();
        }
        return (Ingots) instance;
    }
}