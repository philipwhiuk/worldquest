package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

public class StructureCreation {

    static class Provider {
        static Map<String, StructureCreation> loadStructureCreationFromBuffer(BufferedReader buffer) {
            Map<String, StructureCreation> structureCreators = new HashMap<>();
            structureCreators.put(
                "Firemaking",
                    new StructureCreation(
                    new GObjects.FireBuilder(),
                    new String[]{},
                    true,
                    "You probably shouldn't make a fire here.",
                    "Fire-making",
                    10));
            return structureCreators;
        }
    }

    GObjects.GameObjectBuilder gameObjectBuilder;
    String[] buildArgs;
    boolean requiresOutdoors;
    String skill;
    int experience;
    String unableMessage;

    StructureCreation(GObjects.GameObjectBuilder gameObjectBuilder, String buildArgs[],
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

        } else {
            game.eventMessage(unableMessage);
        }
        player.inventory.remove(item);
        game.spawn(gameObjectBuilder.build(buildArgs), player.x, player.y);
        player.gainExperience(skill, 10);
    }
}
