package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

class ResourceGathering {

    static class Provider {

        @SuppressWarnings("unused")
        static //TODO: Load Resource Gathering Methods from buffer
        Map<String, ResourceGathering> loadResourceGatheringFromBuffer(BufferedReader buffer) {
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
        public static void saveResourceGatheringToBuffer(Map<String, ResourceGathering> resourceGathering, BufferedWriter buffer) throws IOException {
            //TODO: Save Resource Gathering Methods from buffer
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
            if (player.inventory.hasSpaceForItem(game.scenarioData.item(baseProduct))) {
                player.gainExperience(skill, experience);
                player.inventory.add(game.scenarioData.item(baseProduct).copy());
                for (GObjects.GameObject object : tile.objects) {
                    if (object instanceof GObjects.ResourceProvider) {
                        GObjects.ResourceProvider resourceProvider = (GObjects.ResourceProvider) object;
                        if (extractableResourceProviders.contains(resourceProvider.name)) {
                            if (player.inventory.hasSpaceForItem(resourceProvider.resource)) {
                                player.inventory.add(resourceProvider.extract());
                            } else {
                                game.eventMessage("No space to take the " + resourceProvider.resource.name);
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