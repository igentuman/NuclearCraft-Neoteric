package igentuman.nc.content.materials;

public class Chunks extends AbstractMaterial {
    protected static AbstractMaterial instance;
    public static Chunks get()
    {
        if(instance == null) {
            instance = new Chunks();
            instance.type="chunk";
            instance.items = Materials.chunks().values();
        }
        return (Chunks) instance;
    }
}