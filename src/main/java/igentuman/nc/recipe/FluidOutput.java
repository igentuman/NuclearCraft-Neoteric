package igentuman.nc.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/** A processor fluid output: a concrete fluid or a fluid tag resolved to one stack at production time, with an amount. */
public record FluidOutput(@Nullable Fluid fluid, @Nullable TagKey<Fluid> tag, int amount) {

    public static FluidOutput of(Fluid fluid, int amount) {
        return new FluidOutput(fluid, null, amount);
    }

    public static FluidOutput of(TagKey<Fluid> tag, int amount) {
        return new FluidOutput(null, tag, amount);
    }

    public boolean isTag() {
        return tag != null;
    }

    /** One concrete stack (tag → highest-priority member); EMPTY if unresolvable. */
    public FluidStack resolve() {
        if (tag != null) return TagOutputResolver.resolveFluid(tag, amount);
        if (fluid != null) return new FluidStack(fluid, amount);
        return FluidStack.EMPTY;
    }

    /** All candidate stacks for display: concrete → singleton, tag → members in priority order. */
    public List<FluidStack> members() {
        if (tag != null) return TagOutputResolver.membersFluid(tag, amount);
        FluidStack stack = resolve();
        return stack.isEmpty() ? List.of() : List.of(stack);
    }

    /** A tag output is complete only if it resolves to at least one member. */
    public boolean isComplete() {
        if (tag != null) return !TagOutputResolver.membersFluid(tag, amount).isEmpty();
        return fluid != null && amount > 0;
    }

    public static final Codec<FluidOutput> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fluid").forGetter(o -> Optional.ofNullable(o.fluid)),
            TagKey.codec(Registries.FLUID).optionalFieldOf("tag").forGetter(o -> Optional.ofNullable(o.tag)),
            Codec.INT.optionalFieldOf("amount", 1000).forGetter(FluidOutput::amount)
    ).apply(inst, (fluid, tag, amount) -> new FluidOutput(fluid.orElse(null), tag.orElse(null), amount)));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidOutput> STREAM_CODEC = StreamCodec.of(
            (buf, o) -> {
                buf.writeBoolean(o.isTag());
                if (o.isTag()) {
                    buf.writeResourceLocation(o.tag.location());
                } else {
                    buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(o.fluid == null ? Fluids.EMPTY : o.fluid));
                }
                buf.writeVarInt(o.amount);
            },
            buf -> {
                boolean isTag = buf.readBoolean();
                ResourceLocation rl = buf.readResourceLocation();
                int amount = buf.readVarInt();
                return isTag
                        ? FluidOutput.of(TagKey.create(Registries.FLUID, rl), amount)
                        : new FluidOutput(BuiltInRegistries.FLUID.get(rl), null, amount);
            }
    );
}
