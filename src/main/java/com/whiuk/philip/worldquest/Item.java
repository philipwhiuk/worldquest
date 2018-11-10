package com.whiuk.philip.worldquest;

public class Item {
    static Item parseItem(String itemData) {
        String[] data = itemData.split(",");
        return new Item(data[0], Boolean.parseBoolean(data[1]));
    }

    final String name;
    final boolean useable;

    Item(String name, boolean useable) {
        this.name = name;
        this.useable = useable;
    }

    public Item copy() {
        return new Item(this.name, this.useable);
    }

    public boolean canUse() {
        return useable;
    }

    public boolean canEquip() {
        return false;
    }

    public String print() {
        return this.name+","+useable;
    }
}

enum Slot { CHEST }

class Armour extends Item {
    final Slot slot;
    final int protection;

    static Armour parseItem(String itemData) {
        String[] itemDataFields = itemData.split(",");
        return new Armour(itemDataFields[0],
                Boolean.parseBoolean(itemDataFields[1]),
                Slot.valueOf(itemDataFields[2]),
                Integer.parseInt(itemDataFields[3]));
    }

    Armour(String name, boolean useable, Slot slot, int protection) {
        super(name, useable);
        this.slot = slot;
        //TODO: Attack type bonuses
        this.protection = protection;
    }

    @Override
    public Armour copy() {
        return new Armour(name, useable, slot, protection);
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
                Boolean.parseBoolean(itemDataFields[1]),
                itemDataFields[2],
                Integer.parseInt(itemDataFields[3]));
    }

        Weapon(String name, boolean useable, String type, int damage) {
        super(name, useable);
        this.type = type;
        this.damage = damage;
    }

    @Override
    public Weapon copy() {
        return new Weapon(this.name, this.useable, this.type, damage);
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

class Hatchet extends Weapon {

    static Hatchet parseItem(String itemData) {
        String[] itemDataFields = itemData.split(",");
        return new Hatchet(itemDataFields[0], Boolean.parseBoolean(itemDataFields[1]), Integer.parseInt(itemDataFields[3]));
    }

    Hatchet(String name, boolean useable, int damage) {
        super(name, useable, "Hatchet", damage);
    }

    @Override
    public Hatchet copy() {
        return new Hatchet(this.name, this.useable, damage);
    }

    @Override
    public String print() {
        return super.print();
    }
}
