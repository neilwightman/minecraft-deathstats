package de.wightman.minecraft.deathstats.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/*
 License Info
 https://gamedev.stackexchange.com/questions/162976/how-do-i-create-a-weighted-collection-and-then-pick-a-random-element-from-it
 https://stackoverflow.com/help/licensing
*/
public class WeightedRandomBag<T extends Object> {

    private class Entry {
        double accumulatedWeight;
        T object;
    }

    private final List<Entry> entries = new ArrayList<>();
    private final Random rand = new Random();
    private double accumulatedWeight;

    public void addEntry(@NotNull T object, double weight) {
        Objects.requireNonNull(object, "object cannot be null");
        accumulatedWeight += weight;
        Entry e = new Entry();
        e.object = object;
        e.accumulatedWeight = accumulatedWeight;
        entries.add(e);
    }

    public T getRandom() {
        double r = rand.nextDouble() * accumulatedWeight;

        for (Entry entry: entries) {
            if (entry.accumulatedWeight >= r) {
                return entry.object;
            }
        }
        return null; //should only happen when there are no entries
    }
}
