package igentuman.nc.world.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;

public class WastelandScatterFeature extends TemplateFeature {

    private final String prefix;
    private final List<ResourceLocation> pool = new ArrayList<>();
    private volatile boolean loaded = false;

    public WastelandScatterFeature(Codec<NoneFeatureConfiguration> codec, String prefix) {
        super(codec);
        this.prefix = prefix;
    }

    private List<ResourceLocation> pool(StructureTemplateManager manager) {
        if (loaded) return pool;
        synchronized (pool) {
            if (loaded) return pool;
            for (ResourceLocation location : manager.listTemplates().toList()) {
                String path = location.getPath();
                if (!path.startsWith(prefix)) continue;
                String fileName = path.substring(path.lastIndexOf('/') + 1);
                int rarity = fileName.matches(".*\\d+$")
                        ? Integer.parseInt(fileName.replaceAll(".*?([0-9]+)$", "$1")) : 0;
                int weight = 11 - Math.abs(rarity);
                for (int i = 0; i < weight; i++) pool.add(location);
                pool.add(location);
            }
            loaded = true;
        }
        return pool;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        LevelAccessor world = context.level();
        BlockPos pos = context.origin();
        if (!inWasteland(world, pos)) return false;

        StructureTemplateManager manager = templates(world);
        List<ResourceLocation> pool = pool(manager);
        if (pool.isEmpty()) return false;

        ResourceLocation id = pool.get(context.random().nextInt(pool.size()));
        StructureTemplate template = manager.get(id).orElse(null);
        if (template == null) return false;

        StructurePlaceSettings settings = randomSettings(context.random());
        int vSize = template.getSize().getY();

        int total = 0, count = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int x = 0; x < 7; x += 2) {
            for (int z = 0; z < 7; z += 2) {
                int groundY = groundLevel(world, pos.offset(x, 0, z));
                if (groundY > 0) {
                    total += groundY;
                    count++;
                    min = Math.min(min, groundY);
                    max = Math.max(max, groundY);
                }
            }
        }
        if (count == 0) return false;
        if (max - min > 5) return false;

        int y = total / count;
        if (id.getPath().contains("bunker")) y -= (vSize - 2);
        y -= 2;

        placeTemplate(template, (ServerLevelAccessor) world, new BlockPos(pos.getX(), y, pos.getZ()), settings, world.getRandom());
        return true;
    }

    private static int groundLevel(LevelAccessor world, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(pos.getX(), world.getMaxBuildHeight(), pos.getZ());
        while (cursor.getY() > world.getMinBuildHeight() && world.isEmptyBlock(cursor)) {
            cursor.move(0, -1, 0);
        }
        return cursor.getY() <= world.getMinBuildHeight() ? -1 : cursor.getY();
    }
}
