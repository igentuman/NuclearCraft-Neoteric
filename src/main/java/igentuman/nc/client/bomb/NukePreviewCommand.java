package igentuman.nc.client.bomb;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import igentuman.nc.NuclearCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT)
public final class NukePreviewCommand {

    private static final double PICK_DISTANCE = 512.0;
    private static int nextId = -1;

    private NukePreviewCommand() {}

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        if (FMLEnvironment.production) return;
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("nukepreview")
                .then(Commands.literal("spawn")
                        .executes(ctx -> spawn(1f))
                        .then(Commands.argument("yield", FloatArgumentType.floatArg(0.05f, 8f))
                                .executes(ctx -> spawn(FloatArgumentType.getFloat(ctx, "yield")))))
                .then(Commands.literal("seek")
                        .then(Commands.argument("seconds", FloatArgumentType.floatArg(0f))
                                .executes(NukePreviewCommand::seek)))
                .then(Commands.literal("clear").executes(ctx -> clear()))
                .then(Commands.literal("stats").executes(ctx -> stats())));
    }

    private static int spawn(float yield) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return 0;
        Vec3 eye = mc.player.getEyePosition();
        Vec3 end = eye.add(mc.player.getLookAngle().scale(PICK_DISTANCE));
        BlockHitResult hit = mc.level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, mc.player));
        BlockPos pos = hit.getType() == HitResult.Type.MISS ? BlockPos.containing(end) : hit.getBlockPos().above();
        BombFxManager.onDetonationStart(nextId--, pos, yield);
        message("Preview detonation at " + pos.toShortString() + " yield " + yield);
        return 1;
    }

    private static int seek(CommandContext<CommandSourceStack> ctx) {
        float seconds = FloatArgumentType.getFloat(ctx, "seconds");
        int ticks = Mth.floor(seconds * 20f);
        int count = 0;
        for (ActiveBomb bomb : BombFxManager.active()) {
            if (bomb.id >= 0) continue;
            bomb.tickCounter = ticks;
            bomb.soundPlayed = true;
            count++;
        }
        message("Seeked " + count + " preview detonation(s) to " + seconds + " s");
        return count;
    }

    private static int clear() {
        int before = BombFxManager.active().size();
        BombFxManager.active().removeIf(bomb -> bomb.id < 0);
        message("Removed " + (before - BombFxManager.active().size()) + " preview detonation(s)");
        return 1;
    }

    private static int stats() {
        StringBuilder text = new StringBuilder("Nuke smoke live: ").append(GroundSmokeEmitter.liveCount())
                .append(" | debris fires: ").append(DebrisFireEmitter.fireCount())
                .append(" live: ").append(DebrisFireEmitter.liveCount())
                .append(" | shock debris: ").append(ShockwaveDebrisEmitter.liveCount());
        for (ActiveBomb bomb : BombFxManager.active()) {
            text.append(String.format(" | #%d age %.2fs morph %.2f fade %.2f", bomb.id,
                    bomb.ageSeconds(0f), bomb.visual.morph, bomb.visual.fade));
        }
        message(text.toString());
        return 1;
    }

    private static void message(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.displayClientMessage(Component.literal(text), false);
    }
}
