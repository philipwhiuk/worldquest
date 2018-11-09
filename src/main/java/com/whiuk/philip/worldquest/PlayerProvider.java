package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerProvider {

    static Player createPlayer() {
        return new Player(10, 10);
    }

    public static Player loadPlayer(BufferedReader buffer) throws IOException {
        String playerStats[] = buffer.readLine().split(",");
        String mainHandWeaponString = buffer.readLine();
        Weapon weapon = null;
        if (!mainHandWeaponString.isEmpty()) {
            weapon = (Weapon) parseItem(mainHandWeaponString);
        }
        int armourCount = Integer.parseInt(buffer.readLine());
        Map<Slot, Armour> armour = new HashMap<>();
        for (int i = 0; i < armourCount; i++) {
            String[] armourData = buffer.readLine().split(":");
            armour.put(Slot.valueOf(armourData[0]), (Armour) parseItem(armourData[1]));
        }
        int inventoryCount = Integer.parseInt(buffer.readLine());
        List<Item> inventory = new ArrayList<>();
        for (int i = 0; i < inventoryCount; i++) {
            inventory.add(parseItem(buffer.readLine()));
        }
        return new Player(
                Integer.parseInt(playerStats[0]),
                Integer.parseInt(playerStats[1]),
                Integer.parseInt(playerStats[2]),
                Integer.parseInt(playerStats[3]),
                Integer.parseInt(playerStats[4]),
                weapon, armour, inventory,
                Integer.parseInt(playerStats[5]),
                Integer.parseInt(playerStats[6])
        );
    }

    public static void savePlayer(BufferedWriter buffer, Player player) throws IOException {
        buffer.write(String.join(",",
                ""+player.maxHealth, ""+player.health, ""+player.money, ""+player.experience,
                ""+player.baseDamage, ""+player.x, ""+player.y));
        buffer.newLine();
        buffer.write(player.mainHandWeapon != null ? printItem(player.mainHandWeapon) : "");
        buffer.newLine();
        buffer.write(""+player.armour.size());
        buffer.newLine();
        for (Map.Entry<Slot, Armour> entry : player.armour.entrySet()) {
            buffer.write(entry.getKey().name()+":"+printItem(entry.getValue()));
            buffer.newLine();
        }
        buffer.write(""+player.inventory.size());
        buffer.newLine();
        for (Item item : player.inventory) {
            buffer.write(printItem(item));
            buffer.newLine();
        }
    }

    private static Item parseItem(String itemData) {
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

    private static String printItem(Item item) {
        return item.getClass().getSimpleName()+","+item.print();
    }
}
