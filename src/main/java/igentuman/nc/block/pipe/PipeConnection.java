package igentuman.nc.block.pipe;

import net.minecraft.util.StringRepresentable;

public enum PipeConnection implements StringRepresentable {
    NONE("none"),
    PIPE("pipe"),
    MACHINE("machine");

    private final String name;

    PipeConnection(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
