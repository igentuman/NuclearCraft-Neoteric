package igentuman.nc.setup.registration;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;

public class CreativeTabs {

    public static final CreativeModeTab FISSION_REACTOR = CreativeModeTab.builder()
            .icon(() -> new ItemStack(FISSION_BLOCKS.get("fission_reactor_controller").get()))
            .title(Component.translatable("itemGroup.nuclearcraft_fission_reactor"))
            .build();

    public static final CreativeModeTab FUSION_REACTOR = CreativeModeTab.builder()
            .icon(() -> new ItemStack(FUSION_BLOCKS.get("fusion_core").get()))
            .title(Component.translatable("itemGroup.nuclearcraft_fusion_reactor"))
            .build();


    public static final CreativeModeTab NC_BLOCKS = CreativeModeTab.builder()
            .icon(() -> new ItemStack(FISSION_BLOCKS.get("fission_reactor_casing").get()))
            .title(Component.translatable("itemGroup.nuclearcraft_blocks"))
            .build();


    public static final CreativeModeTab NC_ITEMS = CreativeModeTab.builder()
            .icon(() -> new ItemStack(NC_PARTS.get("actuator").get()))
            .title(Component.translatable("itemGroup.nuclearcraft_items"))
            .build();
}
