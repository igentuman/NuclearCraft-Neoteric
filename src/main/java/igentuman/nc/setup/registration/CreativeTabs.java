package igentuman.nc.setup.registration;

import igentuman.nc.content.materials.Materials;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.antlr.v4.runtime.misc.NotNull;;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;

public class CreativeTabs {

    public static final ItemGroup TURBINE = new ItemGroup(MODID+"_turbine") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(TURBINE_BLOCKS.get("turbine_controller").get());
        }
    };

    public static final ItemGroup FISSION_REACTOR = new ItemGroup(MODID+"_fission_reactor") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(FISSION_BLOCKS.get("fission_reactor_controller").get());
        }
    };

    public static final ItemGroup FUSION_REACTOR = new ItemGroup(MODID+"_fusion_reactor") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(FUSION_BLOCKS.get("fusion_core").get());
        }
    };


    public static final ItemGroup NC_BLOCKS = new ItemGroup(MODID+"_blocks") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(NCBlocks.NC_BLOCKS.get(Materials.uranium).get());
        }
    };


    public static final ItemGroup NC_ITEMS = new ItemGroup(MODID+"_items") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(ALL_NC_ITEMS.get("motor").get());
        }
    };
}
