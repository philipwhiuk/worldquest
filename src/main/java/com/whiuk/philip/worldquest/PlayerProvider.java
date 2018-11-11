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
        int skillsCount = Integer.parseInt(buffer.readLine());
        Map<String, Experience> skills = new HashMap<>();
        for (int i = 0; i < skillsCount; i++) {
            String[] skillData = buffer.readLine().split(":");
            try {
                skills.put(skillData[0], new Experience(Integer.parseInt(skillData[1])));
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse skill: " + skillData[0], e);
            }
        }
        String mainHandWeaponString = buffer.readLine();
        Weapon weapon = null;
        try {
            if (!mainHandWeaponString.isEmpty()) {
                weapon = (Weapon) parseItem(mainHandWeaponString);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse weapon", e);
        }
        int armourCount = Integer.parseInt(buffer.readLine());
        Map<Slot, Armour> armour = new HashMap<>();
            for (int i = 0; i < armourCount; i++) {
                String[] armourData = buffer.readLine().split(":");
                try {
                    armour.put(Slot.valueOf(armourData[0]), (Armour) parseItem(armourData[1]));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unable to parse armour in slot: " + armourData[0], e);
                }
            }
        int inventoryCount = Integer.parseInt(buffer.readLine());
        List<Item> inventory = new ArrayList<>();
        for (int i = 0; i < inventoryCount; i++) {
            try {
                inventory.add(parseItem(buffer.readLine()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse inventory item: " + i, e);
            }
        }
        try {
            return new Player(
                    Integer.parseInt(playerStats[0]),
                    Integer.parseInt(playerStats[1]),
                    Integer.parseInt(playerStats[2]),
                    Integer.parseInt(playerStats[3]),
                    skills, weapon, armour, inventory,
                    Integer.parseInt(playerStats[4]),
                    Integer.parseInt(playerStats[5])
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse player", e);
        }
    }

    public static void savePlayer(BufferedWriter buffer, Player player) throws IOException {
        buffer.write(String.join(",",
                ""+player.maxHealth, ""+player.health, ""+player.money,
                ""+player.baseDamage, ""+player.x, ""+player.y));
        buffer.newLine();
        buffer.write(""+player.skills.size());
        buffer.newLine();
        for (Map.Entry<String, Experience> entry : player.skills.entrySet()) {
            buffer.write(entry.getKey()+":"+entry.getValue().experience);
            buffer.newLine();
        }
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
