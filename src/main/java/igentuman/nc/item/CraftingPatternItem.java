package igentuman.nc.item;

import igentuman.nc.client.item.CraftingPatternRenderer;
import igentuman.nc.handler.crafter.CraftingPattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.util.TextUtils.__;

/**
 * Dual-state pattern medium. Blank when it carries no {@link CraftingPattern#TAG}; encoded once a
 * recipe is written in the Pattern Encoder. Encoded stacks render the recipe output's icon via
 * {@link CraftingPatternRenderer} (selected by the {@code encoded} item property override).
 */
public class CraftingPatternItem extends Item {

    public CraftingPatternItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!CraftingPattern.isEncoded(stack)) {
            tooltip.add(__("tooltip.nc.crafting_pattern.blank").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return CraftingPatternRenderer.get();
            }
        });
    }
}
