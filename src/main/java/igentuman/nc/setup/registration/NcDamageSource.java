package igentuman.nc.setup.registration;

import igentuman.nc.NuclearCraft;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NothingNullByDefault
public class NcDamageSource extends DamageSource  {

    private static final List<NcDamageSource> INTERNAL_DAMAGE_SOURCES = new ArrayList<>();
    public static final List<NcDamageSource> DAMAGE_SOURCES = Collections.unmodifiableList(INTERNAL_DAMAGE_SOURCES);
    public static final NcDamageSource RADIATION = new NcDamageSource("radiation").bypassArmor();

    private final String translationKey;
    @Nullable
    private final Vec3 damageLocation;


    private NcDamageSource(String damageType) {
        this(damageType, null);
        INTERNAL_DAMAGE_SOURCES.add(this);
    }

    private NcDamageSource(@NotNull String damageType, @Nullable Vec3 damageLocation) {
        super(NuclearCraft.MODID + "." + damageType);
        this.translationKey = "death.attack." + getMsgId();
        this.damageLocation = damageLocation;
    }

    /**
     * Gets a new instance of this damage source, that is positioned at the given location.
     */
    public NcDamageSource fromPosition(@NotNull Vec3 damageLocation) {
        return new NcDamageSource(getMsgId(), damageLocation);
    }

    public String getTranslationKey() {
        return translationKey;
    }

    @Nullable
    @Override
    public Vec3 getSourcePosition() {
        return damageLocation;
    }

    @Override
    public NcDamageSource setProjectile() {
        super.setProjectile();
        return this;
    }

    @Override
    public NcDamageSource setExplosion() {
        super.setExplosion();
        return this;
    }

    @Override
    public NcDamageSource bypassArmor() {
        super.bypassArmor();
        return this;
    }

    @Override
    public NcDamageSource bypassInvul() {
        super.bypassInvul();
        return this;
    }

    @Override
    public NcDamageSource bypassMagic() {
        super.bypassMagic();
        return this;
    }

    @Override
    public NcDamageSource setIsFire() {
        super.setIsFire();
        return this;
    }

    @Override
    public NcDamageSource setScalesWithDifficulty() {
        super.setScalesWithDifficulty();
        return this;
    }

    @Override
    public NcDamageSource setMagic() {
        super.setMagic();
        return this;
    }
}