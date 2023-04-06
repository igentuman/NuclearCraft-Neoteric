package igentuman.nc.phosphophyllite.util;

import igentuman.nc.util.annotation.NonnullDefault;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Contract;

import java.util.Collections;
import java.util.List;

@NonnullDefault

public class FastArraySet<T> {
    private final Object2IntOpenHashMap<T> indexMap = new Object2IntOpenHashMap<>();
    private final ObjectArrayList<T> elementList = new ObjectArrayList<>();
    private final List<T> unmodifiableList = Collections.unmodifiableList(elementList);
    
    private int version = 0;
    
    @Contract
    public int add(T element) {
        int index = indexMap.getOrDefault(element, -1);
        if (index != -1) {
            return index;
        }
        // because this always adds to the end of the list, this doesnt invalidate previous indices
        index = elementList.size();
        indexMap.put(element, index);
        elementList.add(element);
        return index;
    }
    
    public boolean remove(T element) {
        if (!indexMap.containsKey(element)) {
            return false;
        }
        int index = indexMap.removeInt(element);
        final var popped = elementList.pop();
        if (index == elementList.size()) {
            return false;
        }
        // moving an item around, this invalidates previously fetches indices
        version++;
        // the element we popped off wasn't the one that is getting removed
        assert elementList.get(index) == element;
        elementList.set(index, popped);
        indexMap.put(popped, index);
        return true;
    }
    
    public boolean contains(T element) {
        return indexMap.containsKey(element);
    }
    
    public List<T> elements() {
        return unmodifiableList;
    }
    
    public int size() {
        return elementList.size();
    }
    
    public T get(int i) {
        return elementList.get(i);
    }
    
    public int version() {
        return version;
    }
    
    public int indexOf(T element) {
        return indexMap.getInt(element);
    }
}
