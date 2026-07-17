package igentuman.nc.content.materials;

public class Plates extends AbstractMaterial {

    protected static AbstractMaterial instance;
    public static Plates get()
    {
        if(instance == null) {
            instance = new Plates();
            instance.type = "plate";
            instance.items = Materials.plates().values();
        }
        return (Plates) instance;
    }
}