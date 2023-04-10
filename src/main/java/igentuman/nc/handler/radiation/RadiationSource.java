package igentuman.nc.handler.radiation;

import igentuman.nc.util.NBTConstants;
import igentuman.nc.util.functions.Coord4D;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static igentuman.nc.handler.config.CommonConfig.RadiationConfig.DECAY_SPEED;

public class RadiationSource implements IRadiationSource {

    private final Coord4D pos;
    /** In Sv/h */
    private double magnitude;

    public RadiationSource(Coord4D pos, double magnitude) {
        this.pos = pos;
        this.magnitude = magnitude;
    }

    @NotNull
    @Override
    public Coord4D getPos() {
        return pos;
    }

    @Override
    public double getMagnitude() {
        return magnitude;
    }

    @Override
    public void radiate(double magnitude) {
        this.magnitude += magnitude;
    }

    @Override
    public boolean decay() {
        magnitude *= DECAY_SPEED.get();
        return magnitude < RadiationManager.MIN_MAGNITUDE;
    }

    public static RadiationSource load(CompoundTag tag) {
        return new RadiationSource(Coord4D.read(tag), tag.getDouble(NBTConstants.RADIATION));
    }

    public void write(CompoundTag tag) {
        pos.write(tag);
        tag.putDouble(NBTConstants.RADIATION, magnitude);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RadiationSource other = (RadiationSource) o;
        return magnitude == other.magnitude && pos.equals(other.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, magnitude);
    }
}