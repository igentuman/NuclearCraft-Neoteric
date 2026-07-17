package igentuman.nc.compat.tis3d;

import igentuman.nc.block.fusion.entity.FusionCoreBE;
import li.cil.tis3d.api.serial.SerialInterface;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.serial.SerialProtocolDocumentationReference;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;

import static igentuman.nc.util.TextUtils.__;

public final class FusionCoreSerialInterfaceProvider implements SerialInterfaceProvider {

    private static final Component DOCUMENTATION_TITLE = __("tis3d.manual.serial_protocols.ncn_fusion");
    private static final String DOCUMENTATION_LINK = "ncn_fusion.md";
    private static final SerialProtocolDocumentationReference DOCUMENTATION_REFERENCE;

    public boolean matches(Level level, BlockPos position, Direction side) {
        return level.getBlockEntity(position) instanceof FusionCoreBE;
    }

    public Optional<SerialInterface> getInterface(Level level, BlockPos position, Direction face) {
        FusionCoreBE port = (FusionCoreBE) Objects.requireNonNull((FusionCoreBE)level.getBlockEntity(position));
        return Optional.of(new FusionCoreSerialInterface(port));
    }

    public Optional<SerialProtocolDocumentationReference> getDocumentationReference() {
        return Optional.of(DOCUMENTATION_REFERENCE);
    }

    public boolean stillValid(Level level, BlockPos position, Direction side, SerialInterface serialInterface) {
        return serialInterface instanceof FusionCoreSerialInterface;
    }

    static {
        DOCUMENTATION_REFERENCE = new SerialProtocolDocumentationReference(DOCUMENTATION_TITLE, "ncn_fusion.md");
    }

    private static final class FusionCoreSerialInterface implements SerialInterface {
        private static final String TAG_MODE = "mode";
        private final FusionCoreBE core;
        private Mode mode;

        FusionCoreSerialInterface(FusionCoreBE core) {
            this.mode = Mode.HEAT;
            this.core = core;
        }

        public boolean canWrite() {
            return true;
        }

        public void write(short value) {
            this.mode = switch (value) {
                case 0 -> Mode.ENERGY;
                case 1 -> Mode.HEAT;
                case 2 -> Mode.EFFICIENCY;
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
                    value = this.core.energyStorage().getEnergyStored();
                    total = this.core.energyStorage().getMaxEnergyStored();
                    if (total > 0) {
                        return (short)(value * 100 / total);
                    }
                    break;
                case HEAT:
                    value = this.core.reactorHeat;
                    total = this.core.getMaxHeat();
                    if (total > 0) {
                        return (short)(value * 100 / total);
                    }
                    break;
                case EFFICIENCY:
                    return (short) (this.core.efficiency * 100);
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
            EFFICIENCY
        }
    }
}
