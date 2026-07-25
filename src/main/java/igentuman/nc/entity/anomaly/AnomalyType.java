package igentuman.nc.entity.anomaly;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

import static igentuman.nc.NuclearCraft.rl;

public enum AnomalyType {
    GRAVITATIONAL(() -> ParticleTypes.PORTAL, 0x2A4DBF, "gravitational", 4, 7),
    ELECTRIC(() -> ParticleTypes.ELECTRIC_SPARK, 0x66CCFF, "electric", 4, 5),
    RADIOACTIVE(() -> ParticleTypes.HAPPY_VILLAGER, 0x33FF33, "radiation", 4, 4),
    BURNING(() -> ParticleTypes.FLAME, 0xFF6600, "burning", 4, 5),
    PSYCHO(() -> ParticleTypes.WITCH, 0xCC33FF, "psycho", 4, 4),
    TELEPORTING(() -> ParticleTypes.PORTAL, 0xFF66FF, "teleporting", 4, 4);

    private final Supplier<ParticleOptions> ambientParticle;
    private final int color;
    private final ResourceLocation texture;
    private final int gridCols;
    private final int gridRows;

    AnomalyType(Supplier<ParticleOptions> ambientParticle, int color, String textureName, int gridCols, int gridRows) {
        this.ambientParticle = ambientParticle;
        this.color = color;
        this.texture = rl("textures/particle/anomaly/" + textureName + ".png");
        this.gridCols = gridCols;
        this.gridRows = gridRows;
    }

    public String id() {
        return name().toLowerCase();
    }

    public ParticleOptions ambientParticle() {
        return ambientParticle.get();
    }

    public int color() {
        return color;
    }

    public ResourceLocation texture() {
        return texture;
    }

    public int gridCols() {
        return gridCols;
    }

    public int gridRows() {
        return gridRows;
    }

    public int frameCount() {
        return gridCols * gridRows;
    }

    public static AnomalyType byOrdinal(int ordinal) {
        AnomalyType[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return GRAVITATIONAL;
        }
        return values[ordinal];
    }
}
