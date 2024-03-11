package igentuman.nc.util;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK;

public class ClientTools {

    private static void putVertex(BakedQuadBuilder builder, Vector3d pos, Vector4f vector,
                                  float u, float v, TextureAtlasSprite sprite) {

        VertexFormat format = BLOCK;
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

    public static BakedQuad createQuad(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, TransformationMatrix rotation, TextureAtlasSprite sprite) {
        Vector3d normal = new Vector3d(v3.x(), v3.y(), v3.z());
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
        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
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