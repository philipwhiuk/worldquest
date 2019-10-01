package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;

import static com.whiuk.philip.worldquest.ItemAction.USE;

class ItemTypeDao {
    static class Provider {
        static Map<String, ItemType> loadItemTypesFromJson(JSONArray itemTypesData) throws IOException {
            Map<String, ItemType> itemTypes = new HashMap<>();
            for (Object itemTypeObj: itemTypesData) {
                JSONObject itemTypeData = (JSONObject) itemTypeObj;
                String itemID = (String) itemTypeData.get("id");
                String itemClass = (String) itemTypeData.get("class");
                switch(itemClass) {
                    case "Item":
                        itemTypes.put(itemID, new StandardItemType((String) itemTypeData.get("name"),
                                parseActions((JSONArray) itemTypeData.get("actions"))));
                        break;
                    case "Weapon":
                        itemTypes.put(itemID, new WeaponType((String) itemTypeData.get("name"),
                                parseActions((JSONArray) itemTypeData.get("actions")),
                                (String) itemTypeData.get("type"), ((Long) itemTypeData.get("damage")).intValue()));
                        break;
                    case "Hatchet":
                        itemTypes.put(itemID, new HatchetType((String) itemTypeData.get("name"),
                                parseActions((JSONArray) itemTypeData.get("actions")),
                                ((Long) itemTypeData.get("damage")).intValue()));
                        break;
                    case "Armour":
                        itemTypes.put(itemID, new ArmourType((String) itemTypeData.get("name"),
                                parseActions((JSONArray) itemTypeData.get("actions")),
                                Slot.valueOf((String) itemTypeData.get("slot")), ((Long) itemTypeData.get("protection")).intValue()));
                        break;
                    case "Consumable":
                        itemTypes.put(itemID, new ConsumableType((String) itemTypeData.get("name"),
                                parseActions((JSONArray) itemTypeData.get("actions")),
                                ConsumableType.parseStatChanges((JSONArray) itemTypeData.get("statChanges"))));
                        break;
                    default:
                        throw new IllegalArgumentException(itemClass);
                }

            }
            return itemTypes;
        }

        static List<ItemAction> parseActions(JSONArray actionData) {
            List<ItemAction> actions = new ArrayList<>();
            for (Object action : actionData) {
                actions.add(ItemAction.valueOf((String) action));
            }
            return actions;
        }
    }

    static class Persistor {
        static JSONArray saveItemTypesToJson(Map<String, ItemType> itemTypes) {
            JSONArray data = new JSONArray();
            for (Map.Entry<String, ItemType> itemType: itemTypes.entrySet()) {
                JSONObject itemTypeData = new JSONObject();
                itemTypeData.put("id", itemType.getKey());
                itemTypeData.putAll(itemType.getValue().toJson());
                data.add(itemTypeData);
            }
            return data;
        }
    }
}

abstract class Item<T extends ItemType> {
    private final T type;
    private float quality;
    private int damage;

    Item(T itemType) {
        this.type = itemType;
    }

    T getType() {
        return this.type;
    }
}

class StandardItem extends Item<ItemType> {
    StandardItem(ItemType itemType) {
        super(itemType);
    }
}

class Weapon<S extends WeaponType> extends Item<S> {
    Weapon(S itemType) {
        super(itemType);
    }
}
class Armour extends Item<ArmourType> {
    Armour(ArmourType armourType) {
        super(armourType);
    }
}
class Consumable extends Item<ConsumableType> {
    Consumable(ConsumableType itemType) {
        super(itemType);
    }
}
class Hatchet extends Weapon<HatchetType> {
    Hatchet(HatchetType itemType) {
        super(itemType);
    }
}

abstract class ItemType<T extends Item> {
    final String name;
    final List<ItemAction> actions;

    ItemType(String name, List<ItemAction> actions) {
        this.name = name;
        this.actions = actions;
    }

    abstract T create();

    public boolean canUse() {
        return actions.contains(USE);
    }

    public ItemAction getPrimaryAction() {
        return actions.get(0);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ItemType && name.equals(((ItemType) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean hasAction() {
        return !actions.isEmpty();
    }

    public JSONObject toJson() {
        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("actions", new JSONArray());
        return data;
    }
}

class StandardItemType extends ItemType<Item> {

    public StandardItem create() {
        return new StandardItem(this);
    }

    StandardItemType(String name, List<ItemAction> actions) {
        super(name, actions);
    }
}

enum Slot {OFF_HAND("Off-hand"), CHEST("Chest"), LEGS("Legs"), ARMS("Arms"), HANDS("Hands"), FEET("Feet"), HEAD("Head");
    public String friendlyName;
    Slot(String friendlyName) {
        this.friendlyName = friendlyName;
    }
}

class ArmourType extends ItemType<Armour> {
    final Slot slot;
    final int protection;

    ArmourType(String name, List<ItemAction> actions, Slot slot, int protection) {
        super(name, actions);
        this.slot = slot;
        this.protection = protection;
    }

    @Override
    public Armour create() {
        return new Armour(this);
    }
}

class WeaponType extends ItemType<Weapon> {
    final String weaponSkill;
    final int damage;

    WeaponType(String name, List<ItemAction> actions, String weaponSkill, int damage) {
        super(name, actions);
        this.weaponSkill = weaponSkill;
        this.damage = damage;
    }

    @Override
    public Weapon create() {
        return new Weapon<>(this);
    }
}

class ConsumableType extends ItemType<Consumable> {

    List<StatChange> statChanges;

    static List<StatChange> parseStatChanges(JSONArray statData) {
        List<StatChange> statChanges = new ArrayList<>();
        for (Object sCO : statData) {
            JSONObject statChangeData = (JSONObject) sCO;
            statChanges.add(new StatChange(
                    (String) statChangeData.get("stat"),
                    ((Long) statChangeData.get("value")).intValue()));
        }
        return statChanges;
    }

    ConsumableType(String name, List<ItemAction> actions, List<StatChange> statChanges) {
        super(name, actions);
        this.statChanges = statChanges;
    }

    @Override
    Consumable create() {
        return new Consumable(this);
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

class HatchetType extends WeaponType {

    HatchetType(String name, List<ItemAction> actions, int damage) {
        super(name, actions, "Hatchet", damage);
    }

    @Override
    public Hatchet create() {
        return new Hatchet(this);
    }
}
