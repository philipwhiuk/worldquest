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
        Map<String, ItemAction> loadItemActionsFromBuffer(ScenarioData data, BufferedReader buffer) {
            Map<String, ItemAction> itemActions = new HashMap<>();
            itemActions.put("Firemaking", new ItemAction() {
                @Override
                void perform(WorldQuest game, Tile tile, Player player, int firemakingTool, int logs) {
                    if (tile.isOutdoors()) {
                        player.inventory.remove(logs);
                        game.spawn(new GObjects.Fire(), player.x, player.y);
                        player.gainExperience("Fire-making", 10);
                    } else {
                        game.eventMessage("You probably shouldn't make a fire here.");
                    }
                }
            });
            itemActions.put("DigGrass", new ItemAction() {
                @Override
                void perform(WorldQuest game, Tile tile, Player player, int shovel, int na) {
                    game.attemptResourceGathering(data.resourceGathering.get("DigGrass"), tile);
                }
            });
            itemActions.put("DigDirt", new ItemAction() {
                @Override
                void perform(WorldQuest game, Tile tile, Player player, int shovel, int na) {
                    game.attemptResourceGathering(data.resourceGathering.get("DigDirt"), tile);
                }
            });
            itemActions.put("Mine", new ItemAction() {
                @Override
                void perform(WorldQuest game, Tile tile, Player player, int pickaxe, int na) {
                    game.attemptResourceGathering(data.resourceGathering.get("Mine"), tile);
                }
            });
            itemActions.put("Smelt", new ItemAction() {
                @Override
                void perform(WorldQuest game, Tile tile, Player player, int ore1, int na) {
                    game.showCrafting(new CraftingOptions("Smelting", data.recipeList.get("Smelt")));
                }
            });
            itemActions.put("Smith", new ItemAction() {
                @Override
                void perform(WorldQuest game, Tile tile, Player player, int hammer, int na) {
                    game.showCrafting(new CraftingOptions("Smithing", data.recipeList.get("Smith")));
                }
            });
            itemActions.put("Fish", new ItemAction() {
                @Override
                void perform(WorldQuest game, Tile tile, Player player, int rod, int na) {
                    game.attemptResourceGathering(data.resourceGathering.get("Fish"), tile);
                }
            });
            itemActions.put("Cook", new ItemAction() {
                @Override
                void perform(WorldQuest game, Tile tile, Player player, int food, int na) {
                    game.showCrafting(new CraftingOptions("Cooking", data.recipeList.get("Cook")));
                }
            });
            return itemActions;
        }
    }

    public static class Persistor {
        //TODO Load and save
        public static void saveItemActionsToBuffer(Map<String, ItemAction> itemActions, BufferedWriter buffer) throws IOException {
        }
    }
}
