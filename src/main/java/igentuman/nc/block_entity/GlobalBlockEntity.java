package igentuman.nc.block_entity;

import igentuman.nc.handler.SidedContentHandler;
import igentuman.nc.handler.energy.CustomEnergyStorage;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.util.BoilingBuffer;
import igentuman.nc.util.HeatBuffer;
import igentuman.nc.recipe.ProcessorRecipeInput;
import igentuman.nc.recipe.RecipeInfo;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import igentuman.nc.util.NBTSerializable;
import igentuman.nc.util.caps.EnergyCapDefinition;
import igentuman.nc.util.caps.FluidCapDefinition;
import igentuman.nc.util.caps.ItemCapDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.level.block.Block.UPDATE_CLIENTS;

/** Base block entity providing reflection-driven NBT/sync, sided item/fluid/energy capabilities, and recipe ticking. */
public class GlobalBlockEntity extends BlockEntity {

    public String name;
    public final RecipeInfo recipeInfo = new RecipeInfo(this);

    /** Sided capability orchestrator. */
    public final SidedContentHandler contentHandler;

    /** Total number of item slots (0 if no item cap). */
    public final int slotCount;

    /** Total number of fluid tanks (0 if no fluid cap). */
    public final int tankCount;

    /** Energy storage capability - null if no energy cap is defined in the ModEntry. */
    @Nullable
    public final CustomEnergyStorage energyStorage;

    private final List<Field> booleanFields;
    private final List<Field> intFields;
    private final List<Field> intArrayFields;
    private final List<Field> doubleFields;
    private final List<Field> stringFields;
    private final List<Field> stringArrayFields;
    private final List<Field> floatFields;
    private final List<Field> byteFields;
    private final List<Field> longFields;
    private final List<Field> blockPosFields;
    private final List<Field> directionFields;
    private final List<Field> bufferFields;

    /** Fields annotated with @NBTField(syncToClient = true), used for ContainerData sync. */
    private final List<Field> syncFields;

    /**
     * Dynamically built ContainerData that syncs all @NBTField(syncToClient = true) fields.
     * Supports int, boolean, byte, and short field types.
     * Adding a new annotated field to any subclass will automatically include it.
     */
    public final ContainerData containerData;

    @NBTField(syncToClient = true)
    public int progress = 0;
    @NBTField(syncToClient = true)
    public int maxProgress = 100;

    protected boolean wasChanged = false;

    protected Object currentSound;

    protected void playSound(SoundEvent sound, float volume) {
        if (level == null || !level.isClientSide) return;
        currentSound = igentuman.nc.client.sound.BlockEntitySounds.play(currentSound, sound, volume, worldPosition);
    }

    protected void stopSound() {
        if (currentSound == null) return;
        igentuman.nc.client.sound.BlockEntitySounds.stop(currentSound);
        currentSound = null;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide) stopSound();
    }

    public boolean hasInventory() {
        return contentHandler.hasItemCapability();
    }

    public boolean hasFluidTanks() {
        return contentHandler.hasFluidCapability();
    }

    public boolean hasEnergyStorage() {
        return energyStorage != null && energyStorage.getMaxEnergyStored() > 0;
    }

    public void markDirty() {
        wasChanged = true;
    }

    /**
     * Returns the side-aware IItemHandler for the given side.
     * Applies slot mode filtering per side configuration.
     */
    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return contentHandler.getItemCapability(side);
    }

    /**
     * Returns the side-aware IFluidHandler for the given side.
     * Applies tank mode filtering per side configuration.
     */
    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return contentHandler.getFluidCapability(side);
    }

    /**
     * Returns the IEnergyStorage for the given side.
     * Energy is accessible from all sides.
     */
    @Nullable
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return energyStorage;
    }

    public GlobalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, String name) {
        super(type, pos, blockState);
        this.name = name;

        this.contentHandler = new SidedContentHandler();

        // Initialize item inventory from ModEntry's itemCap definition
        ItemCapDefinition capDef = null;
        if (name != null) {
            ModEntry entry = ModEntries.get(name);
            if (entry != null && entry.itemCap() != null) {
                capDef = entry.itemCap();
            }
        }
        if (capDef != null) {
            int inputSlots = capDef.inputSlots;
            int outputSlots = capDef.outputSlots;
            int extraSlots = capDef.globalSlots + capDef.catalystSlots + capDef.hiddenSlots;
            ItemCapabilityHandler itemCapHandler = new ItemCapabilityHandler(inputSlots, outputSlots, extraSlots);
            contentHandler.setItemHandler(itemCapHandler);
            this.slotCount = itemCapHandler.getTotalSlots();
        } else {
            this.slotCount = 0;
        }

        // Initialize fluid tanks from ModEntry's fluidCap definition
        FluidCapDefinition fluidCapDef = null;
        if (name != null) {
            ModEntry entry = ModEntries.get(name);
            if (entry != null && entry.fluidCap() != null) {
                fluidCapDef = entry.fluidCap();
            }
        }
        if (fluidCapDef != null) {
            List<FluidCapDefinition.Tank> allTanks = new ArrayList<>();
            allTanks.addAll(fluidCapDef.inputTanks);
            allTanks.addAll(fluidCapDef.outputTanks);
            allTanks.addAll(fluidCapDef.globalTanks);
            int totalTanks = allTanks.size();
            int[] capacities = new int[totalTanks];
            for (int i = 0; i < totalTanks; i++) {
                capacities[i] = allTanks.get(i).capacity;
            }
            int inputTanks = fluidCapDef.inputTanks.size();
            int outputTanks = fluidCapDef.outputTanks.size();
            int globalTanks = fluidCapDef.globalTanks.size();
            FluidCapabilityHandler fluidCapHandler = new FluidCapabilityHandler(inputTanks, outputTanks, globalTanks, capacities);
            contentHandler.setFluidHandler(fluidCapHandler);
            this.tankCount = totalTanks;
        } else {
            this.tankCount = 0;
        }

        // Initialize energy storage from ModEntry's energyCap definition
        EnergyCapDefinition energyCapDef = null;
        if (name != null) {
            ModEntry energyEntry = ModEntries.get(name);
            if (energyEntry != null && energyEntry.energyCap() != null) {
                energyCapDef = energyEntry.energyCap();
            }
        }
        if (energyCapDef != null) {
            final int cap = energyCapDef.getCapacity();
            final int maxIn = energyCapDef.getInputRate();
            final int maxOut = energyCapDef.getOutputRate();
            this.energyStorage = CustomEnergyStorage.create(cap, maxIn, maxOut, this::setChanged);
        } else {
            this.energyStorage = null;
        }

        contentHandler.setBlockEntity(this);

        directionFields = initFields(Direction.class);
        booleanFields = initFields(boolean.class);
        intFields = initFields(int.class);
        intArrayFields = initFields(int[].class);
        doubleFields = initFields(double.class);
        stringFields = initFields(String.class);
        stringArrayFields = initFields(String[].class);
        blockPosFields = initFields(BlockPos.class);
        floatFields = initFields(float.class);
        byteFields = initFields(byte.class);
        longFields = initFields(long.class);
        bufferFields = initBufferFields();

        syncFields = initSyncFields();

        containerData = new ContainerData() {
            @Override
            public int get(int index) {
                if (index < 0 || index >= syncFields.size()) return 0;
                Field field = syncFields.get(index);
                try {
                    Class<?> type = field.getType();
                    if (type == int.class) return field.getInt(GlobalBlockEntity.this);
                    if (type == boolean.class) return field.getBoolean(GlobalBlockEntity.this) ? 1 : 0;
                    if (type == byte.class) return field.getByte(GlobalBlockEntity.this);
                    if (type == short.class) return field.getShort(GlobalBlockEntity.this);
                } catch (IllegalAccessException e) {
                    return 0;
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                if (index < 0 || index >= syncFields.size()) return;
                Field field = syncFields.get(index);
                try {
                    Class<?> type = field.getType();
                    if (type == int.class) field.setInt(GlobalBlockEntity.this, value);
                    else if (type == boolean.class) field.setBoolean(GlobalBlockEntity.this, value != 0);
                    else if (type == byte.class) field.setByte(GlobalBlockEntity.this, (byte) value);
                    else if (type == short.class) field.setShort(GlobalBlockEntity.this, (short) value);
                } catch (IllegalAccessException ignored) { }
            }

            @Override
            public int getCount() {
                return syncFields.size();
            }
        };
    }

    public void setChanged() {
        super.setChanged();
        wasChanged = true;
    }

    /**
     * Collects all @NBTField-annotated fields from the entire class hierarchy.
     * Walks from the concrete subclass up to (but not including) GlobalBlockEntity's parent.
     */
    private List<Field> collectAllNBTFields() {
        List<Field> all = new ArrayList<>();
        Class<?> clazz = getClass();
        while (clazz != null && BlockEntity.class.isAssignableFrom(clazz)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(NBTField.class)) {
                    field.setAccessible(true);
                    all.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return all;
    }

    private List<Field> initFields(Class<?> fieldClass) {
        List<Field> fields = new ArrayList<>();
        for (Field field : collectAllNBTFields()) {
            if (field.getType().equals(fieldClass)) {
                fields.add(field);
            }
        }
        return fields;
    }

    /** Collects fields whose type implements {@link NBTSerializable} (HeatBuffer, BoilingBuffer, ...). */
    private List<Field> initBufferFields() {
        List<Field> fields = new ArrayList<>();
        for (Field field : collectAllNBTFields()) {
            if (NBTSerializable.class.isAssignableFrom(field.getType())) {
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * Collects fields annotated with @NBTField(syncToClient = true)
     * that can be represented as int for ContainerData sync.
     * Supported types: int, boolean, byte, short.
     */
    private List<Field> initSyncFields() {
        List<Field> fields = new ArrayList<>();
        for (Field field : collectAllNBTFields()) {
            NBTField annotation = field.getAnnotation(NBTField.class);
            if (annotation.syncToClient() && isSyncableType(field.getType())) {
                fields.add(field);
            }
        }
        return fields;
    }

    private static boolean isSyncableType(Class<?> type) {
        return type == int.class || type == boolean.class
                || type == byte.class || type == short.class;
    }

    /** Returns the number of ContainerData slots used for client sync. */
    public int getSyncFieldCount() {
        return syncFields.size();
    }

    /** Returns the ContainerData index for the given field name, or -1 if not found. */
    public int getSyncFieldIndex(String fieldName) {
        for (int i = 0; i < syncFields.size(); i++) {
            if (syncFields.get(i).getName().equals(fieldName)) {
                return i;
            }
        }
        return -1;
    }

    public void readTagData(CompoundTag tag) {
        try {
            for(Field f: directionFields) {
                if (tag.contains(f.getName())) {
                    f.set(this, Direction.byName(tag.getString(f.getName())));
                }
            }
            for(Field f: blockPosFields) {
                if (tag.contains(f.getName())) {
                    f.set(this, BlockPos.of(tag.getLong(f.getName())));
                }
            }
            for(Field f: booleanFields) {
                if (tag.contains(f.getName())) {
                    f.setBoolean(this, tag.getBoolean(f.getName()));
                }
            }
            for(Field f: intFields) {
                if (tag.contains(f.getName())) {
                    f.setInt(this, tag.getInt(f.getName()));
                }
            }
            for(Field f: stringFields) {
                if (tag.contains(f.getName())) {
                    f.set(this, tag.getString(f.getName()));
                }
            }
            for(Field f: doubleFields) {
                if (tag.contains(f.getName())) {
                    f.setDouble(this, tag.getDouble(f.getName()));
                }
            }
            for(Field f: floatFields) {
                if (tag.contains(f.getName())) {
                    f.setFloat(this, tag.getFloat(f.getName()));
                }
            }
            for(Field f: byteFields) {
                if (tag.contains(f.getName())) {
                    f.setByte(this, tag.getByte(f.getName()));
                }
            }
            for(Field f: longFields) {
                if (tag.contains(f.getName())) {
                    f.setLong(this, tag.getLong(f.getName()));
                }
            }
            for(Field f: bufferFields) {
                if (tag.contains(f.getName())) {
                    NBTSerializable buffer = (NBTSerializable) f.get(this);
                    if (buffer != null) {
                        buffer.load(tag.getCompound(f.getName()));
                    }
                }
            }
            for(Field f: intArrayFields) {
                if (tag.contains(f.getName())) {
                    f.set(this, tag.getIntArray(f.getName()));
                }
            }
            for(Field f: stringArrayFields) {
                if (tag.contains(f.getName())) {
                    ListTag tagList = tag.getList(f.getName(), 8);
                    String[] stringArray = new String[tagList.size()];
                    for (int i = 0; i < tagList.size(); i++) {
                        stringArray[i] = tagList.getString(i);
                    }
                    f.set(this, stringArray);
                }
            }
        } catch (IllegalAccessException ignore) { }
    }

    public void saveFullTagData(CompoundTag tag) {
        try {
            for (Field f : blockPosFields) {
                if((f.get(this)) != null) {
                    tag.putLong(f.getName(), ((BlockPos) f.get(this)).asLong());
                }
            }
            for (Field f : directionFields) {
                Direction direction = (Direction) f.get(this);
                if (direction != null) {
                    tag.putString(f.getName(), direction.getName());
                }
            }
            for (Field f : booleanFields) {
                tag.putBoolean(f.getName(), f.getBoolean(this));
            }
            for (Field f : intFields) {
                tag.putInt(f.getName(), f.getInt(this));
            }
            for (Field f : stringFields) {
                String value = (String) f.get(this);
                if (value != null) {
                    tag.putString(f.getName(), value);
                }
            }
            for (Field f : doubleFields) {
                tag.putDouble(f.getName(), f.getDouble(this));
            }
            for (Field f : floatFields) {
                tag.putFloat(f.getName(), f.getFloat(this));
            }
            for (Field f : byteFields) {
                tag.putByte(f.getName(), f.getByte(this));
            }
            for (Field f : longFields) {
                tag.putLong(f.getName(), f.getLong(this));
            }
            for (Field f : bufferFields) {
                NBTSerializable buffer = (NBTSerializable) f.get(this);
                if (buffer != null) {
                    CompoundTag bufferTag = new CompoundTag();
                    buffer.save(bufferTag);
                    tag.put(f.getName(), bufferTag);
                }
            }
            for (Field f : intArrayFields) {
                int[] array = (int[]) f.get(this);
                if (array != null) {
                    tag.putIntArray(f.getName(), array);
                }
            }
            for (Field f : stringArrayFields) {
                String[] stringArray = (String[]) f.get(this);
                if (stringArray != null) {
                    ListTag tagList = new ListTag();
                    for (String string : stringArray) {
                        if (string != null) {
                            tagList.add(StringTag.valueOf(string));
                        }
                    }
                    tag.put(f.getName(), tagList);
                }
            }
        } catch (IllegalAccessException ignore) { }
    }

    public boolean supportRecipes() {
        return ModEntries.get(name).hasRecipes();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveFullTagData(tag);
        tag.put("ContentHandler", contentHandler.serializeNBT(registries));
        if (energyStorage != null) {
            tag.put("Energy", energyStorage.serializeNBT(registries));
        }
        if (supportRecipes()) {
            tag.put("RecipeInfo", recipeInfo.save());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        readTagData(tag);
        if (tag.contains("ContentHandler")) {
            contentHandler.deserializeNBT(registries, tag.getCompound("ContentHandler"));
        }
        if (energyStorage != null && tag.contains("Energy")) {
            energyStorage.deserializeNBT(registries, tag.get("Energy"));
        }
        if (supportRecipes() && tag.contains("RecipeInfo")) {
            recipeInfo.load(tag.getCompound("RecipeInfo"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Called every tick on the server side.
     * Override in subclasses to add server-side logic (processing, energy consumption, etc.).
     */
    public void serverTick() {
        if (name != null && !ModEntries.isEnabled(name)) return;
        contentHandler.tick();
        recipeInfo.tick();
        if(recipeInfo.changed || wasChanged) {
            if(!wasChanged) {
                setChanged();
            }
            assert getLevel() != null;
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), UPDATE_CLIENTS);
            wasChanged = false;
        }
    }

    /**
     * Called every tick on the client side.
     * Override in subclasses to add client-side logic (animations, particles, etc.).
     */
    public void clientTick() {
        // Default: no-op. Subclasses override to add behavior.
    }

    /** Drops all items from the inventory into the world. Call on block break. */
    public void drops() {
        if (level == null || !contentHandler.hasItemCapability()) return;
        ItemCapabilityHandler handler = contentHandler.getItemHandler();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(),
                        worldPosition.getY(), worldPosition.getZ(), stack);
            }
        }
    }

    public ProcessorRecipeInput inputs() {
        List<ItemStack> items = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();

        if (contentHandler.hasItemCapability() && name != null) {
            ModEntry entry = ModEntries.get(name);
            int inputSlots = (entry != null && entry.itemCap() != null) ? entry.itemCap().inputSlots : 0;
            ItemCapabilityHandler handler = contentHandler.getItemHandler();
            for (int i = 0; i < inputSlots; i++) {
                items.add(handler.getStackInSlot(i));
            }
        }

        if (contentHandler.hasFluidCapability() && name != null) {
            ModEntry entry = ModEntries.get(name);
            int inputTanks = (entry != null && entry.fluidCap() != null) ? entry.fluidCap().inputTanks.size() : 0;
            FluidCapabilityHandler handler = contentHandler.getFluidHandler();
            for (int i = 0; i < inputTanks; i++) {
                fluids.add(handler.getFluidInTank(i));
            }
        }

        return new ProcessorRecipeInput(items, fluids);
    }

    public HeatBuffer heatBuffer() {
        return null;
    }

    public BoilingBuffer boilingBuffer() {
        return null;
    }
}

