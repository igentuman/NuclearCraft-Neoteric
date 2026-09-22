package igentuman.nc.api.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record ParticleIngredient(
        Set<ResourceLocation> species,
        long amount,
        long minimumEnergyKeV,
        long maximumEnergyKeV,
        double minimumFocus
) {

    public ParticleIngredient {
        if (species == null || species.isEmpty()) {
            throw new IllegalArgumentException("Particle ingredient requires at least one species");
        }
        species = Set.copyOf(species);
        if (amount <= 0) {
            throw new IllegalArgumentException("Particle ingredient amount must be positive: " + amount);
        }
        if (minimumEnergyKeV < 0) {
            throw new IllegalArgumentException("Particle ingredient minimum energy must not be negative: " + minimumEnergyKeV);
        }
        if (maximumEnergyKeV < minimumEnergyKeV) {
            throw new IllegalArgumentException("Particle ingredient maximum energy must not be below minimum: [" + minimumEnergyKeV + ", " + maximumEnergyKeV + "]");
        }
        if (!Double.isFinite(minimumFocus) || minimumFocus < 0) {
            throw new IllegalArgumentException("Particle ingredient minimum focus must be finite and non-negative: " + minimumFocus);
        }
    }

    public boolean test(ParticleStack candidate) {
        if (candidate.isEmpty()) {
            return false;
        }
        if (!species.contains(candidate.particleId())) {
            return false;
        }
        if (candidate.amount() < amount) {
            return false;
        }
        if (candidate.meanEnergyKeV() < minimumEnergyKeV || candidate.meanEnergyKeV() > maximumEnergyKeV) {
            return false;
        }
        return candidate.focus() >= minimumFocus;
    }

    private static final Codec<List<ResourceLocation>> SPECIES_LIST_CODEC = ParticleStack.PARTICLE_ID_CODEC.listOf().comapFlatMap(
            list -> list.isEmpty()
                    ? DataResult.error(() -> "Particle ingredient requires at least one species")
                    : DataResult.success(list),
            list -> list
    );

    public static final Codec<ParticleIngredient> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            SPECIES_LIST_CODEC.fieldOf("species").forGetter(i -> List.copyOf(i.species())),
            Codec.LONG.fieldOf("amount").forGetter(ParticleIngredient::amount),
            Codec.LONG.optionalFieldOf("minimum_energy", 0L).forGetter(ParticleIngredient::minimumEnergyKeV),
            Codec.LONG.fieldOf("maximum_energy").forGetter(ParticleIngredient::maximumEnergyKeV),
            Codec.DOUBLE.optionalFieldOf("minimum_focus", 0.0).forGetter(ParticleIngredient::minimumFocus)
    ).apply(inst, (species, amount, minEnergy, maxEnergy, minFocus) ->
            new ParticleIngredient(new LinkedHashSet<>(species), amount, minEnergy, maxEnergy, minFocus)));

    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleIngredient> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), i -> List.copyOf(i.species()),
            ByteBufCodecs.VAR_LONG, ParticleIngredient::amount,
            ByteBufCodecs.VAR_LONG, ParticleIngredient::minimumEnergyKeV,
            ByteBufCodecs.VAR_LONG, ParticleIngredient::maximumEnergyKeV,
            ByteBufCodecs.DOUBLE, ParticleIngredient::minimumFocus,
            (species, amount, minEnergy, maxEnergy, minFocus) ->
                    new ParticleIngredient(new LinkedHashSet<>(species), amount, minEnergy, maxEnergy, minFocus)
    );
}
