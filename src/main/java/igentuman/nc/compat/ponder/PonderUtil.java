package igentuman.nc.compat.ponder;

import net.createmod.catnip.api.client.platform.ModClientHooksHelper;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.impl.client.gui.PonderSceneRenderer;
import net.createmod.ponder.impl.client.gui.PonderSceneRenderState;

/** Client integration with Ponder. */
public class PonderUtil {

    public static void registerRendererEarly() {
        // NeoForge gathers GUI renderers before Ponder's FMLClientSetupEvent listener runs.
        // Ponder's later registration replaces the same entry in Catnip's renderer map.
        ModClientHooksHelper.INSTANCE.registerPictureInPictureRenderer(
                PonderSceneRenderState.class, PonderSceneRenderer::new);
    }

    public static void initPlugin() {
        PonderIndex.addPlugin(new NCPonderPlugin());
    }
}
