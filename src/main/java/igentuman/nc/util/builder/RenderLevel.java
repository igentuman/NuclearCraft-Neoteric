package igentuman.nc.util.builder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class RenderLevel extends ClientLevel {
    public RenderLevel() {
        super(Minecraft.getInstance().level.connection, Minecraft.getInstance().level.getLevelData(), OVERWORLD, Minecraft.getInstance().level.dimensionTypeRegistration(), 2, 0, () -> null, Minecraft.getInstance().levelRenderer, false, 0);
    }

    @Override
    public void updateSkyBrightness() {
    }

    protected void prepareWeather() {
    }

}
