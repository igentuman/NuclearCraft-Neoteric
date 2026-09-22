package igentuman.nc.particle;

import igentuman.nc.api.particle.ParticleDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ParticleCatalogValidator {

    private ParticleCatalogValidator() {
    }

    public record Report(List<Problem> problems) {
        public boolean valid() {
            return problems.isEmpty();
        }
    }

    public record Problem(ResourceLocation particleId, String fieldPath, String message) {
    }

    public static Report validate(Map<ResourceLocation, ParticleDefinition> definitions) {
        List<Problem> problems = new ArrayList<>();

        for (Map.Entry<ResourceLocation, ParticleDefinition> entry : definitions.entrySet()) {
            ResourceLocation id = entry.getKey();
            ParticleDefinition definition = entry.getValue();

            if (!definitions.containsKey(definition.antiparticleId())) {
                problems.add(new Problem(id, "antiparticleId",
                        "references unknown particle " + definition.antiparticleId()));
            } else {
                ParticleDefinition antiparticle = definitions.get(definition.antiparticleId());
                if (!id.equals(antiparticle.antiparticleId())) {
                    problems.add(new Problem(id, "antiparticleId",
                            "antiparticle " + definition.antiparticleId() + " does not reciprocate"));
                }
            }

            for (ParticleDefinition.Component component : definition.composition()) {
                if (component.count() <= 0) {
                    problems.add(new Problem(id, "composition[" + component.particleId() + "]",
                            "component count must be positive, was " + component.count()));
                }
                if (!definitions.containsKey(component.particleId())) {
                    problems.add(new Problem(id, "composition[" + component.particleId() + "]",
                            "references unknown particle " + component.particleId()));
                }
            }
        }

        for (ResourceLocation id : definitions.keySet()) {
            List<ResourceLocation> cycle = findCompositionCycle(id, definitions);
            if (cycle != null) {
                problems.add(new Problem(id, "composition", "composition cycle: " + cycle));
            }
        }

        return new Report(problems);
    }

    private static List<ResourceLocation> findCompositionCycle(ResourceLocation start,
                                                                 Map<ResourceLocation, ParticleDefinition> definitions) {
        List<ResourceLocation> path = new ArrayList<>();
        Set<ResourceLocation> onPath = new HashSet<>();
        if (walk(start, definitions, path, onPath)) {
            return path;
        }
        return null;
    }

    private static boolean walk(ResourceLocation id, Map<ResourceLocation, ParticleDefinition> definitions,
                                 List<ResourceLocation> path, Set<ResourceLocation> onPath) {
        ParticleDefinition definition = definitions.get(id);
        if (definition == null) {
            return false;
        }
        path.add(id);
        onPath.add(id);
        for (ParticleDefinition.Component component : definition.composition()) {
            ResourceLocation componentId = component.particleId();
            if (onPath.contains(componentId)) {
                path.add(componentId);
                return true;
            }
            if (definitions.containsKey(componentId) && walk(componentId, definitions, path, onPath)) {
                return true;
            }
        }
        path.remove(path.size() - 1);
        onPath.remove(id);
        return false;
    }
}
