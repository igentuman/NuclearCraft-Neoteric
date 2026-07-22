package igentuman.nc.handler.crafter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AutoCraftSolver<K> {

    public record PatternDef<K>(K output, int outputCount, Map<K, Integer> inputs) {}

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
            if (p.outputCount() > 0 && !p.inputs().isEmpty()) {
                outputIndex.putIfAbsent(p.output(), p);
            }
        }
    }

    private Map<K, Integer> pool;
    private Map<K, Integer> stockRemaining;
    private Map<K, Integer> baseCost;
    private Map<K, Integer> shortages;
    private List<PatternDef<K>> operations;
    private Set<K> active;
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
        if (pattern == null || !active.add(item)) {
            shortages.merge(item, need, Integer::sum);
            return false;
        }

        int outputCount = pattern.outputCount();
        int times = (need + outputCount - 1) / outputCount;

        boolean allOk = true;
        for (Map.Entry<K, Integer> input : pattern.inputs().entrySet()) {
            long amount = (long) input.getValue() * times;
            int clamped = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
            if (!produce(input.getKey(), clamped, depth + 1)) {
                allOk = false;
                if (aborted) {
                    active.remove(item);
                    return false;
                }
            }
        }
        active.remove(item);
        if (!allOk) {
            return false;
        }

        int produced = outputCount * times;
        pool.merge(item, produced - need, Integer::sum);
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
