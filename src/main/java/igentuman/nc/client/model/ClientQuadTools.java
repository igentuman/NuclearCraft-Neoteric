package igentuman.nc.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

public final class ClientQuadTools {

    private ClientQuadTools() {}

    public static Vec3 v(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    public static BakedQuad createQuad(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, TextureAtlasSprite sprite) {
        Vec3 normal = v3.subtract(v2).cross(v1.subtract(v2)).normalize();

        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        consumer.setSprite(sprite);
        consumer.setDirection(Direction.getNearest(normal.x, normal.y, normal.z));
        consumer.setTintIndex(-1);

        putVertex(consumer, normal, v1, 0f, 0f, sprite);
        putVertex(consumer, normal, v2, 0f, 1f, sprite);
        putVertex(consumer, normal, v3, 1f, 1f, sprite);
        putVertex(consumer, normal, v4, 1f, 0f, sprite);

        return consumer.bakeQuad();
    }

    private static void putVertex(QuadBakingVertexConsumer consumer, Vec3 normal, Vec3 pos,
                                  float u, float v, TextureAtlasSprite sprite) {
        consumer.addVertex((float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(255, 255, 255, 255)
                .setUv(sprite.getU(u), sprite.getV(v))
                .setUv2(0, 0)
                .setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    }
}
