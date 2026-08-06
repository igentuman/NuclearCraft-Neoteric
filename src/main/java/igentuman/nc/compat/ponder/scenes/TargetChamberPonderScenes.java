package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

public class TargetChamberPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.TARGET_CHAMBER.getPath(), "Creating a Target Chamber");
    }
}
