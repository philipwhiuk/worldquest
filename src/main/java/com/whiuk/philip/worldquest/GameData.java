package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.Arrays;

public class GameData {
    private static final int DIFFICULTY = 1;

    static TileType Grass = new TileType(
            new Color(0,100,0),
            new Color(0,40,0),
            true);
    static TileType Door = new TileType(
            new Color(55,27,0),
            new Color(20,15,0),
            true);
    static TileType Floor = new TileType(
            new Color(100,68,8),
            new Color(50,34,4),
            true);
    static TileType Wall = new TileType(
            Color.GRAY,
            Color.DARK_GRAY,
            false);
    static NPCType Goblin = new NPCType(
            "Goblin",
            Color.RED,
            true,
            true,
            5,
            2*DIFFICULTY,
            new GObjects.ItemDrop[]{
                    new GObjects.ItemDrop(new Weapon("Bronze dagger", false,2)),
                    new GObjects.ItemDrop(new Weapon("Bronze sword", false,4)),
                    new GObjects.ItemDrop(new Hatchet("Bronze hatchet", false,3)),
                    new GObjects.ItemDrop(new Armour("Leather tunic", false, Slot.CHEST)),
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
        void perform(WorldQuest game, Player player, int firemakingTool, int logs) {
            player.inventory.remove(logs);
            game.spawn(new GObjects.Fire(), player.x, player.y);
        }
    };
}
