package igentuman.nc.compat.emi.ingredient;

import dev.emi.emi.api.stack.EmiStack;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.util.Units;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

/**
 * EMI implementation of ParticleStack
 * Based on the JEI implementation but adapted for EMI's EmiStack system
 */
public class ParticleEmiStack extends EmiStack {
    
    private final ParticleStack particleStack;
    
    public ParticleEmiStack(ParticleStack particleStack) {
        this.particleStack = particleStack.copy();
        this.amount = particleStack.getAmount();
    }
    
    public ParticleEmiStack(ParticleStack particleStack, long amount) {
        this.particleStack = particleStack.copy();
        this.particleStack.setAmount((int) amount);
        this.amount = amount;
    }
    
    public ParticleStack getParticleStack() {
        return particleStack;
    }
    
    @Override
    public EmiStack copy() {
        return new ParticleEmiStack(particleStack, amount);
    }
    
    @Override
    public boolean isEmpty() {
        return particleStack == null || particleStack.getParticle() == null || particleStack.getAmount() <= 0;
    }
    
    @Override
    public Object getKey() {
        return particleStack.getParticle();
    }
    
    @Override
    public ResourceLocation getId() {
        if (particleStack.getParticle() != null) {
            return rl(particleStack.getParticle().getName());
        }
        return rl("empty");
    }
    
    @Override
    public net.minecraft.nbt.CompoundTag getNbt() {
        return null; // ParticleStack doesn't use NBT data
    }
    
    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int x, int y, float delta, int flags) {
        ParticleEmiStackRenderer.render(this, graphics, x, y, delta, flags);
    }
    
    @Override
    public List<Component> getTooltipText() {
        if (isEmpty()) {
            return List.of();
        }
        
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(__(particleStack.getParticle().getUnlocalizedName()));
        tooltip.add(__("tooltip.nuclearcraft.particlestack.amount", Units.getSIFormat(particleStack.getAmount(),"pu")).withStyle(ChatFormatting.GRAY));
        
        if (particleStack.getMeanEnergy() > 0) {
            tooltip.add(__("tooltip.nuclearcraft.particlestack.mean_energy", Units.getParticleEnergy(particleStack.getMeanEnergy())).withStyle(ChatFormatting.GRAY));
        }
        
        if (particleStack.getFocus() > 0) {
            tooltip.add(__("tooltip.nuclearcraft.particlestack.focus", Units.getSIFormat(particleStack.getFocus(), "")).withStyle(ChatFormatting.GRAY));
        }
        
        return tooltip;
    }
    
    @Override
    public List<ClientTooltipComponent> getTooltip() {
        java.util.List<ClientTooltipComponent> tooltip = new java.util.ArrayList<>();
        tooltip.add(ClientTooltipComponent.create(__(particleStack.getParticle().getUnlocalizedName()).getVisualOrderText()));
        tooltip.add(ClientTooltipComponent.create(__("tooltip.nuclearcraft.particlestack.amount", Units.getSIFormat(particleStack.getAmount(),"pu")).withStyle(ChatFormatting.GRAY).getVisualOrderText()));

        if (particleStack.getMeanEnergy() > 0) {
            tooltip.add(ClientTooltipComponent.create(__("tooltip.nuclearcraft.particlestack.mean_energy", Units.getParticleEnergy(particleStack.getMeanEnergy())).withStyle(ChatFormatting.GRAY).getVisualOrderText()));
        }

        if (particleStack.getFocus() > 0) {
            tooltip.add(ClientTooltipComponent.create(__("tooltip.nuclearcraft.particlestack.focus", Units.getSIFormat(particleStack.getFocus(), "")).withStyle(ChatFormatting.GRAY).getVisualOrderText()));
        }

        return tooltip;
    }
    

    
    @Override
    public Component getName() {
        if (isEmpty()) {
            return Component.literal("Empty");
        }
        return __(particleStack.getParticle().getUnlocalizedName());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ParticleEmiStack other)) return false;
        
        if (isEmpty() && other.isEmpty()) return true;
        if (isEmpty() || other.isEmpty()) return false;
        
        return particleStack.getParticle().equals(other.particleStack.getParticle()) &&
               particleStack.getAmount() == other.particleStack.getAmount() &&
               particleStack.getMeanEnergy() == other.particleStack.getMeanEnergy() &&
               Double.compare(particleStack.getFocus(), other.particleStack.getFocus()) == 0;
    }
    
    @Override
    public int hashCode() {
        if (isEmpty()) return 0;
        return particleStack.getParticle().hashCode();
    }
    
    @Override
    public String toString() {
        if (isEmpty()) {
            return "Empty ParticleStack";
        }
        return particleStack.getParticle().getName() + " x" + getAmount();
    }
    
    /**
     * Factory method to create ParticleEmiStack from ParticleStack
     */
    public static ParticleEmiStack of(ParticleStack particleStack) {
        return new ParticleEmiStack(particleStack);
    }
    
    /**
     * Factory method to create ParticleEmiStack from ParticleStack with specific amount
     */
    public static ParticleEmiStack of(ParticleStack particleStack, long amount) {
        return new ParticleEmiStack(particleStack, amount);
    }
}