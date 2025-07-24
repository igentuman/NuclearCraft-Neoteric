package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.script.ScriptType;
import igentuman.nc.block.kugelblitz.entity.BlackHoleBE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;


public class NCKubeJsEvents {
    public static final EventGroup GROUP = EventGroup.of("NCKJSEvents");
    public static final EventHandler PLAYER_ENTER_BLACKHOLE = GROUP.server("PlayerEnterBlackhole", () -> PlayerEnterBlackholeEventJS.class);
    public static void onPlayerEnterBlackhole(BlackHoleBE.PlayerEnterBlackholeEvent event) {
        PlayerEnterBlackholeEventJS evenjs = new PlayerEnterBlackholeEventJS(event.getPlayer(), event.getBlackholePos(), event.getLevel());
        EventResult result = PLAYER_ENTER_BLACKHOLE.post(ScriptType.SERVER, evenjs);
        event.setCanceled(result.interruptDefault() || result.interruptFalse() || result.interruptTrue());
    }

    public static class PlayerEnterBlackholeEventJS extends EventJS {
        public ServerPlayer getPlayer() {
            return player;
        }

        public BlockPos getBlackholePos() {
            return blackholePos;
        }

        public Level getLevel() {
            return level;
        }

        private final ServerPlayer player;
        private final BlockPos blackholePos;
        private final Level level;

        public PlayerEnterBlackholeEventJS(ServerPlayer player, BlockPos blackholePos, Level level) {
            this.player = player;
            this.blackholePos = blackholePos;
            this.level = level;
        }
    }
}
