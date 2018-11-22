package com.whiuk.philip.worldquest;

public class ItemProvider {
    static Item parseItem(String itemData) {
        String[] itemClassData = itemData.split(",", 2);
        switch (itemClassData[0]) {
            case "Item":
                return Item.parseItem(itemClassData[1]);
            case "Weapon":
                return Weapon.parseItem(itemClassData[1]);
            case "Armour":
                return Armour.parseItem(itemClassData[1]);
            case "Hatchet":
                return Hatchet.parseItem(itemClassData[1]);
        }
        throw new IllegalArgumentException(itemClassData[0]);
    }

    public static String printItem(Item resource) {
        if (resource instanceof Hatchet) {
            return "Hatchet,"+resource.print();
        } else if (resource instanceof Armour) {
            return "Armour,"+resource.print();
        } else if (resource instanceof Weapon) {
            return "Weapon,"+resource.print();
        } else {
            return "Item,"+resource.print();
        }
    }
}
