package igentuman.nc.advancements;

import com.google.gson.JsonObject;
import igentuman.nc.NuclearCraft;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class RadiationContaminationTrigger extends SimpleCriterionTrigger<RadiationContaminationTrigger.Instance> {
    static final ResourceLocation ID = new ResourceLocation(NuclearCraft.MODID, "radiation_contamination");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext ctx) {
        MinMaxBounds.Doubles rads = MinMaxBounds.Doubles.fromJson(json.get("rads"));
        return new Instance(predicate, rads);
    }

    public void trigger(ServerPlayer player, double rads) {
        this.trigger(player, instance -> instance.matches(rads));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Doubles rads;

        public Instance(ContextAwarePredicate predicate, MinMaxBounds.Doubles rads) {
            super(ID, predicate);
            this.rads = rads;
        }

        public boolean matches(double rads) {
            return this.rads.matches(rads);
        }
    }
}
