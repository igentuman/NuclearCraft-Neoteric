package igentuman.nc.util;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

public class PortMode {
    public static final EnumProperty<Mode> PORT_MODE = EnumProperty.create("port_mode", Mode.class, Mode.DISABLED, Mode.INPUT, Mode.OUTPUT);

    public enum Mode implements StringRepresentable {
        INPUT("input"),
        OUTPUT("output"),
        DISABLED("disabled");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        public Mode next() {
            return switch (this) {
                case INPUT -> OUTPUT;
                case OUTPUT -> DISABLED;
                default -> INPUT;
            };
        }
    }
}
