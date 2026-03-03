package igentuman.nc.setup.registration;

import igentuman.nc.NuclearCraft;
import igentuman.nc.radiation.data.PlayerRadiation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class NCAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NuclearCraft.MODID);

    public static final Supplier<AttachmentType<PlayerRadiation>> PLAYER_RADIATION =
        ATTACHMENTS.register("player_radiation", () ->
            AttachmentType.serializable(PlayerRadiation::new).build());

    public static void init(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}
