package igentuman.nc.compat.ponder.scenes;

import igentuman.nc.compat.ponder.PonderScenes;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

public class CollisionChamberPonderScenes {

    public static void create(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(PonderScenes.COLLISION_CHAMBER.getPath(), "Creating a Collision Chamber");
    }
}
