package igentuman.nc.compat.emi;

import dev.emi.emi.api.stack.EmiStack;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.setup.Registers;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class ParticleEmiStack extends EmiStack {

    private final ParticleStack particleStack;

    public ParticleEmiStack(ParticleStack particleStack) {
        this.particleStack = particleStack;
        this.amount = particleStack.amount();
    }

    public ParticleStack getParticleStack() {
        return particleStack;
    }

    public static ParticleEmiStack of(ParticleStack particleStack) {
        return new ParticleEmiStack(particleStack);
    }

    @Override
    public EmiStack copy() {
        return new ParticleEmiStack(particleStack);
    }

    @Override
    public boolean isEmpty() {
        return particleStack.isEmpty();
    }

    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public Object getKey() {
        return particleStack.isEmpty() ? "empty" : particleStack.particleId();
    }

    @Override
    public ResourceLocation getId() {
        return particleStack.isEmpty() ? rl("empty") : rl("particle/" + particleStack.particleId().getPath());
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta, int flags) {
        if (particleStack.isEmpty()) return;
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(particleStack.particleId());
        if (definition == null) return;
        graphics.blit(definition.textureId(), x, y, 0, 0, 16, 16, 16, 16);
    }

    @Override
    public List<Component> getTooltipText() {
        List<Component> tooltip = new ArrayList<>();
        if (particleStack.isEmpty()) return tooltip;
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(particleStack.particleId());
        tooltip.add(definition == null
                ? Component.literal(particleStack.particleId().getPath())
                : Component.translatable(definition.translationKey()));
        if (particleStack.amount() > 0) {
            tooltip.add(__("tooltip.nuclearcraft.particlestack.amount", TextUtils.scaledFormat(particleStack.amount()))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (particleStack.meanEnergyKeV() > 0) {
            tooltip.add(__("tooltip.nuclearcraft.particlestack.energy", TextUtils.formatParticleEnergy(particleStack.meanEnergyKeV()))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (particleStack.focus() > 0) {
            tooltip.add(__("tooltip.nuclearcraft.particlestack.focus", TextUtils.numberFormat(particleStack.focus()))
                    .withStyle(ChatFormatting.GRAY));
        }
        return tooltip;
    }

    @Override
    public Component getName() {
        if (particleStack.isEmpty()) return Component.literal("Empty");
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(particleStack.particleId());
        return definition == null ? Component.literal(particleStack.particleId().getPath()) : Component.translatable(definition.translationKey());
    }

}
