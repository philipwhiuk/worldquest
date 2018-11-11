package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GameData {
    private static final int DIFFICULTY = 1;

    static Item BronzeDagger = new Weapon("Bronze dagger", false,"Dagger", 2);
    static Item BronzeSword = new Weapon("Bronze sword", false,"Sword", 4);
    static Item BronzeHatchet = new Hatchet("Bronze hatchet", true,3);
    static Item LeatherTunic = new Armour("Leather tunic", false, Slot.CHEST, 2);
    static Item SteelFlint = new Item("Steel & flint", true);
    static Item Shovel = new Item("Shovel", true);
    static Item Hammer = new Item("Hammer", false);
    static Item Pickaxe = new Item("Pickaxe", false);
    static Item PileOfDirt = new Item("Pile of dirt", false);
    static Item RockShards = new Item("Rock shards", false);

    static Map<String, TileType> tileTypes = new HashMap<>();
    {
        tileTypes.put("Grass", GameData.Grass);
        tileTypes.put("Wall", GameData.Wall);
        tileTypes.put("Floor", GameData.Floor);
        tileTypes.put("Door", GameData.Door);
        tileTypes.put("Dirt", GameData.Dirt);
        tileTypes.put("Rock", GameData.Rock);
    }

    static Map<String, NPCType> npcTypes = new HashMap<>();
    {
        npcTypes.put("Goblin", GameData.Goblin);
        npcTypes.put("Shopkeeper", GameData.ShopKeeper);
    }
    static Map<String, ItemAction> itemUses = new HashMap<>();
    {
        itemUses.put("Steel & flint,Oak logs", GameData.Firemaking);
    }
    static Map<String, ItemAction> tileItemUses = new HashMap<>();
    {
        tileItemUses.put("Grass,Shovel", GameData.Dig);
        tileItemUses.put("Dirt,Shovel", GameData.Dig);
        tileItemUses.put("Rock,Shovel", GameData.Mine);
    }

    static TileType Grass = new TileType(
            "Grass",
            new Color(0,100,0),
            new Color(0,40,0),
            true,
            true);
    static TileType Dirt = new TileType(
            "Dirt",
            new Color(100,68,8),
            new Color(50,34,4),
            true,
            false);
    static TileType Rock = new TileType(
            "Rock",
            new Color(50,50,50),
            new Color(5,5,5),
            true,
            false);
    static TileType Door = new TileType(
            "Door",
            new Color(55,27,0),
            new Color(20,15,0),
            true,
            false);
    static TileType Floor = new TileType(
            "Floor",
            new Color(100,11,0),
            new Color(52,6,0),
            true,
            false);
    static TileType Wall = new TileType(
            "Wall",
            Color.GRAY,
            Color.DARK_GRAY,
            false,
            false);
    static NPCType Goblin = new NPCType(
            "Goblin",
            Color.RED,
            true,
            true,
            true,
            5,
            5*DIFFICULTY,
            new GObjects.ItemDrop[]{
                    new GObjects.ItemDrop(BronzeDagger.copy()),
                    new GObjects.ItemDrop(BronzeSword.copy()),
                    new GObjects.ItemDrop(BronzeHatchet.copy()),
                    new GObjects.ItemDrop(LeatherTunic.copy()),
                    new GObjects.ItemDrop(Shovel.copy()),
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
            false,
            10,
            0,
            new GObjects.ItemDrop[]{},
            true,
            new ConversationChoice(null, "Hello and welcome to my shop!",
                    new ShopDisplay()),
            new Shop("General Store", Arrays.asList(
                    new ShopListing(
                            Hammer.copy(),
                            1, 1, 5),
                    new ShopListing(
                            Shovel.copy(),
                            1, 1, 5),
                    new ShopListing(
                            Pickaxe.copy(),
                            1, 1, 5),
                    new ShopListing(SteelFlint.copy(),
                            1, 1, 5)
                    ))
    );
    static ItemAction Firemaking = new ItemAction() {
        @Override
        void perform(WorldQuest game, Tile tile, Player player, int firemakingTool, int logs) {
            if (tile.isOutdoors()) {
                player.inventory.remove(logs);
                game.spawn(new GObjects.Fire(), player.x, player.y);
                player.gainExperience("Fire-making", 10);
            }
        }
    };
    static ItemAction Dig = new ItemAction() {
        @Override
        void perform(WorldQuest game, Tile tile, Player player, int shovel, int na) {
            if (tile.type == Grass) {
                game.changeTileType(tile, Dirt);
                player.gainExperience("Digging", 10);
                player.inventory.add(PileOfDirt.copy());
            } else if (tile.type == Dirt) {
                game.changeTileType(tile, Rock);
                player.gainExperience("Digging", 10);
                player.inventory.add(PileOfDirt.copy());
            }
        }
    };
    static ItemAction Mine = new ItemAction() {
        @Override
        void perform(WorldQuest game, Tile tile, Player player, int shovel, int na) {
            if (tile.type == Rock) {
                player.gainExperience("Mining", 15);
                player.inventory.add(RockShards.copy());
            }
        }
    };
}
