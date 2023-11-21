package igentuman.nc.block.entity;

import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.util.NCBlockPos;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class NuclearCraftBE extends BlockEntity {
    protected String name;
    protected NCBlockPos bePos;

    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString();
    }


    public NuclearCraftBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        name = getName(pBlockState);
    }
    private boolean initFlag = false;
    private List<Field> booleanFields       = new ArrayList<>();
    private List<Field> intFields           = new ArrayList<>();
    private List<Field> intArrayFields      = new ArrayList<>();
    private List<Field> doubleFields        = new ArrayList<>();
    private List<Field> stringFields        = new ArrayList<>();
    private List<Field> stringArrayFields   = new ArrayList<>();
    private List<Field> floatFields         = new ArrayList<>();
    private List<Field> byteFields          = new ArrayList<>();
    private List<Field> longFields          = new ArrayList<>();
    private List<Field> blockPosFields      = new ArrayList<>();

    public void saveTagData(CompoundTag tag) {
        initFields();
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

    public void readTagData(CompoundTag tag) {
        initFields();
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

    private void initFields() {
        if(initFlag) return;
        for (Field field : getClass().getFields()) {
            if (field.isAnnotationPresent(NBTField.class)) {
                if(field.getType().equals(BlockPos.class)) {
                    blockPosFields.add(field);
                    continue;
                }
                if(field.getType().equals(int.class)) {
                    intFields.add(field);
                    continue;
                }
                if(field.getType().equals(String.class)) {
                    stringFields.add(field);
                    continue;
                }
                if(field.getType().equals(boolean.class)) {
                    booleanFields.add(field);
                    continue;
                }
                if(field.getType().equals(byte.class)) {
                    byteFields.add(field);
                    continue;
                }
                if(field.getType().equals(double.class)) {
                    doubleFields.add(field);
                    continue;
                }
                if(field.getType().equals(float.class)) {
                    floatFields.add(field);
                    continue;
                }
                if(field.getType().equals(long.class)) {
                    longFields.add(field);
                    continue;
                }
                if(field.getType().equals(int[].class)) {
                    intArrayFields.add(field);
                    continue;
                }
                if(field.getType().equals(String[].class)) {
                    stringArrayFields.add(field);
                }
            }
        }
        initFlag = true;
    }

    public ItemCapabilityHandler getItemInventory() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void handleSliderUpdate(int buttonId, int ratio) {

    }
}
