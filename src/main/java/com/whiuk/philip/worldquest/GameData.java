package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.Arrays;

//TODO: Extract this into scenario file
public class GameData {
    private static final int DIFFICULTY = 1;

    static TileType Grass = new TileType(
            new Color(0,100,0),
            new Color(0,40,0),
            true,
            true);
    static TileType Door = new TileType(
            new Color(55,27,0),
            new Color(20,15,0),
            true,
            false);
    static TileType Floor = new TileType(
            new Color(100,68,8),
            new Color(50,34,4),
            true,
            false);
    static TileType Wall = new TileType(
            Color.GRAY,
            Color.DARK_GRAY,
            false,
            false);
    static NPCType Goblin = new NPCType(
            "Goblin",
            Color.RED,
            true,
            true,
            5,
            5*DIFFICULTY,
            new GObjects.ItemDrop[]{
                    new GObjects.ItemDrop(new Weapon("Bronze dagger", false,"Dagger", 2)),
                    new GObjects.ItemDrop(new Weapon("Bronze sword", false,"Sword", 4)),
                    new GObjects.ItemDrop(new Hatchet("Bronze hatchet", true,3)),
                    new GObjects.ItemDrop(new Armour("Leather tunic", false, Slot.CHEST, 2)),
                    new GObjects.ItemDrop(new Item("Steel & flint", true)),
                    new GObjects.ItemDrop(5),
                    new GObjects.ItemDrop(10),
            },
            false,
            null,
            null);
    static NPCType ShopKeeper = new NPCType(
            "Shopkeeper",
            Color.WHITE,
            false,
            false,
            10,
            0,
            new GObjects.ItemDrop[]{},
            true,
            new ConversationChoice(null, "Hello and welcome to my shop!",
                    new ShopDisplay()),
            new Shop("General Store", Arrays.asList(
                    new ShopListing(
                            new Item("Hammer", false),
                            1, 1, 5)))
    );
    static ItemAction Firemaking = new ItemAction() {
        @Override
        void perform(WorldQuest game, Tile tile, Player player, int firemakingTool, int logs) {
            if (tile.isOutdoors()) {
                player.inventory.remove(logs);
                game.spawn(new GObjects.Fire(), player.x, player.y);
            } else {
                //TODO: Error message
            }
        }
    };
}
