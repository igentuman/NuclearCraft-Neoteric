package igentuman.nc.phosphophyllite.multiblock2.rectangular;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public enum AxisPosition implements StringRepresentable {
    LOWER("lower"),
    MIDDLE("middle"),
    UPPER("upper");
    
    private final String name;
    
    AxisPosition(String name) {
        this.name = name;
    }
    
    @Override
    public String getSerializedName() {
        return toString().toLowerCase(Locale.US);
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public static final EnumProperty<AxisPosition> X_AXIS_POSITION = EnumProperty.create("x_axis", AxisPosition.class);
    public static final EnumProperty<AxisPosition> Y_AXIS_POSITION = EnumProperty.create("y_axis", AxisPosition.class);
    public static final EnumProperty<AxisPosition> Z_AXIS_POSITION = EnumProperty.create("z_axis", AxisPosition.class);
    
}
