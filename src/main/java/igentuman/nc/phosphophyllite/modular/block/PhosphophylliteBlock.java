package igentuman.nc.phosphophyllite.modular.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import igentuman.nc.phosphophyllite.modular.api.BlockModule;
import igentuman.nc.phosphophyllite.modular.api.IModularBlock;
import igentuman.nc.phosphophyllite.modular.api.ModuleRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("unused")
public class PhosphophylliteBlock extends Block implements IModularBlock {
    
    public static final Logger MODULE_LOGGER = LogManager.getLogger("Phosphophyllite/ModularBlock");
    @Deprecated(forRemoval = true)
    public static final Logger LOGGER = MODULE_LOGGER;
    
    private Int2ObjectMap<BlockModule<?>> modules;
    
    private List<BlockModule<?>> moduleList;
    
    private void buildModules() {
        if (modules != null) {
            return;
        }
        final Int2ObjectMap<BlockModule<?>> modules = new Int2ObjectOpenHashMap<>();
        final List<BlockModule<?>> moduleList = new ArrayList<>();
        // must be called before super constructor
        Class<?> thisClazz = this.getClass();
        ModuleRegistry.forEachBlockModule((clazz, constructor) -> {
            if (clazz.isAssignableFrom(thisClazz)) {
                var module = constructor.apply(this);
                modules.put(clazz.hashCode(), module);
                moduleList.add(module);
            }
        });
        this.modules = modules;
        this.moduleList = Collections.unmodifiableList(moduleList);
    }
    
    @Override
    public BlockModule<?> module(Class<? extends IModularBlock> interfaceClazz) {
        return modules.get(interfaceClazz.hashCode());
    }
    
    @Override
    public List<BlockModule<?>> modules() {
        return moduleList;
    }
    
    public PhosphophylliteBlock(Properties properties) {
        super(properties);
        BlockState state = buildDefaultState(defaultBlockState());
        for (var module : moduleList) {
            state = module.buildDefaultState(state);
        }
        registerDefaultState(state);
    }
    
    protected BlockState buildDefaultState(BlockState state) {
        return state;
    }
    
    @Override
    protected final void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        buildModules(); // because this is called from the super constructor, and i need this shit built
        buildStateDefinition(builder);
        for (var module : moduleList) {
            module.buildStateDefinition(builder);
        }
    }
    
    protected void buildStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public final void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        onNeighborChange(state, worldIn, pos, blockIn, fromPos, isMoving);
        for (var module : moduleList) {
            module.onNeighborChange(state, worldIn, pos, blockIn, fromPos, isMoving);
        }
    }
    
    public void onNeighborChange(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
    }
    
    @Override
    public final void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        onPlaced(level, pos, state, placer, stack);
        for (var module : moduleList) {
            module.onPlaced(level, pos, state, placer, stack);
        }
    }
    
    public void onPlaced(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    }
    
    @Override
    public final InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var result = onUse(state, level, pos, player, hand, hitResult);
        for (var module : moduleList) {
            var moduleResult = module.onUse(state, level, pos, player, hand, hitResult);
            if (result == InteractionResult.PASS) {
                result = moduleResult;
            } else if (moduleResult != InteractionResult.PASS) {
                MODULE_LOGGER.warn("Multiple modules using same onUse for block type \"" + getClass().getSimpleName() + "\" at " + pos);
            }
        }
        return result;
    }
    
    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }
}
