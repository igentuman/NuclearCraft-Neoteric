package igentuman.nc.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;

public class ClientTools {

    private static void putVertex(BakedQuadBuilder builder, Vec3 pos, Vector4f vector,
                                  float u, float v, TextureAtlasSprite sprite) {

        VertexFormat format = DefaultVertexFormat.BLOCK;
        float[] colour = {1, 1, 1, 1};
        for(int e = 0; e < format.getElements().size(); e++)
            switch(format.getElements().get(e).getUsage())
            {
                case POSITION:
                    builder.put(e, (float)pos.x, (float)pos.y, (float)pos.z);
                    break;
                case COLOR:
                    float d = 1;//LightUtil.diffuseLight(faceNormal.x, faceNormal.y, faceNormal.z);
                    builder.put(e, d*colour[0], d*colour[1], d*colour[2], 1*colour[3]*1);
                    break;
                case UV:
                    if(format.getElements().get(e).getType()== VertexFormatElement.Type.FLOAT)
                    {
                        builder.put(e, sprite.getU(u), sprite.getV(v));
                    }
                    else
                        //Lightmap UVs (0, 0 is "automatic")
                        builder.put(e, 0, 0);
                    break;
                case NORMAL:
                    builder.put(e, vector.x(), vector.y(), vector.z());
                    break;
                default:
                    builder.put(e);
            }
    }

    public static BakedQuad createQuad(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, Transformation rotation, TextureAtlasSprite sprite) {
        Vec3 normal = new Vec3(v3.x(), v3.y(), v3.z());
        normal.normalize();
        Vector3f temp = v1.copy();
        temp.normalize();
        temp.sub(v2);
        normal.normalize();

        int tw = sprite.getWidth();
        int th = sprite.getHeight();

        rotation = rotation.blockCenterToCorner();
        //rotation.transformNormal(normal);

        Vector4f vv1 = new Vector4f((Vector3f) v1); rotation.transformPosition(vv1);
        Vector4f vv2 = new Vector4f((Vector3f) v2); rotation.transformPosition(vv2);
        Vector4f vv3 = new Vector4f((Vector3f) v3); rotation.transformPosition(vv3);
        Vector4f vv4 = new Vector4f((Vector3f) v4); rotation.transformPosition(vv4);

        BakedQuad[] quad = new BakedQuad[1];
        var builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(Direction.getNearest(normal.x(), normal.y(), normal.z()));
        putVertex(builder, normal, vv1, 0, 0, sprite);
        putVertex(builder, normal, vv2, 0, th, sprite);
        putVertex(builder, normal, vv3, tw, th, sprite);
        putVertex(builder, normal, vv4, tw, 0, sprite);
        return quad[0];
    }


    public static Vector3f v(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }
}