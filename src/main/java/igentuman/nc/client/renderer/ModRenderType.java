package igentuman.nc.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import igentuman.nc.compat.iris.IrisCompat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static igentuman.nc.NuclearCraft.rl;

public final class ModRenderType {

    private ModRenderType() {
    }

    /** Fog-faded, unlit, double-sided translucent sprite quads ({@code POSITION_TEX_COLOR}). */
    public static final RenderPipeline BILLBOARD_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(rl("pipeline/billboard"))
            .withVertexShader(rl("core/rendertype_billboard"))
            .withFragmentShader(rl("core/rendertype_billboard"))
            .withSampler("Sampler0")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .build();

    /** Opaque, double-sided black hole core that writes colour and depth. */
    public static final RenderPipeline BLACK_HOLE_CORE_PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(rl("pipeline/black_hole_core"))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withCull(false)
            .build();

    /** Additive (lightning blend) accretion disk that writes colour only. */
    public static final RenderPipeline BLACK_HOLE_DISK_PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(rl("pipeline/black_hole_disk"))
            .withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.LIGHTNING), ColorTargetState.WRITE_COLOR))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withCull(false)
            .build();

    /** Lines drawn on top of everything (no depth test, no depth write); used by debug outlines. */
    public static final RenderPipeline LINES_NO_DEPTH_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(rl("pipeline/lines_no_depth"))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build();

    private static RenderPipeline ghostPipeline(String name, RenderPipeline material) {
        return material.toBuilder()
                .withLocation(rl("pipeline/" + name))
                .withFragmentShader(rl("core/ghost_block"))
                .withShaderDefine("GHOST_ALPHA", 0.5F)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                .build();
    }

    /** Ghosts retain material cutoffs, then fade to 50% opacity without testing or writing depth. */
    public static final RenderPipeline GHOST_SOLID_PIPELINE = ghostPipeline("ghost_solid", RenderPipelines.SOLID_BLOCK);
    public static final RenderPipeline GHOST_CUTOUT_PIPELINE = ghostPipeline("ghost_cutout", RenderPipelines.CUTOUT_BLOCK);
    public static final RenderPipeline GHOST_TRANSLUCENT_PIPELINE = ghostPipeline("ghost_translucent", RenderPipelines.TRANSLUCENT_BLOCK);

    /** Every pipeline the mod creates for world rendering; registered by {@link ModShaders}. */
    public static final List<RenderPipeline> PIPELINES = List.of(
            BILLBOARD_PIPELINE, BLACK_HOLE_CORE_PIPELINE, BLACK_HOLE_DISK_PIPELINE, LINES_NO_DEPTH_PIPELINE,
            GHOST_SOLID_PIPELINE, GHOST_CUTOUT_PIPELINE, GHOST_TRANSLUCENT_PIPELINE);

    private static final Function<Identifier, RenderType> BILLBOARD_CUSTOM = Util.memoize(texture ->
            RenderType.create("nc_billboard", RenderSetup.builder(BILLBOARD_PIPELINE)
                    .withTexture("Sampler0", texture)
                    .affectsCrumbling()
                    .sortOnUpload()
                    .bufferSize(256)
                    .createRenderSetup()));

    // Iris maps the built-in emissive entity pipeline to the shader pack's entity program.
    public static final Function<Identifier, RenderType> BILLBOARD = texture ->
            IrisCompat.shaderPackInUse() ? RenderTypes.entityTranslucentEmissive(texture) : BILLBOARD_CUSTOM.apply(texture);

    public static final RenderType BLACK_HOLE_CORE = RenderType.create("nc_black_hole_core",
            RenderSetup.builder(BLACK_HOLE_CORE_PIPELINE).bufferSize(4096).createRenderSetup());

    public static final RenderType BLACK_HOLE_DISK = RenderType.create("nc_black_hole_disk",
            RenderSetup.builder(BLACK_HOLE_DISK_PIPELINE).bufferSize(8192).sortOnUpload().createRenderSetup());

    private static RenderType blockType(String name, RenderPipeline pipeline, boolean translucent) {
        RenderSetup.RenderSetupBuilder setup = RenderSetup.builder(pipeline)
                .useLightmap()
                .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
                .bufferSize(RenderType.SMALL_BUFFER_SIZE);
        if (translucent) setup.sortOnUpload();
        return RenderType.create(name, setup.createRenderSetup());
    }

    // Keep the default output target so picture-in-picture can redirect both attachments.
    private static final RenderType PREVIEW_SOLID = blockType("nc_preview_solid", RenderPipelines.SOLID_BLOCK, false);
    private static final RenderType PREVIEW_CUTOUT = blockType("nc_preview_cutout", RenderPipelines.CUTOUT_BLOCK, false);
    private static final RenderType PREVIEW_TRANSLUCENT = blockType("nc_preview_translucent", RenderPipelines.TRANSLUCENT_BLOCK, true);

    // Every ghost layer blends, so sort solid and cutout quads as well as translucent ones.
    private static final RenderType GHOST_SOLID = blockType("nc_ghost_solid", GHOST_SOLID_PIPELINE, true);
    private static final RenderType GHOST_CUTOUT = blockType("nc_ghost_cutout", GHOST_CUTOUT_PIPELINE, true);
    private static final RenderType GHOST_TRANSLUCENT = blockType("nc_ghost_translucent", GHOST_TRANSLUCENT_PIPELINE, true);

    /** Lit, see-through block quads preserving the baked material's alpha cutoff before fading. */
    public static RenderType ghostBlock(ChunkSectionLayer layer) {
        return switch (layer) {
            case SOLID -> GHOST_SOLID;
            case CUTOUT -> GHOST_CUTOUT;
            case TRANSLUCENT -> GHOST_TRANSLUCENT;
        };
    }

    /** Lit, depth-tested block quads preserving the baked material's alpha mode. */
    public static RenderType previewBlock(ChunkSectionLayer layer) {
        return switch (layer) {
            case SOLID -> PREVIEW_SOLID;
            case CUTOUT -> PREVIEW_CUTOUT;
            case TRANSLUCENT -> PREVIEW_TRANSLUCENT;
        };
    }

    /** See-through debug lines ({@code POSITION_COLOR_NORMAL_LINE_WIDTH}). */
    public static final RenderType LINES_NO_DEPTH = RenderType.create("nc_lines_no_depth",
            RenderSetup.builder(LINES_NO_DEPTH_PIPELINE).createRenderSetup());
}
