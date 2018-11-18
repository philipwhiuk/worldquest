package com.whiuk.philip.worldquest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Inventory implements Iterable<Item> {
    private ArrayList<Item> items;

    Inventory() {
        items = new ArrayList<>();
    }

    public void addAll(List<Item> items) {
        for (Item item : items) {
            if (hasSpaceForItem(item)) {
                this.items.add(item);
            }
        }
    }

    boolean hasSpaceForItem(Item item) {
        return items.size() < 15;
    }

    public Item get(int index) {
        return items.get(index);
    }

    public Item remove(int index) {
        return items.remove(index);
    }

    public void add(Item item) {
        if (!hasSpaceForItem(item)) {
            throw new RuntimeException("Failed to make space before adding item to inventory");
        }
        items.add(item);
    }

    public boolean remove(Item item) {
        return items.remove(item);
    }

    public int size() {
        return items.size();
    }

    public boolean contains(Item i) {
        return items.contains(i);
    }

    @Override
    public Iterator<Item> iterator() {
        return items.iterator();
    }
}
