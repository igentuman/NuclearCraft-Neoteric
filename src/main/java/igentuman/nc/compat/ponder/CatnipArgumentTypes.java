package igentuman.nc.compat.ponder;

import net.createmod.catnip.impl.command.argument.ConfigIdArgument;
import net.createmod.catnip.impl.command.argument.ConfigPathArgument;
import net.createmod.catnip.impl.command.argument.ModIdArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static igentuman.nc.NuclearCraft.MODID;

/**
 * Catnip (bundled with Ponder) adds a {@code /catnip config} command whose argument types it never
 * registers; Create normally does that. Without Create, building the command tree packet throws
 * "Unrecognized argument type" and the player is kicked on join, so register them here.
 */
public final class CatnipArgumentTypes {
    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
            DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MODID);

    static {
        ARGUMENT_TYPES.register("catnip_config_id", () -> ArgumentTypeInfos.registerByClass(
                ConfigIdArgument.class, SingletonArgumentInfo.contextFree(ConfigIdArgument::configId)));
        ARGUMENT_TYPES.register("catnip_config_path", () -> ArgumentTypeInfos.registerByClass(
                ConfigPathArgument.class, SingletonArgumentInfo.contextFree(ConfigPathArgument::path)));
        ARGUMENT_TYPES.register("catnip_mod_id", () -> ArgumentTypeInfos.registerByClass(
                ModIdArgument.class, SingletonArgumentInfo.contextFree(ModIdArgument::modId)));
    }

    private CatnipArgumentTypes() {
    }

    public static void register(IEventBus modEventBus) {
        ARGUMENT_TYPES.register(modEventBus);
    }
}
