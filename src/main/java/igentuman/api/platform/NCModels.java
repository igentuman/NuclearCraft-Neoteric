package igentuman.api.platform;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Platform wrapper for NeoForge 1.21.1 model generation API changes.
 *
 * <p>CustomLoaderBuilder constructor changed:
 * <ul>
 *   <li>1.20: {@code CustomLoaderBuilder(ResourceLocation, T, ExistingFileHelper)}</li>
 *   <li>1.21: {@code CustomLoaderBuilder(ResourceLocation, T, ExistingFileHelper, boolean allowInlineElements)}</li>
 * </ul>
 */
public final class NCModels {
    private NCModels() {}

    /**
     * Creates a CustomLoaderBuilder for use in BlockModelBuilder.customLoader().
     * Wraps the 4th param {@code allowInlineElements} added in NeoForge 1.21.1.
     *
     * @param loaderId The geometry loader ResourceLocation
     * @param parent   The parent BlockModelBuilder
     * @param helper   The ExistingFileHelper
     * @return a new CustomLoaderBuilder instance
     */
    public static CustomLoaderBuilder<BlockModelBuilder> customLoader(
            ResourceLocation loaderId, BlockModelBuilder parent, ExistingFileHelper helper) {
        return new CustomLoaderBuilder<>(loaderId, parent, helper, false) {};
    }
}
