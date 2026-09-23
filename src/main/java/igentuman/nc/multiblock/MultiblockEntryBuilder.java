package igentuman.nc.multiblock;

import igentuman.nc.api.multiblock.AbstractMultiblockCache;
import igentuman.nc.api.multiblock.AbstractMultiblockLogic;
import igentuman.nc.api.multiblock.AbstractMultiblockValidator;
import igentuman.nc.registration.ModEntry;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Fluent builder that assembles a {@link MultiblockEntry} from controller, ports, casing, interior, validator, logic and cache. */
public class MultiblockEntryBuilder {

    private final String name;
    private ModEntry controller;
    private final List<ModEntry> ports = new ArrayList<>();
    private final List<Supplier<Block>> casing = new ArrayList<>();
    private final List<Supplier<Block>> interior = new ArrayList<>();
    private Supplier<? extends AbstractMultiblockLogic<?>> logicSupplier = () -> new AbstractMultiblockLogic<>() {
    };
    private Supplier<? extends AbstractMultiblockCache> cacheSupplier;
    private Supplier<? extends AbstractMultiblockValidator<?>> validatorSupplier;

    private MultiblockEntryBuilder(String name) {
        this.name = name;
    }

    public static MultiblockEntryBuilder name(String name) {
        return new MultiblockEntryBuilder(name);
    }

    public MultiblockEntryBuilder controller(ModEntry controller) {
        this.controller = controller;
        return this;
    }

    public MultiblockEntryBuilder ports(ModEntry... entries) {
        Collections.addAll(this.ports, entries);
        return this;
    }

    @SafeVarargs
    public final MultiblockEntryBuilder casing(Supplier<Block>... blocks) {
        Collections.addAll(this.casing, blocks);
        return this;
    }

    @SafeVarargs
    public final MultiblockEntryBuilder interior(Supplier<Block>... blocks) {
        Collections.addAll(this.interior, blocks);
        return this;
    }

    public MultiblockEntryBuilder logic(Supplier<? extends AbstractMultiblockLogic<?>> logicSupplier) {
        this.logicSupplier = logicSupplier;
        return this;
    }

    public MultiblockEntryBuilder cache(Supplier<? extends AbstractMultiblockCache> cacheSupplier) {
        this.cacheSupplier = cacheSupplier;
        return this;
    }

    public MultiblockEntryBuilder validator(Supplier<? extends AbstractMultiblockValidator<?>> validatorSupplier) {
        this.validatorSupplier = validatorSupplier;
        return this;
    }

    public MultiblockEntry build() {
        Objects.requireNonNull(validatorSupplier, () -> name + " multiblock needs a validator");
        Objects.requireNonNull(cacheSupplier, () -> name + " multiblock needs a cache");
        List<Supplier<Block>> required = new ArrayList<>();
        if (controller != null && controller.block() != null) required.add(() -> controller.block().get());
        for (ModEntry port : ports) {
            if (port.block() != null) required.add(() -> port.block().get());
        }
        required.addAll(casing);
        required.addAll(interior);

        MultiblockEntry entry = new MultiblockEntry(name, validatorSupplier, logicSupplier, cacheSupplier, required,
                controller, List.copyOf(ports));

        MultiblockRegistry.register(entry);
        return entry;
    }
}
