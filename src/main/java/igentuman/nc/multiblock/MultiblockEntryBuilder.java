package igentuman.nc.multiblock;

import igentuman.nc.api.multiblock.BlockPredicate;
import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockLogic;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import igentuman.nc.api.impl.CubicMultiblockValidator;
import igentuman.nc.api.impl.MultiblockCacheImpl;
import igentuman.nc.api.impl.MultiblockLogicImpl;
import igentuman.nc.registration.ModEntry;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/** Fluent builder that assembles a {@link MultiblockEntry} from controller, ports, casing, interior, and size. */
public class MultiblockEntryBuilder {

    private final String name;
    private ModEntry controller;
    private final List<ModEntry> ports = new ArrayList<>();
    private final List<Supplier<Block>> casing = new ArrayList<>();
    private final List<Supplier<Block>> interior = new ArrayList<>();
    private int minWidth = 3;
    private int maxWidth = 3;
    private int minHeight = 3;
    private int maxHeight = 3;
    private int minDepth = 3;
    private int maxDepth = 3;
    private Supplier<IMultiblockLogic> logicSupplier = () -> new MultiblockLogicImpl();
    private Supplier<IMultiblockCache> cacheSupplier = MultiblockCacheImpl::new;
    private Supplier<IMultiblockValidator> validatorSupplier;

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

    public MultiblockEntryBuilder size(int width, int height, int depth) {
        this.minWidth = width;
        this.maxWidth = width;
        this.minHeight = height;
        this.maxHeight = height;
        this.minDepth = depth;
        this.maxDepth = depth;
        return this;
    }

    public MultiblockEntryBuilder sizeRange(int minWidth, int maxWidth, int minHeight, int maxHeight, int minDepth, int maxDepth) {
        if (minWidth < 1 || minHeight < 1 || minDepth < 1)
            throw new IllegalArgumentException("multiblock size must be >= 1");
        if (maxWidth < minWidth || maxHeight < minHeight || maxDepth < minDepth)
            throw new IllegalArgumentException("multiblock size range max < min");
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        return this;
    }

    public MultiblockEntryBuilder logic(Supplier<IMultiblockLogic> logicSupplier) {
        this.logicSupplier = logicSupplier;
        return this;
    }

    public MultiblockEntryBuilder cache(Supplier<IMultiblockCache> cacheSupplier) {
        this.cacheSupplier = cacheSupplier;
        return this;
    }

    public MultiblockEntryBuilder validator(Supplier<IMultiblockValidator> validatorSupplier) {
        this.validatorSupplier = validatorSupplier;
        return this;
    }

    public MultiblockEntry build() {
        Supplier<IMultiblockValidator> validator = validatorSupplier != null
                ? validatorSupplier
                : defaultValidatorSupplier();

        List<Supplier<Block>> required = new ArrayList<>();
        if (controller != null && controller.block() != null) required.add(() -> controller.block().get());
        for (ModEntry port : ports) {
            if (port.block() != null) required.add(() -> port.block().get());
        }
        required.addAll(casing);
        required.addAll(interior);

        MultiblockEntry entry = new MultiblockEntry(name, validator, logicSupplier, cacheSupplier, required, controller, List.copyOf(ports));

        MultiblockRegistry.register(entry);
        return entry;
    }

    private Supplier<IMultiblockValidator> defaultValidatorSupplier() {
        BlockPredicate controllerPredicate = controllerPredicate();
        BlockPredicate shellPredicate = shellPredicate();
        BlockPredicate interiorPredicate = interiorPredicate();
        int minW = minWidth, maxW = maxWidth;
        int minH = minHeight, maxH = maxHeight;
        int minD = minDepth, maxD = maxDepth;
        return () -> new CubicMultiblockValidator(
                controllerPredicate, shellPredicate, interiorPredicate,
                minW, maxW, minH, maxH, minD, maxD);
    }

    private BlockPredicate controllerPredicate() {
        if (controller == null || controller.block() == null) {
            return BlockPredicate.any();
        }
        ModEntry ctrl = controller;
        return (state, be) -> state.is(ctrl.block().get());
    }

    private BlockPredicate shellPredicate() {
        List<ModEntry> portList = List.copyOf(ports);
        List<Supplier<Block>> casingList = List.copyOf(casing);
        ModEntry ctrl = controller;
        return (state, be) -> {
            if (ctrl != null && ctrl.block() != null && state.is(ctrl.block().get())) return true;
            for (ModEntry p : portList) {
                if (p.block() != null && state.is(p.block().get())) return true;
            }
            for (Supplier<Block> c : casingList) {
                Block block = c.get();
                if (block != null && state.is(block)) return true;
            }
            return false;
        };
    }

    private BlockPredicate interiorPredicate() {
        if (interior.isEmpty()) {
            return BlockPredicate.any();
        }
        List<Supplier<Block>> interiorList = List.copyOf(interior);
        return (state, be) -> {
            for (Supplier<Block> i : interiorList) {
                Block block = i.get();
                if (block != null && state.is(block)) return true;
            }
            return false;
        };
    }
}
