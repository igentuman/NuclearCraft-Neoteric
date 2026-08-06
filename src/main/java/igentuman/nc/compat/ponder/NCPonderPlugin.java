package igentuman.nc.compat.ponder;

import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.registration.*;
import net.minecraft.resources.ResourceLocation;

import static igentuman.nc.NuclearCraft.MODID;

public class NCPonderPlugin  implements PonderPlugin {
    @Override
    public String getModId() {
        return MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderPlugin.super.registerTags(helper);
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        PonderPlugin.super.registerSharedText(helper);
    }

    @Override
    public void onPonderLevelRestore(PonderLevel ponderLevel) {
        PonderPlugin.super.onPonderLevelRestore(ponderLevel);
    }

    @Override
    public void indexExclusions(IndexExclusionHelper helper) {
        PonderPlugin.super.indexExclusions(helper);
    }
}
