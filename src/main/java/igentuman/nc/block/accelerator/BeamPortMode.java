package igentuman.nc.block.accelerator;

import net.minecraft.util.StringRepresentable;

public enum BeamPortMode implements StringRepresentable {
    INPUT("input"),
    OUTPUT("output"),
    DISABLED("disabled");

    private final String serializedName;

    BeamPortMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
