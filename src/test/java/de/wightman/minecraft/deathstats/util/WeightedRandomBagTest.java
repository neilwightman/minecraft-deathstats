package de.wightman.minecraft.deathstats.util;

import org.junit.jupiter.api.Test;

public class WeightedRandomBagTest {

    @Test
    public void testBag() {
        WeightedRandomBag<String> itemDrops = new WeightedRandomBag<>();

        // Setup - a real game would read this information from a configuration file or database
        itemDrops.addEntry("10 Gold",  5.0);
        itemDrops.addEntry("Sword",   20.0);
        itemDrops.addEntry("Shield",  45.0);
        itemDrops.addEntry("Armor",   20.0);
        itemDrops.addEntry("Potion",  10.0);

        // drawing random entries from it
        for (int i = 0; i < 20; i++) {
            System.out.println(itemDrops.getRandom());
        }
    }
}
