package igentuman.nc.multiblock;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.entity.MultiblockControllerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultiblockHandlerTest {

    @Mock
    private AbstractNCMultiblock mockMultiblock;

    @Mock
    private MultiblockController mockController;

    @Mock
    private MultiblockControllerBE mockControllerBE;

    private Field multiblockField;
    private Field chunkCacheField;
    private Field toRemoveField;
    private Field ignoreUpdateField;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Use reflection to access private static fields
        multiblockField = MultiblockHandler.class.getDeclaredField("multiblocks");
        multiblockField.setAccessible(true);

        chunkCacheField = MultiblockHandler.class.getDeclaredField("chunkCache");
        chunkCacheField.setAccessible(true);

        toRemoveField = MultiblockHandler.class.getDeclaredField("toRemove");
        toRemoveField.setAccessible(true);

        ignoreUpdateField = MultiblockHandler.class.getDeclaredField("ignoreUpdate");
        ignoreUpdateField.setAccessible(true);

        // Clear the static collections before each test
        ((HashMap<?, ?>) multiblockField.get(null)).clear();
        ((HashMap<?, ?>) chunkCacheField.get(null)).clear();
        ((List<?>) toRemoveField.get(null)).clear();
        ((List<?>) ignoreUpdateField.get(null)).clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        try {
            ((HashMap<?, ?>) multiblockField.get(null)).clear();
            ((HashMap<?, ?>) chunkCacheField.get(null)).clear();
            ((List<?>) toRemoveField.get(null)).clear();
            ((List<?>) ignoreUpdateField.get(null)).clear();
        } catch (Exception e) {
            // Ignore any cleanup exceptions
        }
    }

    @Test
    void testRemoveMultiblock() throws IllegalAccessException {
        // Setup
        ChunkPos chunkPos = new ChunkPos(0, 0);
        when(mockMultiblock.getId()).thenReturn("test-id");
        when(mockMultiblock.controller()).thenReturn(mockController);
        when(mockMultiblock.getChunk()).thenReturn(chunkPos);

        // Add the multiblock first
        MultiblockHandler.instance.addMultiblock(mockMultiblock);

        // Then remove it
        MultiblockHandler.instance.removeMultiblock(mockMultiblock);

        // Verify it was removed
        HashMap<String, AbstractNCMultiblock> multiblocks =
                (HashMap<String, AbstractNCMultiblock>) multiblockField.get(null);
        assertFalse(multiblocks.containsKey("test-id"));

        // Check chunk cache was cleaned up
        HashMap<Long, List<String>> chunkCache =
                (HashMap<Long, List<String>>) chunkCacheField.get(null);
        assertTrue(chunkCache.isEmpty() || !chunkCache.get(chunkPos.toLong()).contains("test-id"));
    }

    @Test
    void testAddMultiblockForce() throws IllegalAccessException {
        // Setup
        ChunkPos chunkPos = new ChunkPos(0, 0);

        // Create first multiblock
        AbstractNCMultiblock firstMultiblock = mock(AbstractNCMultiblock.class);
        when(firstMultiblock.getId()).thenReturn("same-id");
        when(firstMultiblock.controller()).thenReturn(mockController);
        when(firstMultiblock.getChunk()).thenReturn(chunkPos);

        // Create second multiblock with same ID
        AbstractNCMultiblock secondMultiblock = mock(AbstractNCMultiblock.class);
        when(secondMultiblock.getId()).thenReturn("same-id");
        when(secondMultiblock.controller()).thenReturn(mockController);
        when(secondMultiblock.getChunk()).thenReturn(chunkPos);

        // Add first multiblock
        MultiblockHandler.instance.addMultiblock(firstMultiblock);

        // Add second multiblock with force=true
        MultiblockHandler.instance.addMultiblock(secondMultiblock, true);

        // Verify second multiblock replaced first one
        HashMap<String, AbstractNCMultiblock> multiblocks =
                (HashMap<String, AbstractNCMultiblock>) multiblockField.get(null);
        assertEquals(secondMultiblock, multiblocks.get("same-id"));
    }

    @Test
    void testAddMultiblock() throws IllegalAccessException {
        // Setup
        ChunkPos chunkPos = new ChunkPos(0, 0);

        when(mockMultiblock.getId()).thenReturn("test-id");
        when(mockMultiblock.controller()).thenReturn(mockController);
        when(mockMultiblock.getChunk()).thenReturn(chunkPos);

        // Action
        MultiblockHandler.instance.addMultiblock(mockMultiblock);

        // Verify
        HashMap<String, AbstractNCMultiblock> multiblocks =
                (HashMap<String, AbstractNCMultiblock>) multiblockField.get(null);

        assertTrue(multiblocks.containsKey("test-id"));
        assertEquals(mockMultiblock, multiblocks.get("test-id"));

        HashMap<Long, List<String>> chunkCache =
                (HashMap<Long, List<String>>) chunkCacheField.get(null);

        assertTrue(chunkCache.containsKey(chunkPos.toLong()));
        assertTrue(chunkCache.get(chunkPos.toLong()).contains("test-id"));
    }

    @Test
    void testAddMultiblockWithNullController() {
        // Only need to stub controller(), the getId() isn't used for this test case
        when(mockMultiblock.controller()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            MultiblockHandler.instance.addMultiblock(mockMultiblock);
        });
    }

    @Test
    void testIgnoreUpdate() throws IllegalAccessException {
        BlockPos pos = new BlockPos(1, 1, 1);
        MultiblockHandler.instance.addIgnoreToUpdate(pos);

        List<BlockPos> ignoreList = (List<BlockPos>) ignoreUpdateField.get(null);
        assertTrue(ignoreList.contains(pos));

        // Adding the same position again should not duplicate it
        MultiblockHandler.instance.addIgnoreToUpdate(pos);
        assertEquals(1, ignoreList.size());
    }

    @Test
    void testTick() throws IllegalAccessException {
        // Create a mock multiblock with valid controller and block entity
        AbstractNCMultiblock multiblock = mock(AbstractNCMultiblock.class);
        MultiblockController controller = mock(MultiblockController.class);
        MultiblockControllerBE blockEntity = mock(MultiblockControllerBE.class);

        when(multiblock.getId()).thenReturn("test-id");
        when(multiblock.controller()).thenReturn(controller);
        when(controller.controllerBE()).thenReturn(blockEntity);
        when(blockEntity.isRemoved()).thenReturn(false);
        when(multiblock.isLoaded()).thenReturn(true);
        when(multiblock.getChunk()).thenReturn(new ChunkPos(0, 0));

        // Add the multiblock
        MultiblockHandler.instance.addMultiblock(multiblock);

        // Call tick
        MultiblockHandler.instance.tick();

        // Verify that tick was called on our multiblock
        verify(multiblock).tick();
    }
}