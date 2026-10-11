package igentuman.nc.compat.tis3d;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.block_entity.OutputLevelSource;
import li.cil.tis3d.api.serial.SerialInterface;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.serial.SerialProtocolDocumentationReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ControllerSerialProvider implements SerialInterfaceProvider {

    private final Class<? extends OutputLevelSource> controllerType;
    private final int[] modes;
    private final int defaultMode;
    private final SerialProtocolDocumentationReference documentation;

    public ControllerSerialProvider(Class<? extends OutputLevelSource> controllerType, String protocol, int defaultMode, int... modes) {
        this.controllerType = controllerType;
        this.modes = modes;
        this.defaultMode = defaultMode;
        this.documentation = new SerialProtocolDocumentationReference(
                Component.translatable("tis3d.manual.serial_protocols." + protocol), protocol + ".md");
    }

    @Override
    public boolean matches(Level level, BlockPos position, Direction side) {
        return controllerOf(level.getBlockEntity(position)) != null;
    }

    @Override
    public Optional<SerialInterface> getInterface(Level level, BlockPos position, Direction side) {
        BlockEntity source = level.getBlockEntity(position);
        return source == null ? Optional.empty() : Optional.of(new ControllerSerialInterface(source));
    }

    @Override
    public Optional<SerialProtocolDocumentationReference> getDocumentationReference() {
        return Optional.of(documentation);
    }

    @Override
    public boolean stillValid(Level level, BlockPos position, Direction side, SerialInterface serialInterface) {
        return serialInterface instanceof ControllerSerialInterface channel
                && channel.provider() == this
                && channel.source == level.getBlockEntity(position);
    }

    @Nullable
    private OutputLevelSource controllerOf(@Nullable BlockEntity blockEntity) {
        MultiblockControllerBE controller = switch (blockEntity) {
            case MultiblockControllerBE c -> c;
            case MultiblockPortBE port -> port.controller();
            case null, default -> null;
        };
        return controllerType.isInstance(controller) ? controllerType.cast(controller) : null;
    }

    private final class ControllerSerialInterface implements SerialInterface {

        private static final String TAG_MODE = "mode";

        private final BlockEntity source;
        private int mode = defaultMode;

        ControllerSerialInterface(BlockEntity source) {
            this.source = source;
        }

        ControllerSerialProvider provider() {
            return ControllerSerialProvider.this;
        }

        @Override
        public boolean canWrite() {
            return true;
        }

        @Override
        public void write(short value) {
            if (value >= 0 && value < modes.length) mode = value;
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public short peek() {
            OutputLevelSource controller = controllerOf(source);
            return controller == null ? 0 : (short) (controller.outputLevel(modes[mode]) * 100);
        }

        @Override
        public void skip() {
        }

        @Override
        public void reset() {
            mode = defaultMode;
        }

        @Override
        public void save(CompoundTag tag) {
            tag.putInt(TAG_MODE, mode);
        }

        @Override
        public void load(CompoundTag tag) {
            int saved = tag.contains(TAG_MODE) ? tag.getInt(TAG_MODE) : defaultMode;
            mode = saved >= 0 && saved < modes.length ? saved : defaultMode;
        }
    }
}
