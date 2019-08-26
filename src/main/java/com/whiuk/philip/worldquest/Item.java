package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.whiuk.philip.worldquest.Item.ItemAction.EQUIP;
import static com.whiuk.philip.worldquest.Item.ItemAction.USE;

public class Item {

    static class Provider {
        static Map<String, Item> loadItemsFromBuffer(BufferedReader buffer) throws IOException {
            Map<String, Item> items = new HashMap<>();
            int itemCount = Integer.parseInt(buffer.readLine());
            for (int i = 0; i < itemCount; i++) {
                String[] itemData = buffer.readLine().split(",");
                String itemID = itemData[0];
                String itemClass = itemData[1];
                switch(itemClass) {
                    case "Item":
                        items.put(itemID, new Item(itemData[2], Item.parseActions(itemData[3])));
                        break;
                    case "Weapon":
                        items.put(itemID, new Weapon(itemData[2],
                                Item.parseActions(itemData[3]), itemData[4], Integer.parseInt(itemData[5])));
                        break;
                    case "Hatchet":
                        items.put(itemID, new Hatchet(itemData[2],
                                Item.parseActions(itemData[3]), Integer.parseInt(itemData[4])));
                        break;
                    case "Armour":
                        items.put(itemID, new Armour(itemData[2], Item.parseActions(itemData[3]),
                                Slot.valueOf(itemData[4]), Integer.parseInt(itemData[5])));
                        break;
                    case "Consumable":
                        items.put(itemID, new Consumable(itemData[2], Item.parseActions(itemData[3]),
                                Consumable.parseStatChanges(itemData[4])));
                        break;
                    default:
                        throw new IllegalArgumentException(itemClass);
                }

            }
            return items;
        }
    }
    static class Persistor {
        static void saveItemsToBuffer(Map<String, Item> items, BufferedWriter buffer) {
        }
    }

    enum ItemAction {
        EQUIP, USE, EAT
    }

    static Item parseItem(String itemData) {
        String[] data = itemData.split(",");
        return new Item(data[0], parseActions(data[1]));
    }

    static List<ItemAction> parseActions(String actionData) {
        return Arrays
                .stream(actionData.split("\\|"))
                .map(ItemAction::valueOf)
                .collect(Collectors.toList());
    }

    final String name;
    final List<ItemAction> actions;

    Item(String name, List<ItemAction> actions) {
        this.name = name;
        this.actions = actions;
    }

    public Item copy() {
        return new Item(this.name, this.actions);
    }

    public boolean canUse() {
        return actions.contains(USE);
    }

    public boolean canEquip() {
        return actions.contains(EQUIP);
    }

    public String print() {
        return this.name+","+actions.stream().map(Enum::name).collect(Collectors.joining("|"));
    }

    public ItemAction getPrimaryAction() {
        return actions.get(0);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Item && name.equals(((Item) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean hasAction() {
        return !actions.isEmpty();
    }
}

enum Slot {OFF_HAND("Off-hand"), CHEST("Chest"), LEGS("Legs"), ARMS("Arms"), HANDS("Hands"), FEET("Feet"), HEAD("Head");
    public String friendlyName;
    Slot(String friendlyName) {
        this.friendlyName = friendlyName;
    }
}

class Armour extends Item {
    final Slot slot;
    final int protection;

    static Armour parseItem(String itemData) {
        String[] itemDataFields = itemData.split(",");
        return new Armour(itemDataFields[0],
                parseActions(itemDataFields[1]),
                Slot.valueOf(itemDataFields[2]),
                Integer.parseInt(itemDataFields[3]));
    }

    Armour(String name, List<ItemAction> actions, Slot slot, int protection) {
        super(name, actions);
        this.slot = slot;
        this.protection = protection;
    }

    @Override
    public Armour copy() {
        return new Armour(name, actions, slot, protection);
    }

    @Override
    public boolean canEquip() {
        return true;
    }

    @Override
    public String print() {
        return super.print()+","+slot+","+protection;
    }
}

class Weapon extends Item {
    public final String type;
    public final int damage;

    static Weapon parseItem(String itemData) {
        String[] itemDataFields = itemData.split(",");
        return new Weapon(
                itemDataFields[0],
                parseActions(itemDataFields[1]),
                itemDataFields[2],
                Integer.parseInt(itemDataFields[3]));
    }

        Weapon(String name, List<ItemAction> actions, String type, int damage) {
        super(name, actions);
        this.type = type;
        this.damage = damage;
    }

    @Override
    public Weapon copy() {
        return new Weapon(this.name, this.actions, this.type, damage);
    }

    @Override
    public boolean canEquip() {
        return true;
    }

    @Override
    public String print() {
        return super.print()+","+type+","+damage;
    }
}

class Consumable extends Item {

    public List<StatChange> statChanges;

    static Consumable parseItem(String itemData) {
        String[] itemDataFields = itemData.split(",");
        return new Consumable(itemDataFields[0],
                parseActions(itemDataFields[1]),
                parseStatChanges(itemDataFields[2]));
    }

    static List<StatChange> parseStatChanges(String statData) {
        String[] data = statData.split("\\|");
        List<StatChange> statChanges = new ArrayList<>();
        for (int i = 0; i < data.length; i += 2) {
            statChanges.add(new StatChange(data[i], Integer.parseInt(data[i+1])));
        }
        return statChanges;
    }

    static String printStatChanges(List<StatChange> statChanges) {
        return statChanges
                .stream()
                .map(s -> s.stat+"|" + s.change)
                .collect(Collectors.joining("|"));
    }

    Consumable(String name, List<ItemAction> actions, List<StatChange> statChanges) {
        super(name, actions);
        this.statChanges = statChanges;
    }

    @Override
    public Consumable copy() {
        return new Consumable(name, actions, statChanges);
    }

    @Override
    public boolean canEquip() {
        return true;
    }

    @Override
    public String print() {
        return super.print()+","+printStatChanges(statChanges);
    }
}

class StatChange {
    String stat;
    int change;

    StatChange(String stat, int change) {
        this.stat = stat;
        this.change = change;
    }
}

class Hatchet extends Weapon {

    static Hatchet parseItem(String itemData) {
        String[] itemDataFields = itemData.split(",");
        return new Hatchet(itemDataFields[0], parseActions(itemDataFields[1]), Integer.parseInt(itemDataFields[3]));
    }

    Hatchet(String name, List<ItemAction> actions, int damage) {
        super(name, actions, "Hatchet", damage);
    }

    @Override
    public Hatchet copy() {
        return new Hatchet(this.name, this.actions, damage);
    }

    @Override
    public String print() {
        return super.print();
    }
}
