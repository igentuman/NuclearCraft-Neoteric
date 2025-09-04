package igentuman.nc.block.entity;

import igentuman.api.nc.SideModeToggleable;
import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.handler.CatalystHandler;
import igentuman.nc.handler.UpgradesHandler;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.RecipeInfo;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.util.BlockPosInstance;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

import static igentuman.nc.compat.gregtech.GTUtils.*;
import static igentuman.nc.handler.config.CommonConfig.GTCEUCompatibilityConfig.GTCEUCompatibility.GTCEU_AND_FE;
import static igentuman.nc.handler.config.CommonConfig.GTCEUCompatibilityConfig.GTCEUCompatibility.ONLY_GTCEU;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.ModUtil.isMekanismLoaded;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;

public class NuclearCraftBE extends BlockEntity {

    protected final String name;
    protected BlockPosInstance bePos;
    protected boolean changed;
    protected SoundInstance currentSound;
    protected int playSoundCooldown = 0;
    protected UUID playerUID = null;
    public HashMap<Integer, SideModeToggleable.SideMode> sideConfig = new HashMap<>();
    public SidedContentHandler contentHandler;
    protected CustomEnergyStorage energyStorage;
    public final RecipeInfo recipeInfo;
    public UpgradesHandler upgradesHandler;
    public CatalystHandler catalystHandler;
    protected NcRecipe recipe;
    protected boolean saveSideMapFlag = true;
    public boolean wasUpdated = true;
    public HashMap<String, NcRecipe> cachedRecipes = new HashMap<>();
    protected LazyOptional<IEnergyStorage> energy;

    public NuclearCraftBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        name = getName(pBlockState);
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
        recipeInfo = new RecipeInfo();
    }

    protected void sendOutPower() {
        for (Direction direction : Direction.values()) {
            transferEnergyToSide(direction);
        }
    }

    protected void pullEnergyFromSide(Direction direction) {
        BlockEntity be = level.getExistingBlockEntity(worldPosition.relative(direction));
        if (be == null) {
            return;
        }
        if(isGtLoaded() && isOnlyGTCEUCapEnabled()) {
            return;
        }
        be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).map(handler -> {
                    if (handler.canExtract()) {
                        int canReceive = energyStorage().receiveEnergy(handler.getEnergyStored(), true);
                        if (canReceive > 0) {
                            int received = energyStorage().receiveEnergy(canReceive, false);
                            handler.extractEnergy(received, false);
                        }
                        setChanged();
                        return true;
                    }
                    return true;
                }
        );
    }

    protected void transferEnergyToSide(Direction direction) {
        if (energyStorage().getEnergyStored() <= 0) {
            return; // No energy to transfer
        }
        int wasEnergy = energyStorage().getEnergyStored();
        BlockEntity be = level.getExistingBlockEntity(worldPosition.relative(direction));
        if (be == null) {
            return;
        }
        if((isGtLoaded() && isGTEUCapEnabled())) {
            transferEU(this, be, energyStorage(), direction);
        }
        if(isGtLoaded() && isOnlyGTCEUCapEnabled()) {
            return;
        }
        int extracted = wasEnergy - energyStorage().getEnergyStored();
        if(extracted >= energyStorage().getMaxExtract()) {
            return;
        }
        int canExtract = energyStorage().getMaxExtract() - extracted;
        be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).map(handler -> {
                    if (handler.canReceive()) {
                        int received = handler.receiveEnergy(Math.min(canExtract, energyStorage().getEnergyStored()), false);
                        energyStorage().consumeEnergy(received);
                        setChanged();
                        return energyStorage().getEnergyStored() > 0;
                    } else {
                        return true;
                    }
                }
        );
    }

    //for the moment input and output energy tiers are the same
    public long getInputEnergyTier() {
        return 2L; // Default to 2, can be overridden in subclasses
    }

    public long getOutputEnergyTier() {
        return 2L; // Default to 2, can be overridden in subclasses
    }

    public SidedContentHandler contentHandler() {
        return contentHandler;
    }

    public UpgradesHandler upgradesHandler() {
        return upgradesHandler;
    }

    public CatalystHandler catalystHandler() {
        return catalystHandler;
    }

    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }

    public BlockEntity blockEntity(BlockPos pos) {
        BlockEntity blockEntity = level.getExistingBlockEntity(pos);
        if(blockEntity == null) {
            blockEntity = level.getBlockEntity(pos);
        }
        return blockEntity;
    }

    public RecipeInfo recipeInfo() {
        return recipeInfo;
    }

    public NcRecipe getRecipe() {
        NcRecipe cachedRecipe = getCachedRecipe();
        if(cachedRecipe != null) return cachedRecipe;
        if(!NcRecipeType.ALL_RECIPES.containsKey(getName())) return null;
        for(NcRecipe recipe: NcRecipeType.getAllRecipesFor(getName(), getLevel())) {
            if(recipe.test(contentHandler())) {
                addToCache(recipe);
                return recipe;
            }
        }
        return null;
    }

    public NcRecipe getCachedRecipe() {
        String key = contentHandler().getCacheKey();
        if(cachedRecipes.containsKey(key)) {
            if(cachedRecipes.get(key).test(contentHandler())) {
                return cachedRecipes.get(key);
            }
        }
        return null;
    }

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    protected void addToCache(NcRecipe recipe) {
        String key = contentHandler().getCacheKey();
        if(cachedRecipes.containsKey(key)) {
            cachedRecipes.replace(key, recipe);
        } else {
            cachedRecipes.put(key, recipe);
        }
    }

    protected void playSound(RegistryObject<SoundEvent> sound, float volume) {
        if(isRemoved() || (currentSound != null && !currentSound.getLocation().equals(sound.get().getLocation()))) {
            SoundHandler.stopTileSound(getBlockPos());
            currentSound = null;
        }
        if((currentSound == null || !Minecraft.getInstance().getSoundManager().isActive(currentSound))) {
            if(currentSound != null && currentSound.getLocation().equals(sound.get().getLocation())) {
                if (!Minecraft.getInstance().getSoundManager().isActive(currentSound) && SoundHandler.isClientPlayerInRange(currentSound)) {
                    currentSound = SoundHandler.startTileSound(sound.get(), SoundSource.BLOCKS, volume, level.getRandom(), getBlockPos());
                }
                return;
            }

            playSoundCooldown = 20;
            currentSound = SoundHandler.startTileSound(sound.get(), SoundSource.BLOCKS, volume, level.getRandom(), getBlockPos());
        }
    }

    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString();
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
    private final List<Field> directionFields;

    public void saveTagData(CompoundTag tag) {
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

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(Objects.requireNonNull(getLevel()).isClientSide()) {
            stopSound();
        }
    }

    protected void stopSound() {
        if (currentSound == null) return;
        SoundHandler.stopTileSound(getBlockPos());
        currentSound = null;
        playSoundCooldown = 0;
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

    public FluidTank getFluidTank(int i) {
        return contentHandler().fluidHandler.tanks.get(i);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        wasUpdated = true;
    }

    private void updateRecipeAfterLoad() {
        if(recipe == null && recipeInfo() != null && recipeInfo().recipe() != null) {
            recipe = recipeInfo().recipe();
        }
    }

    public static boolean isGTEUCapEnabled() {
        return GTCEU_CONFIG.COMPATIBILITY.get() == ONLY_GTCEU
                || GTCEU_CONFIG.COMPATIBILITY.get() == GTCEU_AND_FE;
    }




    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(isGtLoaded()) {
            if (cap == com.gregtechceu.gtceu.api.capability.forge.GTCapability.CAPABILITY_ENERGY_CONTAINER && energyStorage() != null) {
                if (isGTEUCapEnabled()) {
                    if (side != null && sideConfig.get(side.ordinal()) != SideModeToggleable.SideMode.DISABLED)
                        return getGTEnergy(this, side).cast();
                } else {
                    return LazyOptional.empty();
                }
            }
        }
        if (cap == ENERGY && energyStorage() != null) {
            if(GTCEU_CONFIG.COMPATIBILITY.get() != ONLY_GTCEU) {
                return getEnergy().cast();
            } else {
                return LazyOptional.empty();
            }
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER && contentHandler() != null) {
            return contentHandler().getItemCapability(side);
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER && contentHandler() != null) {
            return contentHandler().getFluidCapability(side);
        }

        if(isMekanismLoaded() && contentHandler() != null) {
            if(cap == mekanism.common.capabilities.Capabilities.GAS_HANDLER) {
                if(contentHandler().hasFluidCapability(side)) {
                    return LazyOptional.of(() -> contentHandler().gasConverter(side));
                }
                return LazyOptional.empty();
            }
            if(cap == mekanism.common.capabilities.Capabilities.SLURRY_HANDLER) {
                if(contentHandler().hasFluidCapability(side)) {
                    return LazyOptional.of(() -> contentHandler().getSlurryConverter(side));
                }
                return LazyOptional.empty();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            readTagData(infoTag);
            if (infoTag.contains("recipeInfo") && recipeInfo() != null) {
                recipeInfo().deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            if(infoTag.contains("upgrades") && upgradesHandler() != null) {
                upgradesHandler().deserializeNBT((CompoundTag) (infoTag).get("upgrades"));
            }
            if(infoTag.contains("catalyst") && catalystHandler() != null) {
                catalystHandler().deserializeNBT((CompoundTag) (infoTag).get("catalyst"));
            }
        }
        if (tag.contains("Energy") && energyStorage() != null) {
            energyStorage().deserializeNBT(tag.get("Energy"));
        }
        if (tag.contains("Content") && contentHandler() != null) {
            contentHandler().deserializeNBT(tag.getCompound("Content"));
        }
        if (tag.contains("energy")) {
            energyStorage().setEnergy(tag.getInt("energy"));
        }
        super.load(tag);
        updateRecipeAfterLoad();
    }



    public void loadClientData(CompoundTag tag) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            readTagData(infoTag);
            if (infoTag.contains("recipeInfo") && recipeInfo() != null) {
                recipeInfo().deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            if (tag.contains("Energy") && energyStorage() != null) {
                energyStorage().deserializeNBT(tag.get("Energy"));
            }
            if(infoTag.contains("energy") && energyStorage() != null) {
                energyStorage().setEnergy(infoTag.getInt("energy"));
            }
            if(infoTag.contains("upgrades") && upgradesHandler() != null) {
                upgradesHandler().deserializeNBT((CompoundTag) (infoTag).get("upgrades"));
            }
            if(infoTag.contains("catalyst") && catalystHandler() != null) {
                catalystHandler().deserializeNBT((CompoundTag) (infoTag).get("catalyst"));
            }
        }
        if (tag.contains("Content") && contentHandler() != null) {
            contentHandler().deserializeNBT(tag.getCompound("Content"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        if (contentHandler() != null) {
            contentHandler().saveSideMap();
        }
        if(!tag.contains("Content") && contentHandler() != null) {
            tag.put("Content", contentHandler().serializeNBT());
        }
        if(!tag.contains("Energy") && energyStorage() != null && energyStorage().wasUpdated) {
            tag.put("Energy", energyStorage().serializeNBT());
        }
        CompoundTag infoTag = new CompoundTag();
        if (upgradesHandler() != null) {
            infoTag.put("upgrades", upgradesHandler().serializeNBT());
        }
        if (catalystHandler() != null) {
            infoTag.put("catalyst", catalystHandler().serializeNBT());
        }
        if (recipeInfo() != null) {
            infoTag.put("recipeInfo", recipeInfo().serializeNBT());
        }
        if(playerUID != null) {
            tag.putUUID("playerUID", playerUID);
        }
        saveTagData(infoTag);
        tag.put("Info", infoTag);
    }

    protected void saveClientData(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        if(upgradesHandler() != null && upgradesHandler().wasUpdated) {
            infoTag.put("upgrades", upgradesHandler().serializeNBT());
            upgradesHandler().wasUpdated = false;
        }
        if(catalystHandler() != null && catalystHandler().wasUpdated) {
            infoTag.put("catalyst", catalystHandler().serializeNBT());
            catalystHandler().wasUpdated = false;
        }
        if (recipeInfo() != null) {
            infoTag.put("recipeInfo", recipeInfo().serializeNBT());
        }
        if (contentHandler() != null) {
            if(saveSideMapFlag) {
                contentHandler().saveSideMap();
                saveSideMapFlag = false;
            }
            tag.put("Content", contentHandler().serializeNBT());
        }
        if(!tag.contains("Energy") && energyStorage() != null && energyStorage().wasUpdated) {
            tag.put("Energy", energyStorage().serializeNBT());
        }
        if (energyStorage() != null) {
            infoTag.putInt("energy", energyStorage().getEnergyStored());
        }
        saveTagData(infoTag);
        tag.put("Info", infoTag);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveClientData(tag);
        if(playerUID != null) {
            tag.putUUID("playerUID", playerUID);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            loadClientData(tag);
            if (tag.contains("playerUID")) {
                playerUID = tag.getUUID("playerUID");
            }
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    public void setPlayer(ServerPlayer player) {
        playerUID = player.getUUID();
    }

    public void tickClient() {
    }

    public void tickServer() {
    }

    public void handleOverVoltage() {
    }
}
