package igentuman.nc.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

import static igentuman.nc.NuclearCraft.MODID;

public class TextureUtil {

    public static int rgbaToInt(int[] rgba) {
        int r = rgba[0];
        int g = rgba[1];
        int b = rgba[2];
        int a = rgba[3];

        int color = (a << 24) | (r << 16) | (g << 8) | b;

        return color;
    }

    public static int rgbaToIntHex(int[] rgba) {
        int color = rgbaToInt(rgba);
        return color;
    }

    public static int getAverageColor(String textureLocation) {
        Minecraft mc = Minecraft.getInstance();
        ResourceLocation resourceLocation = new ResourceLocation(MODID, textureLocation);
        try {
            int redSum;
            int greenSum;
            int blueSum;
            int pixelCount;
            String path = "assets/"+resourceLocation.getNamespace()+"/"+resourceLocation.getPath();
            InputStream texStream = resourceLocation.getClass().getClassLoader().getResourceAsStream(path);
            try (NativeImage nativeImage = NativeImage.read(texStream)) {
                int textureWidth = nativeImage.getWidth();
                int textureHeight = nativeImage.getHeight();

                // Calculate the average color
                redSum = 0;
                greenSum = 0;
                blueSum = 0;
                pixelCount = 0;
                for (int x = (textureWidth/2)-4; x < (textureWidth/2)+4; x++) {
                    for (int y = (textureHeight/2)-4; y < (textureHeight/2)+4; y++) {
                        Color pixel = new Color(nativeImage.getPixelRGBA(x, y));
                        redSum += Math.min(254 ,pixel.getRed()+20);//adding some shift to match with item color
                        greenSum += pixel.getGreen();
                        blueSum += Math.max(0, pixel.getBlue()-30);
                        pixelCount++;
                    }
                }
            }
            int redAvg = redSum / pixelCount;
            int greenAvg = greenSum / pixelCount;
            int blueAvg = blueSum / pixelCount;

            return rgbaToIntHex(new int[]{redAvg, greenAvg, blueAvg, 255});
        } catch (NullPointerException|IOException e) {
            e.printStackTrace();
            System.out.print("assets/"+resourceLocation.getNamespace()+"/"+resourceLocation.getPath());
            return rgbaToIntHex(new int[]{0, 0, 0, 0});
        }
    }
}
