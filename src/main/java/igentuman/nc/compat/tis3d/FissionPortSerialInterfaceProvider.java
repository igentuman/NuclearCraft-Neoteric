package igentuman.nc.compat.tis3d;

import igentuman.nc.block.fission.entity.FissionPortBE;
import li.cil.tis3d.api.serial.SerialInterface;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.serial.SerialProtocolDocumentationReference;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

import java.util.Objects;
import java.util.Optional;

import static igentuman.nc.util.TextUtils.__;

public final class FissionPortSerialInterfaceProvider implements SerialInterfaceProvider {

    private static final Component DOCUMENTATION_TITLE = __("tis3d.manual.serial_protocols.ncn_fission");
    private static final String DOCUMENTATION_LINK = "ncn_fission.md";
    private static final SerialProtocolDocumentationReference DOCUMENTATION_REFERENCE;

    public boolean matches(Level level, BlockPos position, Direction side) {
        return level.getBlockEntity(position) instanceof FurnaceBlockEntity;
    }

    public Optional<SerialInterface> getInterface(Level level, BlockPos position, Direction face) {
        FissionPortBE port = (FissionPortBE) Objects.requireNonNull((FissionPortBE)level.getBlockEntity(position));
        return Optional.of(new FissionPortSerialInterface(port));
    }

    public Optional<SerialProtocolDocumentationReference> getDocumentationReference() {
        return Optional.of(DOCUMENTATION_REFERENCE);
    }

    public boolean stillValid(Level level, BlockPos position, Direction side, SerialInterface serialInterface) {
        return serialInterface instanceof FissionPortSerialInterface;
    }

    static {
        DOCUMENTATION_REFERENCE = new SerialProtocolDocumentationReference(DOCUMENTATION_TITLE, "ncn_fission.md");
    }

    private static final class FissionPortSerialInterface implements SerialInterface {
        private static final String TAG_MODE = "mode";
        private final FissionPortBE port;
        private Mode mode;

        FissionPortSerialInterface(FissionPortBE port) {
            this.mode = Mode.HEAT;
            this.port = port;
        }

        public boolean canWrite() {
            return true;
        }

        public void write(short value) {
            this.mode = switch (value) {
                case 0 -> Mode.ENERGY;
                case 1 -> Mode.HEAT;
                case 2 -> Mode.PROGRESS;
                case 3 -> Mode.ITEMS;
                default -> this.mode;
            };
        }

        public boolean canRead() {
            return true;
        }

        public short peek() {
            double value = 0;
            double total = 0;
            switch (this.mode) {
                case ENERGY:
                    value = this.port.getEnergyStored();
                    total = this.port.getMaxEnergyStored();
                    if (total > 0) {
                        return (short)(value * 100 / total);
                    }
                    break;
                case HEAT:
                    value = this.port.getHeatStored();
                    total = this.port.getMaxHeat();
                    if (total > 0) {
                        return (short)(value * 100 / total);
                    }
                    break;
                case PROGRESS:
                    return (short) (this.port.getDepletionProgress() * 100);
                case ITEMS:
                    return (short) (this.port.getFuelCount() * 100 / 64);
                default:
                    return 0;
            }
            return 0;
        }

        public void skip() {
        }

        public void reset() {
            this.mode = Mode.HEAT;
        }

        public void load(CompoundTag tag) {
            this.mode = (Mode) EnumUtils.load(Mode.class, TAG_MODE, tag);
        }

        public void save(CompoundTag tag) {
            EnumUtils.save(this.mode, TAG_MODE, tag);
        }

        private static enum Mode {
            ENERGY,
            HEAT,
            PROGRESS,
            ITEMS
        }
    }
}
