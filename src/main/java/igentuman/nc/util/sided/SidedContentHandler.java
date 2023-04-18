package igentuman.nc.util.sided;

import igentuman.nc.block.entity.processor.NCProcessor;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.util.sided.capability.FluidCapabilityHandler;
import igentuman.nc.util.sided.capability.ItemCapabilityHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashMap;

public class SidedContentHandler implements INBTSerializable<Tag> {

    public final int inputItemSlots;
    public final int outputItemSlots;
    public final int inputFluidSlots;
    public final int outputFluidSlots;
    public final ItemCapabilityHandler itemHandler;
    public final LazyOptional<ItemCapabilityHandler> itemCapability;
    public final FluidCapabilityHandler fluidCapability;

    public NCProcessor processor;

    public SidedContentHandler(int inputItemSlots, int outputItemSlots, int inputFluidSlots, int outputFluidSlots) {
        this.inputItemSlots = inputItemSlots;
        this.outputItemSlots = outputItemSlots;
        this.inputFluidSlots = inputFluidSlots;
        this.outputFluidSlots = outputFluidSlots;
        if(inputItemSlots + outputItemSlots > 0) {
            itemHandler = new ItemCapabilityHandler(inputItemSlots, outputItemSlots);
            itemCapability = LazyOptional.of(() -> itemHandler);
        } else {
            itemHandler = null;
            itemCapability = LazyOptional.empty();
        }
        if(inputFluidSlots + outputFluidSlots > 0) {
            fluidCapability = new FluidCapabilityHandler(inputFluidSlots, outputFluidSlots);
        } else {
            fluidCapability = null;
        }
    }

    public static Tag serializeSideMap(HashMap<Integer, SlotModePair[]> sideMap) {
        CompoundTag nbt = new CompoundTag();
        for (int i = 0; i < 6; i++) {
            nbt.put("side"+i, SlotModePair.serializeArray(sideMap.get(i)));
        }
        return nbt;
    }

    public static HashMap<Integer, SlotModePair[]> deserializeSideMap(CompoundTag sideMap) {
        HashMap<Integer, SlotModePair[]> map = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            map.put(i, SlotModePair.deserializeArray(sideMap.getCompound("side"+i)));
        }
        return map;
    }

    @Override
    public Tag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if(itemHandler != null) {
            nbt.put("itemHandler", itemHandler.serializeNBT());
        }
        if(fluidCapability != null) {
            nbt.put("fluidHandler", fluidCapability.serializeNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if(itemHandler != null) {
            itemHandler.deserializeNBT(((CompoundTag) nbt).getCompound("itemHandler"));
        }
        if(fluidCapability != null) {
            fluidCapability.deserializeNBT(((CompoundTag) nbt).getCompound("fluidHandler"));
        }
    }

    public <T> LazyOptional<T> getItemCapability(Direction side) {
        if(hasItemCapability(side)) return itemCapability.cast();
        return LazyOptional.empty();
    }

    public <T> LazyOptional<T> getFluidCapability(Direction side) {
        if(hasFluidCapability(side)) return fluidCapability.getCapability(side).cast();
        return LazyOptional.empty();
    }

    public boolean hasFluidCapability(Direction side) {
        if(inputFluidSlots+outputFluidSlots == 0) return false;
        return side == null || fluidCapability.sideMap.get(side.ordinal()).length > 0;
    }

    public boolean hasItemCapability(Direction side) {
        if(inputItemSlots+outputItemSlots == 0) return false;
        return side == null || itemHandler.sideMap.get(side.ordinal()).length > 0;
    }
    public void invalidate() {
        itemCapability.invalidate();
    }

    public void toggleSideConfig(int slotId, int direction) {
        if(slotId < inputItemSlots) {
            itemHandler.toggleMode(slotId, direction);
        } else if(slotId < inputItemSlots+outputItemSlots) {
            itemHandler.toggleMode(slotId, direction);
        } else if(slotId < inputItemSlots+outputItemSlots+inputFluidSlots) {
            fluidCapability.toggleMode(slotId-inputItemSlots-outputItemSlots, direction);
        } else if(slotId < inputItemSlots+outputItemSlots+inputFluidSlots+outputFluidSlots) {
            fluidCapability.toggleMode(slotId-inputItemSlots-outputItemSlots, direction);
        }
    }

    public <RECIPE extends NcRecipe> void setProcessor(NCProcessor<RECIPE> recipencProcessor) {
        processor = recipencProcessor;
    }

    public SlotModePair.SlotMode getSlotMode(int direction, int slotId) {
        if(slotId < inputItemSlots) {
            return itemHandler.getMode(slotId, direction);
        } else if(slotId < inputItemSlots+outputItemSlots) {
            return itemHandler.getMode(slotId, direction);
        } else if(slotId < inputItemSlots+outputItemSlots+inputFluidSlots) {
            return fluidCapability.getMode(slotId-inputItemSlots-outputItemSlots, direction);
        } else if(slotId < inputItemSlots+outputItemSlots+inputFluidSlots+outputFluidSlots) {
            return fluidCapability.getMode(slotId-inputItemSlots-outputItemSlots, direction);
        }
        return null;
    }

    public enum SlotType {
        INPUT,
        OUTPUT;
    }

    public enum RelativeDirection {
        FRONT,
        BACK,
        LEFT,
        RIGHT,
        UP,
        DOWN;

        public static Direction toAbsolute(RelativeDirection relativeDirection, Direction facing) {
            switch (relativeDirection) {
                case FRONT:
                    return facing;
                case BACK:
                    return facing.getOpposite();
                case LEFT:
                    return facing.getClockWise();
                case RIGHT:
                    return facing.getCounterClockWise();
                case UP:
                    return Direction.UP;
                case DOWN:
                    return Direction.DOWN;
            }
            return null;
        }

        public static RelativeDirection toRelative(Direction absoluteDirection, Direction facing) {
            if(absoluteDirection == facing) return FRONT;
            if(absoluteDirection == facing.getOpposite()) return BACK;
            if(absoluteDirection == facing.getClockWise()) return LEFT;
            if(absoluteDirection == facing.getCounterClockWise()) return RIGHT;
            if(absoluteDirection == Direction.UP) return UP;
            if(absoluteDirection == Direction.DOWN) return DOWN;
            return null;
        }

        public static String getDirectionName(int direction) {
            return values()[direction].name().toUpperCase();
        }
    }
}
