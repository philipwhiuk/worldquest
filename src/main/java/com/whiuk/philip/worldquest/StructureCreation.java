package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StructureCreation {

    static class Provider {
        static Map<String, StructureCreation> loadStructureCreationFromJson(JSONArray structureCreationData) {
            //TODO: Load from JSON
            Map<String, StructureCreation> structureCreators = new HashMap<>();
            structureCreators.put(
                "Firemaking",
                    new StructureCreation(
                    new GObjects.FireBuilder(),
                    new JSONObject(),
                    true,
                    "You probably shouldn't make a fire here.",
                    "Fire-making",
                    10));
            return structureCreators;
        }
    }

    private GObjects.GameObjectBuilder gameObjectBuilder;
    private JSONObject buildArgs;
    private boolean requiresOutdoors;
    private String skill;
    int experience;
    private String unableMessage;

    StructureCreation(GObjects.GameObjectBuilder gameObjectBuilder, JSONObject buildArgs,
                      boolean requiresOutdoors, String unableMessage, String skill, int experience) {
        this.gameObjectBuilder = gameObjectBuilder;
        this.buildArgs = buildArgs;
        this.skill = skill;
        this.experience = experience;
        this.requiresOutdoors = requiresOutdoors;
        this.unableMessage = unableMessage;
    }

    void create(WorldQuest game, Player player, Tile tile, int item) {
        if (!requiresOutdoors || tile.isOutdoors()) {
            player.inventory.remove(item);
            game.spawn(gameObjectBuilder.build(buildArgs), player.x, player.y);
            player.gainExperience(skill, 10);
        } else {
            game.eventMessage(unableMessage);
        }
    }
}
