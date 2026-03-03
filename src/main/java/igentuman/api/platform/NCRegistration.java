package igentuman.api.platform;

import java.util.Arrays;
import java.util.function.Supplier;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Typed registration factory methods that bridge DeferredRegister's wildcard-based
 * return types to concrete generic types.
 *
 * NeoForge's DeferredHolder has two type parameters with a strict bound (T extends R).
 * DeferredRegister<MenuType<?>>.register() returns DeferredHolder<MenuType<?>, MenuType<?>>,
 * but callers need DeferredHolder<MenuType<?>, MenuType<Specific>>. Java generics are
 * invariant, so a direct assignment fails. These factory methods perform a single
 * unchecked cast that is safe because generics are erased at runtime.
 */
public final class NCRegistration {

    private NCRegistration() {}

    // === MenuType ===

    @SuppressWarnings("unchecked")
    public static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>>
            registerMenu(DeferredRegister<MenuType<?>> registry, String name, IContainerFactory<T> factory) {
        return (DeferredHolder<MenuType<?>, MenuType<T>>) (DeferredHolder<?, ?>)
                registry.register(name, () -> IMenuTypeExtension.create(factory));
    }

    // === BlockEntityType ===

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>>
            registerBlockEntity(DeferredRegister<BlockEntityType<?>> registry, String name,
                                BlockEntityType.BlockEntitySupplier<T> supplier, Supplier<? extends Block>... blocks) {
        return (DeferredHolder<BlockEntityType<?>, BlockEntityType<T>>) (DeferredHolder<?, ?>)
                registry.register(name, () -> BlockEntityType.Builder.of(supplier,
                        Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new)).build(null));
    }

    // === Fluid ===

    @SuppressWarnings("unchecked")
    public static <T extends Fluid> DeferredHolder<Fluid, T>
            registerFluid(DeferredRegister<Fluid> registry, String name, Supplier<? extends T> supplier) {
        return (DeferredHolder<Fluid, T>) (DeferredHolder<?, ?>)
                registry.register(name, supplier);
    }

    // === EntityType ===

    @SuppressWarnings("unchecked")
    public static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>>
            registerEntity(DeferredRegister<EntityType<?>> registry, String name,
                           Supplier<EntityType<T>> supplier) {
        return (DeferredHolder<EntityType<?>, EntityType<T>>) (DeferredHolder<?, ?>)
                registry.register(name, supplier);
    }

    // === Feature ===

    @SuppressWarnings("unchecked")
    public static <T extends Feature<?>> DeferredHolder<Feature<?>, T>
            registerFeature(DeferredRegister<Feature<?>> registry, String name, Supplier<T> supplier) {
        return (DeferredHolder<Feature<?>, T>) (DeferredHolder<?, ?>)
                registry.register(name, supplier);
    }
}
