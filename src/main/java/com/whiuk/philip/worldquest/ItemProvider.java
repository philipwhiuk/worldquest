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
}
