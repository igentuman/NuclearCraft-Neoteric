package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

public class TargetChamberPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        CubeChamberPonderScenes.create(scene, util, PonderScenes.TARGET_CHAMBER,
                "Target Chamber", "target_chamber_controller");
    }
}
