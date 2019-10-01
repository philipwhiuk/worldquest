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

    void addAll(List<Item> newItems) {
        if (hasSpaceForItems(newItems)) {
            this.items.addAll(newItems);
        }
    }

    boolean hasSpaceForItems(int totalCount) {
        return this.items.size() + totalCount <= MAX_INVENTORY_SIZE;
    }

    boolean hasSpaceForItems(List<Item> newItems) {
        return this.items.size() + newItems.size() <= MAX_INVENTORY_SIZE;
    }

    boolean hasSpaceForItem(Item item) {
        return hasSpaceForItems(Collections.singletonList(item));
    }

    boolean hasSpaceForItemOfType(ItemType item) {
        return this.items.size() + 1 <= MAX_INVENTORY_SIZE;
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

    public boolean remove(ItemType itemType) {
        for (Iterator<Item> it = items.iterator(); it.hasNext();) {
            Item i = it.next();
            if (i.getType().equals(itemType)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public int size() {
        return items.size();
    }

    public boolean contains(ItemType i) {
        return items.stream().anyMatch(item -> item.getType().equals(i));
    }

    @Override
    public Iterator<Item> iterator() {
        return items.iterator();
    }

    public boolean contains(ItemType searchItem, int quantity) {
        return items.stream().filter(item -> item.getType().equals(searchItem)).count() >= quantity;
    }
}
