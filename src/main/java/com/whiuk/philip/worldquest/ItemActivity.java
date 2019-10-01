package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

abstract class ItemActivity {
    abstract void perform(WorldQuest game, Tile tile, Player player, int firstItemIndex, int secondItemIndex);

    static class Provider {
        @SuppressWarnings("unused")
        static Map<String, ItemActivity> loadItemActivitiesFromJson(ScenarioData data, JSONArray itemActivitiesData) {
            Map<String, ItemActivity> itemActions = new HashMap<>();
            for (Object iAO: itemActivitiesData) {
                JSONObject activityData = (JSONObject) iAO;
                String itemActivityName = (String) activityData.get("name");
                String type = (String) activityData.get("type");

                switch (type) {
                    case "StructureCreation":
                        String structureCreationName = (String) activityData.get("structureCreationName");
                        itemActions.put(itemActivityName, new ItemActivity() {
                            @Override
                            void perform(WorldQuest game, Tile tile, Player player, int firemakingTool, int logs) {
                                game.attemptStructureCreation(data.structureCreation.get(structureCreationName), tile, logs);
                            }
                        });
                        break;
                    case "ResourceGathering":
                        String resourceGatheringName = (String) activityData.get("resourceGatheringName");
                        itemActions.put(itemActivityName, new ItemActivity() {
                            @Override
                            void perform(WorldQuest game, Tile tile, Player player, int shovel, int na) {
                                game.attemptResourceGathering(data.resourceGathering.get(resourceGatheringName), tile);
                            }
                        });
                        break;
                    case "Crafting":
                        String optionName = (String) activityData.get("optionName");
                        String recipeListName = (String) activityData.get("recipeListName");
                        itemActions.put(itemActivityName, new ItemActivity() {
                            @Override
                            void perform(WorldQuest game, Tile tile, Player player, int ore1, int na) {
                                game.showCrafting(new CraftingOptions(optionName, data.recipeList.get(recipeListName)));
                            }
                        });
                        break;
                }
            }
            return itemActions;
        }
    }

    public static class Persistor {
        //TODO Load and save
        public static JSONArray saveItemActivitiesToJson(Map<String, ItemActivity> itemActions) throws IOException {
            return new JSONArray();
        }
    }
}
