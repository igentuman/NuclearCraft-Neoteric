package igentuman.nc.content.materials;

public class Nuggets extends AbstractMaterial {

    protected static AbstractMaterial instance;

    public static Nuggets get()
    {
        if(instance == null) {
            instance = new Nuggets();
            instance.type = "nugget";
            instance.items = Materials.nuggets().values();
        }
        return (Nuggets) instance;
    }
}