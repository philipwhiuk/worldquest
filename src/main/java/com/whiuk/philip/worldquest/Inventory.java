package com.whiuk.philip.worldquest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Inventory implements Iterable<Item> {
    private static final int MAX_INVENTORY_SIZE = 15;
    private ArrayList<Item> items;

    Inventory() {
        items = new ArrayList<>();
    }

    void addAll(List<Item> items) {
        if (hasSpaceForItems(items)) {
            for (Item item : items) {
                this.items.add(item);
            }
        }
    }

    boolean hasSpaceForItems(List<Item> items) {
        return this.items.size() + items.size() <= MAX_INVENTORY_SIZE;
    }

    boolean hasSpaceForItem(Item item) {
        return hasSpaceForItems(Collections.singletonList(item));
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

    public boolean containsItem(String itemName) {
        return items.stream().anyMatch(item -> item.name.equals(itemName));
    }
}
