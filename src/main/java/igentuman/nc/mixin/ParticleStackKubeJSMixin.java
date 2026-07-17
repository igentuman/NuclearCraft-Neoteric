package igentuman.nc.mixin;

import igentuman.nc.content.particles.ParticleStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ParticleStack.class, remap = false)
public class ParticleStackKubeJSMixin {
    
    public ParticleStack kjs$withCount(int count) {
        ParticleStack self = (ParticleStack) (Object) this;
        ParticleStack copy = self.copy();
        copy.setAmount(count);
        return copy;
    }
    
    public String kjs$getId() {
        ParticleStack self = (ParticleStack) (Object) this;
        if (self.getParticle() != null) {
            return self.getParticle().getName();
        }
        return "empty";
    }
    
    public int kjs$getCount() {
        ParticleStack self = (ParticleStack) (Object) this;
        return self.getAmount();
    }
}