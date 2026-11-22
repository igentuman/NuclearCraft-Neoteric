package igentuman.nc.compat.ponder;

import net.createmod.ponder.api.client.registration.PonderPlugin;
import net.createmod.ponder.api.client.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.Identifier;

import static igentuman.nc.NuclearCraft.MODID;

/** Registers NuclearCraft's multiblock tutorials with Ponder. */
public class NCPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<Identifier> helper) {
        PonderScenes.register(helper);
    }
}
