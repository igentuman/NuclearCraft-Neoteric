package igentuman.nc.setup.registration;

import igentuman.nc.content.materials.Materials;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.antlr.v4.runtime.misc.NotNull;;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;

public class CreativeTabs {

    public static final CreativeModeTab TURBINE = new CreativeModeTab(MODID+"_turbine") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(TURBINE_BLOCKS.get("turbine_controller").get());
        }
    };

    public static final CreativeModeTab FISSION_REACTOR = new CreativeModeTab(MODID+"_fission_reactor") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(FISSION_BLOCKS.get("fission_reactor_controller").get());
        }
    };

    public static final CreativeModeTab FUSION_REACTOR = new CreativeModeTab(MODID+"_fusion_reactor") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(FUSION_BLOCKS.get("fusion_core").get());
        }
    };


    public static final CreativeModeTab NC_BLOCKS = new CreativeModeTab(MODID+"_blocks") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(NCBlocks.NC_BLOCKS.get(Materials.uranium).get());
        }
    };


    public static final CreativeModeTab NC_ITEMS = new CreativeModeTab(MODID+"_items") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(ALL_NC_ITEMS.get("motor").get());
        }
    };
}
