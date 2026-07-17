package igentuman.nc.content.particles.creator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import igentuman.nc.content.particles.IParticleIngredient;
import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleIngredient;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.network.BasePacketHandler;
import igentuman.nc.util.JsonConstants;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Creator for particle ingredients.
 */
@NothingNullByDefault
public class ParticleIngredientCreator implements IParticleIngredientCreator {

    public static final ParticleIngredientCreator INSTANCE = new ParticleIngredientCreator();

    private ParticleIngredientCreator() {
    }

    @Override
    public ParticleIngredient from(Particle particle, int amount, long meanEnergy, double focus) {
        Objects.requireNonNull(particle, "ParticleIngredients cannot be created from a null particle.");
        if (amount <= 0) {
            throw new IllegalArgumentException("ParticleIngredients must have an amount of at least one. Received size was: " + amount);
        }
        return new SingleParticleIngredient(particle, amount, meanEnergy, focus);
    }

    @Override
    public ParticleIngredient from(JsonElement instance) {
        return deserialize(instance);
    }

    @Override
    public ParticleIngredient read(FriendlyByteBuf buffer) {
        Objects.requireNonNull(buffer, "ParticleIngredients cannot be read from a null packet buffer.");
        return switch (buffer.readEnum(IngredientType.class)) {
            case SINGLE -> {
                String particleName = buffer.readUtf();
                int amount = buffer.readVarInt();
                long meanEnergy = buffer.readLong();
                double focus = buffer.readDouble();
                Particle particle = getParticleFromName(particleName);
                yield from(particle, amount, meanEnergy, focus);
            }
            case MULTI -> createMulti(BasePacketHandler.readArray(buffer, ParticleIngredient[]::new, this::read));
        };
    }

    private Particle getParticleFromName(String name) {
        // This is a placeholder - you'll need to implement the actual lookup logic
        // based on how particles are registered in your mod
        return null; // Replace with actual implementation
    }

    @Override
    public ParticleIngredient deserialize(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Ingredient cannot be null.");
        }
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            int size = jsonArray.size();
            if (size == 0) {
                throw new JsonSyntaxException("Ingredient array cannot be empty, at least one ingredient must be defined.");
            } else if (size > 1) {
                ParticleIngredient[] ingredients = new ParticleIngredient[size];
                for (int i = 0; i < size; i++) {
                    ingredients[i] = deserialize(jsonArray.get(i));
                }
                return createMulti(ingredients);
            }
            json = jsonArray.get(0);
        }
        if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected particle to be object or array of objects.");
        }
        JsonObject jsonObject = json.getAsJsonObject();
        
        if (!jsonObject.has("particle")) {
            throw new JsonSyntaxException("Particle ingredient requires a 'particle' field.");
        }
        
        String particleName = jsonObject.get("particle").getAsString();
        Particle particle = getParticleFromName(particleName);
        
        if (particle == null) {
            throw new JsonSyntaxException("Unknown particle: " + particleName);
        }
        
        int amount = 1;
        if (jsonObject.has("amount")) {
            JsonElement count = jsonObject.get("amount");
            if (!GsonHelper.isNumberValue(count)) {
                throw new JsonSyntaxException("Expected amount to be a number that is one or larger.");
            }
            amount = count.getAsJsonPrimitive().getAsInt();
            if (amount < 1) {
                throw new JsonSyntaxException("Expected amount to larger than or equal to one.");
            }
        }
        
        long meanEnergy = 0;
        if (jsonObject.has("meanEnergy")) {
            JsonElement energy = jsonObject.get("meanEnergy");
            if (!GsonHelper.isNumberValue(energy)) {
                throw new JsonSyntaxException("Expected meanEnergy to be a number.");
            }
            meanEnergy = energy.getAsJsonPrimitive().getAsLong();
        }
        
        double focus = 0;
        if (jsonObject.has("focus")) {
            JsonElement focusElement = jsonObject.get("focus");
            if (!GsonHelper.isNumberValue(focusElement)) {
                throw new JsonSyntaxException("Expected focus to be a number.");
            }
            focus = focusElement.getAsJsonPrimitive().getAsDouble();
        }
        
        return from(particle, amount, meanEnergy, focus);
    }

    @Override
    public ParticleIngredient createMulti(ParticleIngredient... ingredients) {
        Objects.requireNonNull(ingredients, "Cannot create a multi ingredient out of a null array.");
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create a multi ingredient out of no ingredients.");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        List<ParticleIngredient> cleanedIngredients = new ArrayList<>();
        for (ParticleIngredient ingredient : ingredients) {
            if (ingredient instanceof MultiParticleIngredient multi) {
                Collections.addAll(cleanedIngredients, multi.ingredients);
            } else {
                cleanedIngredients.add(ingredient);
            }
        }
        return new MultiParticleIngredient(cleanedIngredients.toArray(new ParticleIngredient[0]));
    }

    @Override
    public ParticleIngredient from(Stream<ParticleIngredient> ingredients) {
        return createMulti(ingredients.toArray(ParticleIngredient[]::new));
    }

    @Override
    public ParticleIngredient from(Particle particle, int amount) {
        return from(particle, amount, 0, 0);
    }

    /**
     * Single particle ingredient implementation.
     */
    @NothingNullByDefault
    public static class SingleParticleIngredient extends ParticleIngredient {

        private final Particle particle;
        private final long meanEnergy;
        private final double focus;

        private SingleParticleIngredient(Particle particle, int amount, long meanEnergy, double focus) {
            this.particle = Objects.requireNonNull(particle);
            this.amount = amount;
            this.meanEnergy = meanEnergy;
            this.focus = focus;
        }

        @Override
        public boolean test(ParticleStack stack) {
            return testType(stack) && stack.getAmount() >= amount;
        }

        @Override
        public boolean testType(@NotNull ParticleStack stack) {
            if (stack.getParticle() != particle) {
                return false;
            }
            
            // If meanEnergy is specified, check if the stack's energy is at least that much
            if (meanEnergy > 0 && stack.getMeanEnergy() < meanEnergy) {
                return false;
            }
            
            // If focus is specified, check if the stack's focus is at least that much
            if (focus > 0 && stack.getFocus() < focus) {
                return false;
            }
            
            return true;
        }

        @Override
        public ParticleStack getMatchingInstance(ParticleStack stack) {
            return test(stack) ? new ParticleStack(stack.getParticle(), amount, stack.getMeanEnergy(), stack.getFocus()) : new ParticleStack();
        }

        @Override
        public long getNeededAmount(ParticleStack stack) {
            return testType(stack) ? amount : 0;
        }

        @Override
        public boolean hasNoMatchingInstances() {
            return particle == null;
        }

        @Override
        public List<@NotNull ParticleStack> getRepresentations() {
            return List.of(new ParticleStack(particle, amount, meanEnergy, focus));
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeEnum(IngredientType.SINGLE);
            buffer.writeUtf(particle.getName());
            buffer.writeVarInt(amount);
            buffer.writeLong(meanEnergy);
            buffer.writeDouble(focus);
        }

        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("particle", particle.getName());
            if (amount > 1) {
                json.addProperty("amount", amount);
            }
            if (meanEnergy > 0) {
                json.addProperty("meanEnergy", meanEnergy);
            }
            if (focus > 0) {
                json.addProperty("focus", focus);
            }
            return json;
        }

        @Override
        public String getName() {
            return particle.getName();
        }

        @Override
        public int getAmount() {
            return amount;
        }
        
        public Particle getParticle() {
            return particle;
        }
        
        public long getMeanEnergy() {
            return meanEnergy;
        }
        
        public double getFocus() {
            return focus;
        }
    }

    /**
     * Multi particle ingredient implementation.
     */
    @NothingNullByDefault
    public static class MultiParticleIngredient extends ParticleIngredient {

        private final ParticleIngredient[] ingredients;

        private MultiParticleIngredient(ParticleIngredient... ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public boolean test(ParticleStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(@NotNull ParticleStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
        }

        @Override
        public ParticleStack getMatchingInstance(ParticleStack stack) {
            for (ParticleIngredient ingredient : ingredients) {
                ParticleStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (matchingInstance.getParticle() != null) {
                    return matchingInstance;
                }
            }
            return new ParticleStack();
        }

        @Override
        public long getNeededAmount(ParticleStack stack) {
            for (ParticleIngredient ingredient : ingredients) {
                long amount = ingredient.getNeededAmount(stack);
                if (amount > 0) {
                    return amount;
                }
            }
            return 0;
        }

        @Override
        public boolean hasNoMatchingInstances() {
            return Arrays.stream(ingredients).allMatch(IParticleIngredient::hasNoMatchingInstances);
        }

        @Override
        public List<@NotNull ParticleStack> getRepresentations() {
            List<@NotNull ParticleStack> representations = new ArrayList<>();
            for (ParticleIngredient ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeEnum(IngredientType.MULTI);
            BasePacketHandler.writeArray(buffer, ingredients, IParticleIngredient::write);
        }

        @Override
        public JsonElement serialize() {
            JsonArray json = new JsonArray();
            for (ParticleIngredient ingredient : ingredients) {
                json.add(ingredient.serialize());
            }
            return json;
        }

        @Override
        public String getName() {
            return getRepresentations().get(0).getParticle().getName();
        }

        @Override
        public int getAmount() {
            return getRepresentations().get(0).getAmount();
        }
        
        public List<ParticleIngredient> getIngredients() {
            return List.of(ingredients);
        }
        
        public boolean forEachIngredient(Predicate<ParticleIngredient> checker) {
            boolean result = false;
            for (ParticleIngredient ingredient : ingredients) {
                result |= checker.test(ingredient);
            }
            return result;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return Arrays.equals(ingredients, ((MultiParticleIngredient) o).ingredients);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(ingredients);
        }
    }

    private enum IngredientType {
        SINGLE,
        MULTI
    }
}