package igentuman.nc.block.entity;

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
    public NuclearCraftBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public void saveTagData(CompoundTag tag)
    {
        List<Field> networkedFields = getNetworkedFields();
        for (Field field : networkedFields) {
            String fieldName = field.getName();
            Object fieldValue = null;
            try {
                fieldValue = field.get(this);
            } catch (IllegalAccessException ignore) { }
            if (field.getType() == int.class) {
                tag.putInt(fieldName, (int) fieldValue);
            } else if (field.getType() == double.class) {
                tag.putDouble(fieldName, (double) fieldValue);
            } else if (field.getType() == boolean.class) {
                tag.putBoolean(fieldName, (boolean) fieldValue);
            }  else if (field.getType() == String.class) {
                tag.putString(fieldName, (String) fieldValue);
            } else if (field.getType() == byte.class) {
                tag.putByte(fieldName, (byte) fieldValue);
            } else if (field.getType() == int[].class) {
                tag.putIntArray(fieldName, (int[]) fieldValue);
            } else if (field.getType() == String[].class) {
                String[] stringArray = (String[]) fieldValue;
                ListTag tagList = new ListTag();
                for (String string : stringArray) {
                    tagList.add(StringTag.valueOf(string));
                }
                tag.put(fieldName, tagList);
            }
        }
    }

    public void readTagData(CompoundTag tag)
    {
        List<Field> networkedFields = getNetworkedFields();
        for (Field field : networkedFields) {
            String fieldName = field.getName();
            try {
                if (field.getType() == int.class) {
                    int fieldValue = tag.getInt(fieldName);
                    field.set(this, fieldValue);
                } else if (field.getType() == double.class) {
                    double fieldValue = tag.getDouble(fieldName);
                    field.set(this, fieldValue);
                } else if (field.getType() == boolean.class) {
                    boolean fieldValue = tag.getBoolean(fieldName);
                    field.set(this, fieldValue);
                } else if (field.getType() == String.class) {
                    String fieldValue = tag.getString(fieldName);
                    field.set(this, fieldValue);
                } else if (field.getType() == byte.class) {
                    byte fieldValue = tag.getByte(fieldName);
                    field.set(this, fieldValue);
                } else if (field.getType() == int[].class) {
                    int[] fieldValue = tag.getIntArray(fieldName);
                    field.set(this, fieldValue);
                } else if (field.getType() == String[].class) {
                    ListTag tagList = tag.getList(fieldName, 8);
                    String[] stringArray = new String[tagList.size()];
                    for (int i = 0; i < tagList.size(); i++) {
                        stringArray[i] = tagList.getString(i);
                    }
                    field.set(this, stringArray);
                }
            } catch (IllegalAccessException ignore) { }
        }
    }

    private List<Field> getNetworkedFields() {
        List<Field> networkedFields = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(NBTField.class)) {
                networkedFields.add(field);
            }
        }
        return networkedFields;
    }
}
