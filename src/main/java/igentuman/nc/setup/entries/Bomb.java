package igentuman.nc.setup.entries;

import igentuman.nc.block.bomb.Pu239BombBlock;
import igentuman.nc.block_entity.bomb.Pu239BombBE;
import igentuman.nc.entity.PrimedFissionBombEntity;
import igentuman.nc.setup.ModEntries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.registration.ModEntryBuilder.add;
import static igentuman.nc.registration.ModEntryBuilder.addEntityType;

public class Bomb extends ModEntries {

    public static final DeferredHolder<EntityType<?>, EntityType<PrimedFissionBombEntity>> PRIMED_FISSION_BOMB =
            addEntityType("primed_fission_bomb",
                    EntityType.Builder.<PrimedFissionBombEntity>of(PrimedFissionBombEntity::new, MobCategory.MISC)
                            .sized(0.98F, 0.98F)
                            .noSummon()
                            .fireImmune()
                            .clientTrackingRange(32)
                            .updateInterval(20));

    public static final TicketController TICKET_CONTROLLER = new TicketController(rl("pu_239_bomb"));

    public static void bomb() {
        add("pu_239_bomb")
                .block(name -> new Pu239BombBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.METAL)
                                .sound(SoundType.METAL)
                                .strength(5.0f)
                                .noOcclusion()
                                .requiresCorrectToolForDrops(),
                        name))
                .blockEntity(Pu239BombBE::new)
                .build();
    }
}
