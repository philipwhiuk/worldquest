package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;

import java.io.IOException;
import java.util.*;

class ResourceGathering {

    static class Provider {

        @SuppressWarnings("unused")
        static //TODO: Load Resource Gathering Methods from buffer
        Map<String, ResourceGathering> loadResourceGatheringFromJson(JSONArray resourceGatheringData) {
            Map<String, ResourceGathering> resourceGathering = new HashMap<>();
            resourceGathering.put("Mine", new ResourceGathering(
                    "Rock",
                    "Rock",
                    "RockShards",
                    Arrays.asList("CopperVein", "TinVein"),
                    "Mining",
                    15));
            resourceGathering.put("DigGrass", new ResourceGathering(
                    "Grass",
                    "Dirt",
                    "PileOfDirt",
                    Collections.emptyList(),
                    "Digging",
                    10));
            resourceGathering.put("DigDirt", new ResourceGathering(
                    "Grass",
                    "Rock",
                    "PileOfDirt",
                    Collections.emptyList(),
                    "Digging",
                    10));
            resourceGathering.put("Fish", new ResourceGathering(
                    "Water",
                    "Water",
                    "RawCatfish",
                    Collections.emptyList(),
                    "Fishing",
                    10));
            return resourceGathering;
        }
    }

    public static class Persistor {
        public static JSONArray saveResourceGatheringToJson(Map<String, ResourceGathering> resourceGathering) throws IOException {
            //TODO: Save Resource Gathering Methods from buffer
            return new JSONArray();
        }
    }

    final String tileType;
    final String newTileType;
    final String baseProduct;
    final List<String> extractableResourceProviders;
    final String skill;
    final int experience;

    ResourceGathering(String tileType, String newTileType, String baseProduct,
                      List<String> extractableResourceProviders, String skill, int experience) {
        this.tileType = tileType;
        this.newTileType = newTileType;
        this.baseProduct = baseProduct;
        this.extractableResourceProviders = extractableResourceProviders;
        this.skill = skill;
        this.experience = experience;
    }

    void gather(WorldQuest game, Player player, Tile tile) {
        if (tile.type == game.scenarioData.tileTypeByName(tileType)) {
            if (player.inventory.hasSpaceForItemOfType(game.scenarioData.itemType(baseProduct))) {
                player.gainExperience(skill, experience);
                //TODO: Quality
                player.inventory.add(game.scenarioData.itemType(baseProduct).create());
                for (GObjects.GameObject object : tile.objects) {
                    if (object instanceof GObjects.ResourceProvider) {
                        GObjects.ResourceProvider resourceProvider = (GObjects.ResourceProvider) object;
                        if (extractableResourceProviders.contains(resourceProvider.name)) {
                            ItemType itemType = game.scenarioData.itemType(resourceProvider.resource);
                            if (player.inventory.hasSpaceForItemOfType(itemType)) {
                                player.inventory.add(resourceProvider.extract(game));
                            } else {
                                game.eventMessage("No space to take the " + itemType.name);
                            }
                        }
                    }
                }
                if (!newTileType.equals(tileType)) {
                    game.changeTileType(tile, game.scenarioData.tileTypeByName(newTileType));
                }
            } else {
                game.eventMessage("No space in your inventory");
            }
        }
    }
}