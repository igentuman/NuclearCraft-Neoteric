package igentuman.nc.multiblock;

import java.util.*;

public class ValidationScheduler {

    private final Map<String, List<String>> adjList = new HashMap<>();
    private final Map<String, Integer> indexMap = new HashMap<>();
    private final Map<String, Integer> lowlinkMap = new HashMap<>();
    private final Deque<String> stack = new ArrayDeque<>();
    private final Set<String> onStack = new HashSet<>();
    private final List<List<String>> sccs = new ArrayList<>();
    private int index = 0;

    public void graphAddEdge(String from, String to) {
        adjList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        adjList.putIfAbsent(to, new ArrayList<>());
    }

    public void addNode(String node) {
        adjList.putIfAbsent(node, new ArrayList<>());
    }

    private boolean isCyclic(List<String> scc) {
        if (scc.size() > 1) return true;
        String node = scc.get(0);
        return adjList.get(node).contains(node);
    }

    private void strongConnect(String node) {
        indexMap.put(node, index);
        lowlinkMap.put(node, index++);
        stack.push(node);
        onStack.add(node);
        for (String neighbor : adjList.get(node)) {
            if (!indexMap.containsKey(neighbor)) {
                strongConnect(neighbor);
                lowlinkMap.put(node, Math.min(lowlinkMap.get(node), lowlinkMap.get(neighbor)));
            } else if (onStack.contains(neighbor)) {
                lowlinkMap.put(node, Math.min(lowlinkMap.get(node), indexMap.get(neighbor)));
            }
        }
        if (lowlinkMap.get(node).equals(indexMap.get(node))) {
            List<String> scc = new ArrayList<>();
            String w;
            do {
                w = stack.pop();
                onStack.remove(w);
                scc.add(w);
            } while (!w.equals(node));
            sccs.add(scc);
        }
    }

    public List<String> getSchedule() {
        for (String node : adjList.keySet()) {
            if (!indexMap.containsKey(node)) {
                strongConnect(node);
            }
        }

        Map<String, Integer> nodeToSCC = new HashMap<>();
        for (int i = 0; i < sccs.size(); i++) {
            for (String node : sccs.get(i)) {
                nodeToSCC.put(node, i);
            }
        }

        Map<Integer, Set<Integer>> sccGraph = new HashMap<>();
        for (String node : adjList.keySet()) {
            int fromSCC = nodeToSCC.get(node);
            for (String dep : adjList.get(node)) {
                int toSCC = nodeToSCC.get(dep);
                if (fromSCC != toSCC) {
                    sccGraph.computeIfAbsent(toSCC, k -> new HashSet<>()).add(fromSCC);
                }
            }
        }

        Map<Integer, Integer> inDegree = new HashMap<>();
        for (int i = 0; i < sccs.size(); i++) {
            inDegree.putIfAbsent(i, 0);
        }
        for (Set<Integer> deps : sccGraph.values()) {
            for (int dep : deps) {
                inDegree.put(dep, inDegree.get(dep) + 1);
            }
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < sccs.size(); i++) {
            if (inDegree.get(i) == 0) {
                queue.add(i);
            }
        }

        List<Integer> topoOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            int curr = queue.poll();
            topoOrder.add(curr);
            for (int neighbor : sccGraph.getOrDefault(curr, Set.of())) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        List<String> finalSchedule = new ArrayList<>();
        for (int idx : topoOrder) {
            List<String> scc = sccs.get(idx);
            if (isCyclic(scc)) {
                finalSchedule.addAll(scc);
                finalSchedule.addAll(scc);
            } else {
                finalSchedule.addAll(scc);
            }
        }

        return finalSchedule;
    }
}
