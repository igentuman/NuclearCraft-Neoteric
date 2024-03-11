package igentuman.nc.recipes.ingredient.creator;

import com.google.gson.*;
import igentuman.nc.NuclearCraft;
import igentuman.nc.network.BasePacketHandler;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.IMultiIngredient;
import igentuman.nc.recipes.ingredient.InputIngredient;
import igentuman.nc.util.SerializerHelper;
import igentuman.nc.util.TagUtil;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@NothingNullByDefault
public class FluidStackIngredientCreator implements IFluidStackIngredientCreator {

    public static final FluidStackIngredientCreator INSTANCE = new FluidStackIngredientCreator();

    private FluidStackIngredientCreator() {
    }

    @Override
    public FluidStackIngredient from(FluidStack instance) {
        Objects.requireNonNull(instance, "FluidStackIngredients cannot be created from a null FluidStack.");
        if (instance.isEmpty()) {
            throw new IllegalArgumentException("FluidStackIngredients cannot be created using the empty stack.");
        }
        //Copy the stack to ensure it doesn't get modified afterwards
        return new SingleFluidStackIngredient(instance.copy());
    }

    @Override
    public FluidStackIngredient from(Tags.IOptionalNamedTag<Fluid> tag, int amount) {
        Objects.requireNonNull(tag, "FluidStackIngredients cannot be created from a null tag.");
        if (amount <= 0) {
            throw new IllegalArgumentException("FluidStackIngredients must have an amount of at least one. Received size was: " + amount);
        }
        return new TaggedFluidStackIngredient(tag, amount);
    }

    @Override
    public FluidStackIngredient read(PacketBuffer buffer) {
        Objects.requireNonNull(buffer, "FluidStackIngredients cannot be read from a null packet buffer.");
        switch (buffer.readEnum(IngredientType.class)) {
            case SINGLE:
                return from(FluidStack.readFromPacket(buffer));
            //case TAGGED -> from(Tags.IOptionalNamedTag<Fluid>().create(buffer.readResourceLocation()), buffer.readVarInt());
            case MULTI:
                return createMulti(BasePacketHandler.readArray(buffer, FluidStackIngredient[]::new, this::read));
        };
        return null;
    }

    @Override
    public FluidStackIngredient deserialize(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Ingredient cannot be null.");
        }
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            int size = jsonArray.size();
            if (size == 0) {
                throw new JsonSyntaxException("Ingredient array cannot be empty, at least one ingredient must be defined.");
            } else if (size > 1) {
                FluidStackIngredient[] ingredients = new FluidStackIngredient[size];
                for (int i = 0; i < size; i++) {
                    //Read all the ingredients
                    ingredients[i] = deserialize(jsonArray.get(i));
                }
                return createMulti(ingredients);
            }
            //If we only have a single element, just set our json as that so that we don't have to use Multi for efficiency reasons
            json = jsonArray.get(0);
        }
        if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected fluid to be object or array of objects.");
        }
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("fluid") && jsonObject.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an fluid, not both.");
        } else if (jsonObject.has("fluid")) {
            FluidStack stack = SerializerHelper.deserializeFluid(jsonObject);
            if (stack.isEmpty()) {
                throw new JsonSyntaxException("Unable to create an ingredient from an empty stack.");
            }
            return from(stack);
        } else if (jsonObject.has("tag")) {
            if (!jsonObject.has("amount")) {
                throw new JsonSyntaxException("Expected to receive a amount that is greater than zero.");
            }
            JsonElement count = jsonObject.get("amount");
            if (!JSONUtils.isNumberValue(count)) {
                throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
            }
            int amount = count.getAsJsonPrimitive().getAsInt();
            if (amount < 1) {
                throw new JsonSyntaxException("Expected amount to be greater than zero.");
            }
            ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getAsString(jsonObject, "tag"));
            Tags.IOptionalNamedTag<Fluid> key = TagUtil.createFluidTagKey(resourceLocation);
            return from(key, amount);
        }
        throw new JsonSyntaxException("Expected to receive a resource location representing either a tag or a fluid.");
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Converts a stream of ingredients into a single ingredient by converting the stream to an array and calling {@link #createMulti(FluidStackIngredient[])}.
     */
    @Override
    public FluidStackIngredient createMulti(FluidStackIngredient... ingredients) {
        Objects.requireNonNull(ingredients, "Cannot create a multi ingredient out of a null array.");
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create a multi ingredient out of no ingredients.");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        List<FluidStackIngredient> cleanedIngredients = new ArrayList<>();
        for (FluidStackIngredient ingredient : ingredients) {
            if (ingredient instanceof MultiFluidStackIngredient) {
                //Don't worry about if our inner ingredients are multi as well, as if this is the only external method for
                // creating a multi ingredient, then we are certified they won't be of a higher depth
                Collections.addAll(cleanedIngredients, ((MultiFluidStackIngredient)ingredient).ingredients);
            } else {
                cleanedIngredients.add(ingredient);
            }
        }
        //There should be more than a single fluid, or we would have split out earlier
        return new MultiFluidStackIngredient(cleanedIngredients.toArray(new FluidStackIngredient[0]));
    }

    @Override
    public FluidStackIngredient from(Stream<FluidStackIngredient> ingredients) {
        return createMulti(ingredients.toArray(FluidStackIngredient[]::new));
    }

    @NothingNullByDefault
    public static class SingleFluidStackIngredient extends FluidStackIngredient {

        private final FluidStack fluidInstance;

        private SingleFluidStackIngredient(FluidStack fluidInstance) {
            this.fluidInstance = Objects.requireNonNull(fluidInstance);
        }

        @Override
        public String getName() {
            return fluidInstance.getFluid().getRegistryName().getPath().toString().replace(":", "_");
        }


        @Override
        public boolean test(FluidStack fluidStack) {
            return testType(fluidStack) && fluidStack.getAmount() >= fluidInstance.getAmount();
        }

        @Override
        public boolean testType(FluidStack fluidStack) {
            return Objects.requireNonNull(fluidStack).isFluidEqual(fluidInstance);
        }

        @Override
        public FluidStack getMatchingInstance(FluidStack fluidStack) {
            return test(fluidStack) ? fluidInstance.copy() : FluidStack.EMPTY;
        }

        @Override
        public long getNeededAmount(FluidStack stack) {
            return testType(stack) ? fluidInstance.getAmount() : 0;
        }

        @Override
        public boolean hasNoMatchingInstances() {
            return false;
        }

        @Override
        public List<FluidStack> getRepresentations() {
            return Collections.singletonList(fluidInstance);
        }

        /**
         * For use in recipe input caching. Do not use this to modify the backing stack.
         */
        public FluidStack getInputRaw() {
            return fluidInstance;
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnum(IngredientType.SINGLE);
            fluidInstance.writeToPacket(buffer);
        }

        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("amount", fluidInstance.getAmount());
            json.addProperty("fluid", fluidInstance.getFluid().getRegistryName().getPath().toString());
            if (fluidInstance.hasTag()) {
                json.addProperty("nbt", fluidInstance.getTag().toString());
            }
            return json;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SingleFluidStackIngredient other = (SingleFluidStackIngredient) o;
            //Need to use this over equals to ensure we compare amounts
            return fluidInstance.isFluidStackIdentical(other.fluidInstance);
        }

        @Override
        public int hashCode() {
            return fluidInstance.hashCode();
        }
    }

    @NothingNullByDefault
    public static class TaggedFluidStackIngredient extends FluidStackIngredient {

        protected final Tags.IOptionalNamedTag<Fluid> tag;

        private TaggedFluidStackIngredient(Tags.IOptionalNamedTag<Fluid> tag, int amount) {
            this.tag = tag;
            this.amount = amount;
        }


        public String getName()
        {
            return tag.getName().toString();
        }

        @Override
        public boolean test(FluidStack fluidStack) {
            return testType(fluidStack) && fluidStack.getAmount() >= amount;
        }

        @Override
        public boolean testType(FluidStack fluidStack) {
            return tag.contains(Objects.requireNonNull(fluidStack).getFluid());
        }

        @Override
        public FluidStack getMatchingInstance(FluidStack fluidStack) {
            if (test(fluidStack)) {
                //Our fluid is in the tag, so we make a new stack with the given amount
                return new FluidStack(fluidStack, amount);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public long getNeededAmount(FluidStack stack) {
            return testType(stack) ? amount : 0;
        }

        @Override
        public boolean hasNoMatchingInstances() {
            return tag.getValues().isEmpty();
        }

        @Override
        public List<FluidStack> getRepresentations() {
            List<FluidStack> representations = new ArrayList<>();
            for (Fluid fluid : tag.getValues()) {
                representations.add(new FluidStack(fluid, amount));
            }
            if(representations.isEmpty()) {
                NuclearCraft.LOGGER.error("Fluid Tag {} is empty!", tag.getName());
            }
            return representations;
        }

        /**
         * For use in recipe input caching.
         */
        public Iterable<Fluid> getRawInput() {
            return tag.getValues();
        }

        @Override
        public void write(PacketBuffer buffer) {
           // buffer.writeEnum(IngredientType.TAGGED);
            buffer.writeResourceLocation(tag.getName());
            buffer.writeVarInt(amount);
        }

        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("amount", amount);
            json.addProperty("tag", tag.getName().toString());
            return json;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TaggedFluidStackIngredient other = (TaggedFluidStackIngredient) o;
            return amount == other.amount && tag.equals(other.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tag, amount);
        }
    }

    @NothingNullByDefault
    public static class MultiFluidStackIngredient extends FluidStackIngredient implements IMultiIngredient<FluidStack, FluidStackIngredient> {

        private final FluidStackIngredient[] ingredients;

        private MultiFluidStackIngredient(FluidStackIngredient... ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public boolean test(FluidStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(FluidStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
        }

        @Override
        public FluidStack getMatchingInstance(FluidStack stack) {
            for (FluidStackIngredient ingredient : ingredients) {
                FluidStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public long getNeededAmount(FluidStack stack) {
            for (FluidStackIngredient ingredient : ingredients) {
                long amount = ingredient.getNeededAmount(stack);
                if (amount > 0) {
                    return amount;
                }
            }
            return 0;
        }

        @Override
        public boolean hasNoMatchingInstances() {
            return Arrays.stream(ingredients).allMatch(InputIngredient::hasNoMatchingInstances);
        }

        @Override
        public List<FluidStack> getRepresentations() {
            List<FluidStack> representations = new ArrayList<>();
            for (FluidStackIngredient ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }

        @Override
        public boolean forEachIngredient(Predicate<FluidStackIngredient> checker) {
            boolean result = false;
            for (FluidStackIngredient ingredient : ingredients) {
                result |= checker.test(ingredient);
            }
            return result;
        }

        @Override
        public final List<FluidStackIngredient> getIngredients() {
            return Arrays.asList(ingredients);
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnum(IngredientType.MULTI);
            BasePacketHandler.writeArray(buffer, ingredients, InputIngredient::write);
        }

        @Override
        public JsonElement serialize() {
            JsonArray json = new JsonArray();
            for (FluidStackIngredient ingredient : ingredients) {
                json.add(ingredient.serialize());
            }
            return json;
        }

        @Override
        public String getName() {
            return getRepresentations().get(0).getFluid().getRegistryName().getPath().toString().split(":")[1];
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return Arrays.equals(ingredients, ((MultiFluidStackIngredient) o).ingredients);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(ingredients);
        }
    }

    private enum IngredientType {
        SINGLE,
     //   TAGGED,
        MULTI
    }
}