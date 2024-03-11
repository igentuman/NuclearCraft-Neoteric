package igentuman.nc.block.entity;

import igentuman.nc.block.ISizeToggable;
import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.util.NCBlockPos;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.block.BlockState;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class NuclearCraftBE extends TileEntity {
    protected String name;
    protected NCBlockPos bePos;
    protected boolean changed;
    //protected SoundInstance currentSound;
    protected int playSoundCooldown = 0;

    public HashMap<Integer, ISizeToggable.SideMode> sideConfig = new HashMap<>();

    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString();
    }


    public NuclearCraftBE(TileEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType);
        name = getName(pBlockState);
    }

    public NuclearCraftBE(TileEntityType<?> pType) {
        super(pType);
    }

    protected void trackChanges(boolean was, boolean now)
    {
        changed = was != now || changed;
    }

    protected void trackChanges(boolean was)
    {
        changed = was || changed;
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

    public void saveTagData(CompoundNBT tag) {
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
                ListNBT tagList = new ListNBT();
                for (String string : stringArray) {
                    tagList.add(StringNBT.valueOf(string));
                }
                tag.put(f.getName(), tagList);
            }
        } catch (IllegalAccessException ignore) { }
    }

    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getBlockPos(), 0, this.getUpdateTag());
    }
    @Override
    public void setRemoved() {
        super.setRemoved();
        if(Objects.requireNonNull(getLevel()).isClientSide()) {
            stopSound();
        }
    }

    protected void stopSound() {
        //SoundHandler.stopTileSound(getBlockPos());
        //currentSound = null;
        playSoundCooldown = 0;
    }

    public void readTagData(CompoundNBT tag) {
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
                ListNBT tagList = tag.getList(f.getName(), 8);
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
            if (!field.isAnnotationPresent(NBTField.class)) {
                continue;
            }
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
