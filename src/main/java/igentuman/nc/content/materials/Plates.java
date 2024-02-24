package igentuman.nc.content.materials;

public class Plates extends AbstractMaterial {
    protected static String type = "plate";
    protected static AbstractMaterial instance;
    public static Plates get()
    {
        if(instance == null) {
            instance = new Plates();
            instance.items = Materials.plates().values();
        }
        return (Plates) instance;
    }
}