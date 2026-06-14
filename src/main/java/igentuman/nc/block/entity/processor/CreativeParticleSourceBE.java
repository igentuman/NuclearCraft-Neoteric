package igentuman.nc.block.entity.processor;

import igentuman.nc.block.accelerator.entity.AcceleratorBeamPortBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.Equations;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.atomic.AtomicBoolean;

import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;

public class CreativeParticleSourceBE extends NCProcessorBE {

    @NBTField
    public String selectedParticle = "";
    @NBTField
    public double particleFocus = 1;
    @NBTField
    public double particleEnergy = 1;
    @NBTField
    public int energyScale = 1; // 0=keV, 1=MeV, 2=GeV, 3=TeV

    public CreativeParticleSourceBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.CREATIVE_PARTICLE_SOURCE);
    }

    @Override
    public void tickServer() {
        ParticleStack particleStack = getConfiguredStack();
        if(particleStack == null || particleStack.getParticle() == null) return;
        extractParticle(particleStack);
    }

    public void updateBeamSettings(String particle, double focus, double energy, int scale) {
        selectedParticle = particle;
        particleFocus = focus;
        particleEnergy = energy;
        energyScale = Math.max(0, Math.min(3, scale));
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public long meanEnergy() {
        long multiplier = (long) Math.pow(10, 3 * (energyScale + 1))/1000;
        return (long) (particleEnergy * multiplier);
    }

    public ParticleStack getConfiguredStack() {
        if (selectedParticle.isEmpty() || Particles.getParticleFromName(selectedParticle) == null) {
            return ParticleStack.EMPTY;
        }
        return new ParticleStack(Particles.getParticleFromName(selectedParticle), 10000, meanEnergy(), particleFocus);
    }

    public boolean extractParticle(ParticleStack particleStack) {
        AtomicBoolean result = new AtomicBoolean(false);
        Direction facing = getFacing().getOpposite();
        BlockPos currentPos = worldPosition.relative(facing);
        int maxDistance = 16;

        for (int distance = 0; distance < maxDistance; distance++) {

            BlockState blockState = level.getBlockState(currentPos);
            if (blockState.is(ACCELERATOR_BLOCKS.get("particle_beam").get())) {
                currentPos = currentPos.relative(facing);
                continue;
            }

            if (level.getBlockEntity(currentPos) instanceof AcceleratorBeamPortBE targetPort) {
                if (targetPort.getFacing() == facing.getOpposite()) {
                    if (targetPort.controller() != null) {
                        particleStack.addFocus(-Equations.focusLoss(distance, particleStack));
                        targetPort.controller().getCapability(PARTICLE_HANDLER_CAPABILITY, facing.getOpposite())
                                .ifPresent(handler -> {
                                    handler.reciveParticle(facing.getOpposite(), particleStack);
                                    result.set(true);
                                });
                    }
                }
                break;
            } else if (level.getBlockEntity(currentPos) instanceof TargetChamberBeamPortBE targetPort) {
                if (targetPort.getFacing() == facing.getOpposite()) {
                    if (targetPort.controller() != null) {
                        particleStack.addFocus(-Equations.focusLoss(distance, particleStack));
                        targetPort.getCapability(PARTICLE_HANDLER_CAPABILITY, facing.getOpposite())
                                .ifPresent(handler -> {
                                    handler.reciveParticle(facing.getOpposite(), particleStack);
                                    result.set(true);
                                });
                    }
                }
                break;
            } else {
                break;
            }
        }
        return result.get();
    }

    @NothingNullByDefault
    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id,
                      ItemStackIngredient[] input, ItemStackIngredient[] output,
                      FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                      double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, 1);
        }

        @Override
        public String getCodeId() {
            return Processors.CREATIVE_PARTICLE_SOURCE;
        }
    }
}
