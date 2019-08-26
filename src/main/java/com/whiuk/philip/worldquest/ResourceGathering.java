package com.whiuk.philip.worldquest;

import java.util.List;

class ResourceGathering {
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