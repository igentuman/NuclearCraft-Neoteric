package igentuman.nc.util;

import net.neoforged.fml.loading.FMLEnvironment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/** Samples the average color of a mod texture to tint molten-fuel fluids, with a server-safe fallback. */
public final class TextureUtil {

    /** Fallback tint when sampling is skipped (server) or the texture is missing. */
    public static final int DEFAULT_COLOR = 0xFFCCCCCC;

    private TextureUtil() {}

    /**
     * Samples the central 8x8 block of a mod texture and returns its average color as ARGB.
     *
     * @param texturePath resource path under the mod assets root, e.g.
     *                    {@code "textures/item/fuel/uranium/heu_233.png"}
     */
    public static int getAverageColor(String texturePath) {
        if (!FMLEnvironment.dist.isClient()) {
            return DEFAULT_COLOR;
        }
        String path = "assets/nuclearcraft/" + texturePath;
        try (InputStream stream = TextureUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                return DEFAULT_COLOR;
            }
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                return DEFAULT_COLOR;
            }
            int w = image.getWidth();
            int h = image.getHeight();
            int redSum = 0;
            int greenSum = 0;
            int blueSum = 0;
            int count = 0;
            for (int x = (w / 2) - 4; x < (w / 2) + 4; x++) {
                for (int y = (h / 2) - 4; y < (h / 2) + 4; y++) {
                    if (x < 0 || y < 0 || x >= w || y >= h) continue;
                    int argb = image.getRGB(x, y);
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    // Slight shift to better match the perceived item color.
                    redSum += Math.min(254, r + 20);
                    greenSum += g;
                    blueSum += Math.max(0, b - 30);
                    count++;
                }
            }
            if (count == 0) {
                return DEFAULT_COLOR;
            }
            int r = redSum / count;
            int g = greenSum / count;
            int b = blueSum / count;
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        } catch (Exception e) {
            return DEFAULT_COLOR;
        }
    }
}
