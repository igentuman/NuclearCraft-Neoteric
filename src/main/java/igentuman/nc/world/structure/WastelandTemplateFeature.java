package igentuman.nc.world.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class WastelandTemplateFeature extends TemplateFeature {

    private final Identifier templateId;

    public WastelandTemplateFeature(Codec<NoneFeatureConfiguration> codec, Identifier templateId) {
        super(codec);
        this.templateId = templateId;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        LevelAccessor world = context.level();
        BlockPos pos = context.origin();
        if (!inWasteland(world, pos)) return false;

        StructureTemplateManager manager = templates(world);
        StructureTemplate template = manager.get(templateId).orElse(null);
        if (template == null) return false;

        StructurePlaceSettings settings = randomSettings(context.random());
        placeTemplate(template, (ServerLevelAccessor) world, pos.below(), settings, world.getRandom());
        return true;
    }
}
