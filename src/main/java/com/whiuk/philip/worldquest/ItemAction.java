package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

abstract class ItemAction {
    abstract void perform(WorldQuest game, Tile tile, Player player, int firstItemIndex, int secondItemIndex);

    static class Provider {
        @SuppressWarnings("unused")
        static //TODO: Load ItemActions from buffer
        Map<String, ItemAction> loadItemActionsFromBuffer(ScenarioData data, BufferedReader buffer) throws IOException {
            Map<String, ItemAction> itemActions = new HashMap<>();
            int count = Integer.parseInt(buffer.readLine());
            for (int i = 0; i < count; i++) {
                String[] actionData = buffer.readLine().split(",");
                String itemActionName = actionData[0];
                String type = actionData[1];

                switch (type) {
                    case "StructureCreation":
                        String structureCreationName = actionData[2];
                        itemActions.put(itemActionName, new ItemAction() {
                            @Override
                            void perform(WorldQuest game, Tile tile, Player player, int firemakingTool, int logs) {
                                game.attemptStructureCreation(data.structureCreation.get(structureCreationName), tile, logs);
                            }
                        });
                        break;
                    case "ResourceGathering":
                        String resourceGatheringName = actionData[2];
                        itemActions.put(itemActionName, new ItemAction() {
                            @Override
                            void perform(WorldQuest game, Tile tile, Player player, int shovel, int na) {
                                game.attemptResourceGathering(data.resourceGathering.get(resourceGatheringName), tile);
                            }
                        });
                        break;
                    case "Crafting":
                        String optionName = actionData[2];
                        String recipeListName = actionData[3];
                        itemActions.put(itemActionName, new ItemAction() {
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
        public static void saveItemActionsToBuffer(Map<String, ItemAction> itemActions, BufferedWriter buffer) throws IOException {
        }
    }
}
