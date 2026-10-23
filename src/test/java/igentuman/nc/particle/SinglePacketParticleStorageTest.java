package igentuman.nc.particle;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SinglePacketParticleStorageTest {

    @Test
    void acceptsOnlyTheFirstPacketUntilCleared() {
        SinglePacketParticleStorage input = new SinglePacketParticleStorage(100);
        ParticlePortView firstPort = new ParticlePortView(input, 0, ParticlePortView.Access.INPUT);
        ParticlePortView secondPort = new ParticlePortView(input, 0, ParticlePortView.Access.INPUT);
        ParticleStack first = new ParticleStack(ResourceLocation.fromNamespaceAndPath("nuclearcraft", "alpha"),
                10, 1000, 1);
        ParticleStack second = new ParticleStack(ResourceLocation.fromNamespaceAndPath("nuclearcraft", "alpha"),
                20, 1000, 1);

        assertEquals(ParticleStack.EMPTY, firstPort.insert(0, first, ParticleAction.SIMULATE));
        assertEquals(ParticleStack.EMPTY, firstPort.insert(0, first, ParticleAction.EXECUTE));
        assertEquals(second, secondPort.insert(0, second, ParticleAction.SIMULATE));
        assertEquals(second, secondPort.insert(0, second, ParticleAction.EXECUTE));
        assertEquals(first, input.snapshot(0));

        assertEquals(first, input.extract(0, Long.MAX_VALUE, ParticleAction.EXECUTE));
        assertEquals(ParticleStack.EMPTY, secondPort.insert(0, second, ParticleAction.EXECUTE));
        assertEquals(second, input.snapshot(0));
    }
}
