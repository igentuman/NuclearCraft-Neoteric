package igentuman.nc.client.render.builder;

import org.jspecify.annotations.Nullable;

public class StructureMesh {

    private BlockMesh mesh;

    public void rebuild(FakeStructureLevel level) {
        close();
        mesh = BlockMesh.build(level, level.getBlocks());
    }

    public boolean isEmpty() {
        return mesh == null || mesh.isEmpty();
    }

    public boolean isCurrent() {
        return mesh != null && mesh.isCurrent();
    }

    /** The built mesh remains usable by this frame after the cache is rebuilt or cleared. */
    public @Nullable BlockMesh snapshot() {
        return mesh;
    }

    public void close() {
        mesh = null;
    }
}
