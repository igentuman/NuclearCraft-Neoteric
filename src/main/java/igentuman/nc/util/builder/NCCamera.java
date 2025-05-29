package igentuman.nc.util.builder;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

public class NCCamera extends Camera {
    protected void rotate(float pYRot, float pXRot) {
        this.setRotation(pYRot, pXRot);
    }

    protected void setPosition(double pX, double pY, double pZ) {
        this.setPosition(new Vec3(pX, pY, pZ));
    }
}
