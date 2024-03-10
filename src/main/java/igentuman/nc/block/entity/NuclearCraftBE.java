package igentuman.nc.block.entity;

import igentuman.nc.block.ISizeToggable;
import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.util.NCBlockPos;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class NuclearCraftBE extends BlockEntity {
    protected final String name;
    protected NCBlockPos bePos;
    protected boolean changed;
    protected SoundInstance currentSound;
    protected int playSoundCooldown = 0;

    public HashMap<Integer, ISizeToggable.SideMode> sideConfig = new HashMap<>();

    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString();
    }


    public NuclearCraftBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        name = getName(pBlockState);
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
    }

    protected void trackChanges(boolean was, boolean now)
    {
        changed = was != now || changed;
    }

    protected void trackChanges(boolean was)
    {
        changed = was || changed;
    }

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

    public void saveTagData(CompoundTag tag) {
        try {
            for (Field f : blockPosFields) {
                if((f.get(this)) != null) {
                    tag.putLong(f.getName(), ((BlockPos) f.get(this)).asLong());
                }
            }
            for (Field f : booleanFields) {
                tag.putBoolean(f.getName(), f.getBoolean(this));
            }
            for (Field f : intFields) {
                tag.putInt(f.getName(), f.getInt(this));
            }
            for (Field f : stringFields) {
                tag.putString(f.getName(), (String) f.get(this));
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
            for (Field f : intArrayFields) {
                tag.putIntArray(f.getName(), (int[]) f.get(this));
            }
            for (Field f : stringArrayFields) {
                String[] stringArray = (String[]) f.get(this);
                ListTag tagList = new ListTag();
                for (String string : stringArray) {
                    tagList.add(StringTag.valueOf(string));
                }
                tag.put(f.getName(), tagList);
            }
        } catch (IllegalAccessException ignore) { }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(Objects.requireNonNull(getLevel()).isClientSide()) {
            stopSound();
        }
    }

    protected void stopSound() {
        SoundHandler.stopTileSound(getBlockPos());
        currentSound = null;
        playSoundCooldown = 0;
    }

    public void readTagData(CompoundTag tag) {
        try {
            for(Field f: blockPosFields) {
                f.set(this, BlockPos.of(tag.getLong(f.getName())));
            }
            for(Field f: booleanFields) {
                f.setBoolean(this, tag.getBoolean(f.getName()));
            }
            for(Field f: intFields) {
                f.setInt(this, tag.getInt(f.getName()));
            }
            for(Field f: stringFields) {
                f.set(this, tag.getString(f.getName()));
            }
            for(Field f: doubleFields) {
                f.setDouble(this, tag.getDouble(f.getName()));
            }
            for(Field f: floatFields) {
                f.setFloat(this, tag.getFloat(f.getName()));
            }
            for(Field f: byteFields) {
                f.setByte(this, tag.getByte(f.getName()));
            }
            for(Field f: longFields) {
                f.setLong(this, tag.getLong(f.getName()));
            }
            for(Field f: intArrayFields) {
                f.set(this, tag.getIntArray(f.getName()));
            }
            for(Field f: intArrayFields) {
                ListTag tagList = tag.getList(f.getName(), 8);
                String[] stringArray = new String[tagList.size()];
                for (int i = 0; i < tagList.size(); i++) {
                    stringArray[i] = tagList.getString(i);
                }
                f.set(this, stringArray);
            }
        } catch (IllegalAccessException ignore) { }
    }

    private List<Field> initFields(Class<?> fieldClass) {
        List<Field> fields = new ArrayList<>();
        for (Field field : getClass().getFields()) {
            if (!field.isAnnotationPresent(NBTField.class)) {
                continue;
            }
            if(field.getType().equals(fieldClass)) {
                fields.add(field);
            }
        }
        return fields;
    }

    public ItemCapabilityHandler getItemInventory() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void handleSliderUpdate(int buttonId, int ratio) {
    }

    public void loadClientData(CompoundTag tag) {
    }

    protected void saveClientData(CompoundTag tag) {
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);
    }
}
