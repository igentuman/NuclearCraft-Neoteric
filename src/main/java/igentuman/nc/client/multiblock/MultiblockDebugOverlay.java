package igentuman.nc.client.multiblock;

import igentuman.nc.network.PacketMultiblockDebug;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MultiblockDebugOverlay implements LayeredDraw.Layer {

    public static final MultiblockDebugOverlay INSTANCE = new MultiblockDebugOverlay();

    private MultiblockDebugOverlay() {
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker deltaTracker) {
        if (!MultiblockDebugClient.isActive()) return;
        Minecraft minecraft = Minecraft.getInstance();
        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return;
        PacketMultiblockDebug snapshot = MultiblockDebugClient.get(blockHit.getBlockPos());
        if (snapshot == null) return;

        List<String> lines = new ArrayList<>();
        lines.add(snapshot.machineId().getPath() + " - " + snapshot.phase() + ": " + snapshot.status());
        lines.add(snapshot.diagnosticKey());
        if (snapshot.minimum() != null && snapshot.maximum() != null) {
            lines.add("bounds: " + shortPos(snapshot.minimum()) + " -> " + shortPos(snapshot.maximum()));
        }
        if (snapshot.failingPosition() != null) lines.add("error: " + shortPos(snapshot.failingPosition()));
        if (snapshot.expected() != null || snapshot.actual() != null) {
            lines.add("expected: " + snapshot.expected() + "  actual: " + snapshot.actual());
        }
        if (snapshot.total() > 0) lines.add("progress: " + snapshot.completed() + "/" + snapshot.total());
        lines.add("elapsed: " + String.format(Locale.ROOT, "%.3f ms", snapshot.elapsedNanos() / 1_000_000D));

        int x = 8;
        int y = graphics.guiHeight() / 2 - lines.size() * 5;
        for (String line : lines) {
            graphics.drawString(minecraft.font, line, x, y, 0xFFFFFF55, true);
            y += 10;
        }
    }

    private static String shortPos(net.minecraft.core.BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }
}
