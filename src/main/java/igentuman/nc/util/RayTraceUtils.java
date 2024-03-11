package igentuman.nc.util;


import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class RayTraceUtils {

    public static RayTraceResult rayTraceSimple(World world, LivingEntity living, double blockReachDistance, float partialTicks) {
        Vector3d vec3d = living.getEyePosition(partialTicks);
        Vector3d vec3d1 = living.getViewVector(partialTicks);
        Vector3d vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
        return world.clip(new RayTraceContext(vec3d, vec3d2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, living));
    }

}