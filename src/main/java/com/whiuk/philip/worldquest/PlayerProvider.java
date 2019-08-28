package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerProvider {

    static Player createPlayer(ScenarioData scenarioData) {
        return scenarioData.newPlayer();
    }

    public static Player loadPlayer(BufferedReader buffer) throws IOException {
        String playerStatus[] = buffer.readLine().split(",");

        int statsCount = Integer.parseInt(buffer.readLine());
        Map<String, Experience> stats = new HashMap<>();
        for (int i = 0; i < statsCount; i++) {
            String[] statData = buffer.readLine().split(":");
            try {
                stats.put(statData[0], new Experience(Integer.parseInt(statData[1])));
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse stat: " + statData[0], e);
            }
        }

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
                weapon = (Weapon) Item.Provider.parseItem(mainHandWeaponString);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse weapon", e);
        }
        int armourCount = Integer.parseInt(buffer.readLine());
        Map<Slot, Armour> armour = new HashMap<>();
            for (int i = 0; i < armourCount; i++) {
                String[] armourData = buffer.readLine().split(":");
                try {
                    armour.put(Slot.valueOf(armourData[0]), (Armour) Item.Provider.parseItem(armourData[1]));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unable to parse armour in slot: " + armourData[0], e);
                }
            }
        int inventoryCount = Integer.parseInt(buffer.readLine());
        List<Item> inventory = new ArrayList<>();
        for (int i = 0; i < inventoryCount; i++) {
            try {
                inventory.add(Item.Provider.parseItem(buffer.readLine()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse inventory item: " + i, e);
            }
        }


        int questCount = Integer.parseInt(buffer.readLine());
        Map<String, Quest> quests = new HashMap<>();
        for (int i = 0; i < questCount; i++) {
            try {
                Quest quest = Quest.Provider.parseQuest(buffer);
                quests.put(quest.name, quest);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse quest: " + i, e);
            }
        }

        try {
            return new Player(
                    Integer.parseInt(playerStatus[0]),
                    Integer.parseInt(playerStatus[1]),
                    Integer.parseInt(playerStatus[2]),
                    Integer.parseInt(playerStatus[3]),
                    Integer.parseInt(playerStatus[4]),
                    stats, skills, weapon, armour, inventory, quests,
                    Integer.parseInt(playerStatus[5]),
                    Integer.parseInt(playerStatus[6])
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse player", e);
        }
    }

    public static void savePlayer(BufferedWriter buffer, Player player) throws IOException {
        buffer.write(String.join(",",
                ""+player.maxHealth, ""+player.health,
                ""+player.maxFood, ""+player.food,
                ""+player.money,
                ""+player.x, ""+player.y));
        buffer.newLine();
        buffer.write(""+player.stats.size());
        buffer.newLine();
        for (Map.Entry<String, Experience> entry : player.stats.entrySet()) {
            buffer.write(entry.getKey()+":"+entry.getValue().experience);
            buffer.newLine();
        }
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
        buffer.write(""+player.quests.size());
        buffer.newLine();
        for (Quest quest : player.quests.values()) {
            Quest.Provider.writeQuest(buffer, quest);
        }

    }

    private static String printItem(Item item) {
        return item.getClass().getSimpleName()+","+item.print();
    }
}
