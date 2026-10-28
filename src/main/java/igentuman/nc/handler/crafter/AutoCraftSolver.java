package igentuman.nc.handler.crafter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AutoCraftSolver<K> {

    public record PatternDef<K>(Map<K, Integer> outputs, Map<K, Integer> inputs,
                                String processor, String recipeId, Map<K, List<Integer>> ingredientAmounts) {

        public PatternDef {
            outputs = Collections.unmodifiableMap(new LinkedHashMap<>(outputs));
            inputs = Collections.unmodifiableMap(new LinkedHashMap<>(inputs));
            Map<K, List<Integer>> parts = new LinkedHashMap<>();
            ingredientAmounts.forEach((key, amounts) -> parts.put(key, List.copyOf(amounts)));
            ingredientAmounts = Collections.unmodifiableMap(parts);
        }

        public PatternDef(K output, int outputCount, Map<K, Integer> inputs) {
            this(Map.of(output, outputCount), inputs, "", "", Map.of());
        }

        public List<Integer> amounts(K key) {
            List<Integer> list = ingredientAmounts.get(key);
            return list == null || list.isEmpty() ? List.of(inputs.getOrDefault(key, 0)) : list;
        }

        public boolean isProcessing() {
            return processor != null && !processor.isEmpty();
        }
    }

    public record Plan<K>(List<PatternDef<K>> operations, Map<K, Integer> baseCost) {}

    public record Result<K>(Plan<K> plan, Map<K, Integer> shortages, boolean tooComplex) {
        public boolean feasible() {
            return plan != null;
        }

        static <K> Result<K> ok(Plan<K> plan) {
            return new Result<>(plan, Map.of(), false);
        }

        static <K> Result<K> shortage(Map<K, Integer> shortages) {
            return new Result<>(null, shortages, false);
        }

        static <K> Result<K> overLimit() {
            return new Result<>(null, Map.of(), true);
        }
    }

    private static final int MAX_OPS = 1000;
    private static final int MAX_DEPTH = 64;

    private final Map<K, PatternDef<K>> outputIndex = new LinkedHashMap<>();

    public AutoCraftSolver(List<PatternDef<K>> patterns) {
        for (PatternDef<K> p : patterns) {
            if (!p.inputs().isEmpty() && p.inputs().values().stream().allMatch(n -> n > 0)
                    && p.outputs().values().stream().allMatch(n -> n > 0)) {
                for (K output : p.outputs().keySet()) outputIndex.putIfAbsent(output, p);
            }
        }
    }

    private Map<K, Integer> pool;
    private Map<K, Integer> stockRemaining;
    private Map<K, Integer> baseCost;
    private Map<K, Integer> shortages;
    private List<PatternDef<K>> operations;
    private Set<PatternDef<K>> active;
    private int opCount;
    private boolean aborted;

    public Result<K> solve(K target, int qty, Map<K, Integer> available) {
        pool = new HashMap<>(available);
        stockRemaining = new HashMap<>(available);
        baseCost = new LinkedHashMap<>();
        shortages = new LinkedHashMap<>();
        operations = new ArrayList<>();
        active = new HashSet<>();
        opCount = 0;
        aborted = false;

        boolean ok = qty <= 0 || produce(target, qty, 0);
        if (aborted) {
            return Result.overLimit();
        }
        if (!ok || !shortages.isEmpty()) {
            return Result.shortage(shortages);
        }
        return Result.ok(new Plan<>(operations, baseCost));
    }

    private boolean produce(K item, int need, int depth) {
        if (aborted) {
            return false;
        }
        if (depth > MAX_DEPTH) {
            aborted = true;
            return false;
        }

        int have = pool.getOrDefault(item, 0);
        int use = Math.min(have, need);
        if (use > 0) {
            pool.put(item, have - use);
            int fromStock = Math.min(use, stockRemaining.getOrDefault(item, 0));
            if (fromStock > 0) {
                stockRemaining.merge(item, -fromStock, Integer::sum);
                baseCost.merge(item, fromStock, Integer::sum);
            }
            need -= use;
        }
        if (need == 0) {
            return true;
        }

        PatternDef<K> pattern = outputIndex.get(item);
        if (pattern == null || !active.add(pattern)) {
            shortages.merge(item, need, Integer::sum);
            return false;
        }

        int outputCount = pattern.outputs().get(item);
        int times = (int) (((long) need + outputCount - 1) / outputCount);
        if (times > MAX_OPS - opCount) {
            aborted = true;
            return false;
        }

        boolean allOk = true;
        for (Map.Entry<K, Integer> input : pattern.inputs().entrySet()) {
            long amount = (long) input.getValue() * times;
            if (amount > Integer.MAX_VALUE) {
                aborted = true;
                return false;
            }
            if (!produce(input.getKey(), (int) amount, depth + 1)) {
                allOk = false;
                if (aborted) {
                    active.remove(pattern);
                    return false;
                }
            }
        }
        active.remove(pattern);
        if (!allOk) {
            return false;
        }

        for (Map.Entry<K, Integer> output : pattern.outputs().entrySet()) {
            long available = (long) pool.getOrDefault(output.getKey(), 0) + (long) output.getValue() * times;
            if (output.getKey().equals(item)) available -= need;
            if (available > Integer.MAX_VALUE) {
                aborted = true;
                return false;
            }
            pool.put(output.getKey(), (int) available);
        }
        for (int i = 0; i < times; i++) {
            operations.add(pattern);
            if (++opCount > MAX_OPS) {
                aborted = true;
                return false;
            }
        }
        return true;
    }
}
