package igentuman.api.platform;

import igentuman.nc.client.gui.element.button.NCImageButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Platform translation layer for NeoForge 1.21.1 GUI widget APIs.
 *
 * <h3>ImageButton changes (1.20 to 1.21)</h3>
 * <ul>
 *   <li>Old: {@code new ImageButton(x, y, w, h, u, v, yDiffTex, texture, onPress)}</li>
 *   <li>Old: {@code new ImageButton(x, y, w, h, u, v, yDiffTex, texture, texW, texH, onPress)}</li>
 *   <li>New: Uses {@code WidgetSprites} with separate enabled/disabled sprite ResourceLocations</li>
 *   <li>New: {@code ImageButton.renderWidget()} uses {@code blitSprite()} (atlas-based), not {@code blit()} (UV-based)</li>
 * </ul>
 *
 * <p>Since the NC codebase uses UV-coordinate sprite sheets (not the atlas system),
 * all factory methods return {@link NCImageButton}, which preserves the old UV-based
 * {@code blit()} rendering.</p>
 */
public final class NCWidgets {

    private NCWidgets() {}

    /**
     * Create a UV-based image button on a 256x256 texture sheet.
     *
     * <p>Replaces the removed {@code ImageButton(x, y, w, h, u, v, yDiffTex, texture, onPress)}
     * constructor.</p>
     *
     * @param x        button x position
     * @param y        button y position
     * @param width    button width
     * @param height   button height
     * @param u        sprite U offset on the texture sheet
     * @param v        sprite V offset on the texture sheet
     * @param yDiffTex Y offset between normal/hovered/disabled states
     * @param texture  the 256x256 texture ResourceLocation
     * @param onPress  click handler
     * @return a button that renders via UV-based blit on the texture sheet
     */
    public static NCImageButton imageButton(int x, int y, int width, int height,
                                            int u, int v, int yDiffTex,
                                            ResourceLocation texture,
                                            Button.OnPress onPress) {
        return new NCImageButton(x, y, width, height, u, v, yDiffTex, texture, onPress);
    }

    /**
     * Create a UV-based image button with explicit texture dimensions.
     *
     * <p>Replaces the removed
     * {@code ImageButton(x, y, w, h, u, v, yDiffTex, texture, texW, texH, onPress)}
     * constructor.</p>
     *
     * @param x             button x position
     * @param y             button y position
     * @param width         button width
     * @param height        button height
     * @param u             sprite U offset on the texture sheet
     * @param v             sprite V offset on the texture sheet
     * @param yDiffTex      Y offset between normal/hovered/disabled states
     * @param texture       the texture ResourceLocation
     * @param textureWidth  texture sheet width in pixels
     * @param textureHeight texture sheet height in pixels
     * @param onPress       click handler
     * @return a button that renders via UV-based blit on the texture sheet
     */
    public static NCImageButton imageButton(int x, int y, int width, int height,
                                            int u, int v, int yDiffTex,
                                            ResourceLocation texture,
                                            int textureWidth, int textureHeight,
                                            Button.OnPress onPress) {
        return new NCImageButton(x, y, width, height, u, v, yDiffTex, texture, textureWidth, textureHeight, onPress);
    }
}
