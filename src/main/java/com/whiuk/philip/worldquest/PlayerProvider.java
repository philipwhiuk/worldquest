package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.whiuk.philip.worldquest.JsonUtils.intFromObj;

public class PlayerProvider {

    static Player createPlayer(ScenarioData scenarioData) {
        return scenarioData.newPlayer();
    }

    public static Player loadPlayer(JSONObject playerData, Map<String, ItemType> itemTypes, Map<String, Quest> quests) {

        JSONArray statsData = (JSONArray) playerData.get("stats");
        Map<String, Experience> stats = new HashMap<>();
        for (Object sO : statsData) {
            JSONObject statData = (JSONObject) sO;
            try {
                stats.put((String) statData.get("stat"), new Experience(intFromObj(statData.get("exp"))));
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse stat: " + statData, e);
            }
        }

        JSONArray skillsData = (JSONArray) playerData.get("skills");
        Map<String, Experience> skills = new HashMap<>();
        for (Object sO : skillsData) {
            JSONObject skillData = (JSONObject) sO;
            try {
                stats.put((String) skillData.get("skill"), new Experience(intFromObj(skillData.get("exp"))));
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse skill: " + skillData, e);
            }
        }

        Weapon<WeaponType> weapon = null;
        try {
            String mainHandWeaponString = (String) playerData.getOrDefault("mainHandWeapon", "");
            if (!mainHandWeaponString.isEmpty()) {
                //TODO: Don't create new
                weapon = (Weapon<WeaponType>) ((WeaponType) itemTypes.get(mainHandWeaponString)).create();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse weapon", e);
        }
        JSONArray armourData = (JSONArray) playerData.get("armour");
        Map<Slot, Armour> armour = new HashMap<>();
            for (Object aO : armourData) {
                JSONObject armourSlotData = (JSONObject) aO;
                try {
                    //TODO: Don't create new
                    armour.put(Slot.valueOf((String) armourSlotData.get("slot")),
                            (Armour) itemTypes.get((String) armourSlotData.get("item")).create());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unable to parse armour in slot: " + armourData, e);
                }
            }
        JSONArray inventoryData = (JSONArray) playerData.get("armour");
        List<Item> inventory = new ArrayList<>();
        for (Object iO : inventoryData) {
            try {
                //TODO: Don't create new
                inventory.add(itemTypes.get((String) iO).create());
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse inventory item: " + iO, e);
            }
        }

        JSONArray questsData = (JSONArray) playerData.get("quests");
        Map<String, QuestProgress> questsProgress = new HashMap<>();
        for (Object qO : questsData) {
            try {
                QuestProgress questProgress = QuestProgress.Provider.parseQuestProgress((JSONObject) qO, quests);
                questsProgress.put(questProgress.quest.name, questProgress);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse quest: " + qO, e);
            }
        }

        try {
            return new Player(
                    intFromObj(playerData.get("maxHealth")),
                    intFromObj(playerData.get("health")),
                    intFromObj(playerData.get("maxFood")),
                    intFromObj(playerData.get("food")),
                    intFromObj(playerData.get("money")),
                    stats, skills, weapon, armour, inventory, questsProgress,
                    intFromObj(playerData.get("x")),
                    intFromObj(playerData.get("y"))
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse player", e);
        }
    }

    public static void savePlayer(BufferedWriter buffer, Player player) {
        //TODO:
    }
}
