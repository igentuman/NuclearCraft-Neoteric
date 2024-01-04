package igentuman.nc.handler.sided;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.handler.sided.capability.Gas2FluidConverter;
import igentuman.nc.handler.sided.capability.Slurry2FluidConverter;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.handler.sided.SlotModePair.SlotMode.INPUT;

public class SidedContentHandler implements INBTSerializable<Tag> {

    public final int inputItemSlots;
    public final int outputItemSlots;
    public final int inputFluidSlots;
    public final int outputFluidSlots;
    public final ItemCapabilityHandler itemHandler;
    public final LazyOptional<ItemCapabilityHandler> itemCapability;
    public final FluidCapabilityHandler fluidCapability;

    public NuclearCraftBE blockEntity;
    private boolean updated = false;

    private Gas2FluidConverter gasConverter;
    private Slurry2FluidConverter slurryConverter;

    public SidedContentHandler(int inputItemSlots, int outputItemSlots, int inputFluidSlots, int outputFluidSlots) {
        this.inputItemSlots = inputItemSlots;
        this.outputItemSlots = outputItemSlots;
        this.inputFluidSlots = inputFluidSlots;
        this.outputFluidSlots = outputFluidSlots;
        if(inputItemSlots + outputItemSlots > 0) {
            itemHandler = new ItemCapabilityHandler(inputItemSlots, outputItemSlots);
            itemHandler.tile = blockEntity;
            itemHandler.sidedContentHandler = this;
            itemCapability = LazyOptional.of(() -> itemHandler);
        } else {
            itemHandler = null;
            itemCapability = LazyOptional.empty();
        }
        if(inputFluidSlots + outputFluidSlots > 0) {
            fluidCapability = new FluidCapabilityHandler(inputFluidSlots, outputFluidSlots);
            fluidCapability.tile = blockEntity;
            fluidCapability.sidedContentHandler = this;
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
        if(hasItemCapability(side)) return itemHandler.getCapability(side).cast();
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

    public int toggleSideConfig(int slotId, int direction) {
        try {
            if (slotId < inputFluidSlots) {
                return fluidCapability.toggleMode(getSlotIdFromGlobalId(slotId), direction);
            } else if (slotId < inputFluidSlots + inputItemSlots) {
                return itemHandler.toggleMode(getSlotIdFromGlobalId(slotId), direction);
            } else if (slotId < inputFluidSlots + inputItemSlots + outputFluidSlots) {
                return fluidCapability.toggleMode(getSlotIdFromGlobalId(slotId) + inputFluidSlots, direction);
            } else if (slotId < inputFluidSlots + outputFluidSlots + inputItemSlots + outputItemSlots) {
                return itemHandler.toggleMode(getSlotIdFromGlobalId(slotId) + inputItemSlots, direction);
            }
            return -1;
        } catch (NullPointerException|IndexOutOfBoundsException e) {
            return -1;
        }
    }

    public <RECIPE extends AbstractRecipe> void setBlockEntity(NuclearCraftBE blockEntity) {
        this.blockEntity = blockEntity;
        if(fluidCapability != null) {
            fluidCapability.tile = blockEntity;
        }
        if(itemHandler != null) {
            itemHandler.tile = blockEntity;
        }
    }

    public int getSlotIdFromGlobalId(int id) {
        if (id < inputFluidSlots) {
            return id;
        } else if (id - inputFluidSlots >= 0 && id - inputFluidSlots < inputItemSlots) {
            return id - inputFluidSlots;
        } else if (id - inputFluidSlots - inputItemSlots >= 0 && id - inputFluidSlots - inputItemSlots < outputFluidSlots) {
            return id - inputFluidSlots - inputItemSlots;
        } else if (id - inputFluidSlots - inputItemSlots - outputFluidSlots >= 0 && id - inputFluidSlots - inputItemSlots - outputFluidSlots < outputItemSlots) {
            return id - inputFluidSlots - inputItemSlots - outputFluidSlots;
        }
        return -1;
    }

    public SlotModePair.SlotMode getSlotMode(int direction, int slotId) {
        try {
            if (getSlotType(slotId) == INPUT) {
                if (slotId < inputFluidSlots) {
                    return fluidCapability.getMode(getSlotIdFromGlobalId(slotId), direction);
                }
                return itemHandler.getMode(getSlotIdFromGlobalId(slotId), direction);
            }

            if (slotId < inputFluidSlots + inputItemSlots + outputFluidSlots) {
                return fluidCapability.getMode(getSlotIdFromGlobalId(slotId) + inputFluidSlots, direction);
            }
            return itemHandler.getMode(getSlotIdFromGlobalId(slotId) + inputItemSlots, direction);
        } catch (NullPointerException|IndexOutOfBoundsException e) {
            return SlotModePair.SlotMode.UNKNOWN;
        }
    }

    public SlotModePair.SlotMode getSlotType(int id)
    {
        return id > (inputFluidSlots+inputItemSlots-1) ? SlotModePair.SlotMode.OUTPUT : INPUT;
    }

    public boolean tick() {
        updated = false;
        for(Direction dir: Direction.values()) {
            if(itemHandler != null) {
                updated = itemHandler.pushItems(dir) || updated;
                updated = itemHandler.pullItems(dir) || updated;
            }
            if(fluidCapability != null) {
                updated = fluidCapability.pushFluids(dir) || updated;
                updated = fluidCapability.pullFluids(dir) || updated;
            }
        }
        return updated;
    }

    public void clearHolded() {
        if(hasItemCapability(null)) {
            itemHandler.holdedInputs.clear();
        }
        if(hasFluidCapability(null)) {
            fluidCapability.holdedInputs.clear();
        }
    }

    public String getCacheKey() {
        String key = "";
        if(itemHandler != null) {
            key += itemHandler.getCacheKey();
        }
        if(fluidCapability != null) {
            key += fluidCapability.getCacheKey();
        }
        return key;
    }

    public void saveSideMap() {
        if(itemHandler != null) {
            itemHandler.sideMapUpdated = true;
        }
        if(fluidCapability != null) {
            fluidCapability.sideMapUpdated = true;
        }
    }

    public void setAllowedInputItems(List<ItemStack> allowedInputItems) {
        if(itemHandler != null) {
            itemHandler.allowedInputItems = allowedInputItems;
        }
    }

    public void setAllowedInputFluids(int slotId, List<FluidStack> allowedInputFluids) {
        if(fluidCapability != null) {
            if(fluidCapability.allowedFluids == null) {
                fluidCapability.allowedFluids = new HashMap<>();
            }
            fluidCapability.allowedFluids.remove(slotId);
            fluidCapability.allowedFluids.put(slotId, allowedInputFluids);
        }
    }

    public void voidSlot(int slotId) {
        try {
            if (getSlotType(slotId) == INPUT) {
                if (slotId < inputFluidSlots) {
                    fluidCapability.voidSlot(getSlotIdFromGlobalId(slotId));
                }
                itemHandler.voidSlot(getSlotIdFromGlobalId(slotId));
            }

            if (slotId < inputFluidSlots + inputItemSlots + outputFluidSlots) {
                fluidCapability.voidSlot(getSlotIdFromGlobalId(slotId) + inputFluidSlots);
            }
            itemHandler.voidSlot(getSlotIdFromGlobalId(slotId) + inputItemSlots);
        } catch (NullPointerException|IndexOutOfBoundsException e) {
        }
    }

    public Object[] getSlotContent(int slotId) {
        try {
            if (getSlotType(slotId) == INPUT) {
                if (slotId < inputFluidSlots) {
                    return fluidCapability.getSlotContent(getSlotIdFromGlobalId(slotId));
                }
                return itemHandler.getSlotContent(getSlotIdFromGlobalId(slotId));
            }

            if (slotId < inputFluidSlots + inputItemSlots + outputFluidSlots) {
                return fluidCapability.getSlotContent(getSlotIdFromGlobalId(slotId) + inputFluidSlots);
            }
            return itemHandler.getSlotContent(getSlotIdFromGlobalId(slotId) + inputItemSlots);
        } catch (NullPointerException|IndexOutOfBoundsException e) {
            return new Object[] {};
        }
    }

    public <T> T gasConverter(Direction side) {
        if(gasConverter == null) {
            gasConverter = new Gas2FluidConverter();
            gasConverter.setFluidHandler(fluidCapability);
        }
        return (T) gasConverter.forSide(side);
    }

    public <T> T getSlurryConverter(Direction side) {
        if(slurryConverter == null) {
            slurryConverter = new Slurry2FluidConverter();
            slurryConverter.setFluidHandler(fluidCapability);
        }
        return (T) gasConverter.forSide(side);
    }

    public void voidFluidSlot(int slotId) {
        if(fluidCapability != null) {
            fluidCapability.voidSlot(slotId);
        }
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
