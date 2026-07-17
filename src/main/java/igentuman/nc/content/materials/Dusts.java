package igentuman.nc.content.materials;

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