package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GameData {
    private static final int DIFFICULTY = 1;

    private static Item BronzeDagger = new Weapon("Bronze dagger", false,"Dagger", 2);
    private static Item BronzeSword = new Weapon("Bronze sword", false,"Sword", 4);
    private static Item BronzeHatchet = new Hatchet("Bronze hatchet", true,3);
    private static Item LeatherTunic = new Armour("Leather tunic", false, Slot.CHEST, 2);
    private static Item SteelFlint = new Item("Steel & flint", true);
    private static Item Shovel = new Item("Shovel", true);
    private static Item Hammer = new Item("Hammer", false);
    private static Item Pickaxe = new Item("Pickaxe", true);
    private static Item PileOfDirt = new Item("Pile of dirt", false);
    private static Item RockShards = new Item("Rock shards", false);

    private static TileType Grass = new TileType(
            "Grass",
            new Color(0,100,0),
            new Color(0,40,0),
            true,
            true);
    private static TileType Dirt = new TileType(
            "Dirt",
            new Color(100,68,8),
            new Color(50,34,4),
            true,
            false);
    private static TileType Rock = new TileType(
            "Rock",
            new Color(50,50,50),
            new Color(25,25,25),
            true,
            false);
    private static TileType Door = new TileType(
            "Door",
            new Color(55,27,0),
            new Color(20,15,0),
            true,
            false);
    private static TileType Floor = new TileType(
            "Floor",
            new Color(100,11,0),
            new Color(52,6,0),
            true,
            false);
    private static TileType Wall = new TileType(
            "Wall",
            new Color(100,100,100),
            new Color(75,75,75),
            false,
            false);
    private static NPCType Goblin = new NPCType(
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
    private static NPCType GoblinKing = new NPCType(
            "Goblin King",
            Color.PINK,
            true,
            true,
            true,
            50,
            10*DIFFICULTY,
            new GObjects.ItemDrop[]{
                    new GObjects.ItemDrop(100),
            },
            false,
            null,
            null);
    private static NPCType ShopKeeper = new NPCType(
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
    private static NPCType King = new NPCType(
            "King Ronald",
            Color.CYAN,
            false,
            false,
            false,
            100,
            0,
            new GObjects.ItemDrop[]{},
            true,
            new ConversationChoice(
                    "Greetings Your Majesty",
                    "Hello player!",
                    new ConversationChoiceSelection(Arrays.asList(
                            new ConversationChoice("What can I do for you my King?",
                                    "The goblins are causing havoc!",
                                    new ConversationChoiceSelection(Arrays.asList(
                                            new ConversationChoice("Okay I'll take the job!",
                                            "Thank you - return to me when removed them from the kingdom!",
                                            new QuestStartAction("GoblinSlayer")
                                            ),
                                        new ConversationChoice(
                                            "Sorry I like Goblins",
                                            "Bah! Off with you.",
                                            null
                                            )
                                    ))
                            ),
                            new ConversationChoice(
                                    "Is there a shop nearby?",
                                    "Gerald runs a store just across the road from the castle",
                                    null
                            )
                    ))
            ),
            null
    );

    private static ItemAction Firemaking = new ItemAction() {
        @Override
        void perform(WorldQuest game, Tile tile, Player player, int firemakingTool, int logs) {
            if (tile.isOutdoors()) {
                player.inventory.remove(logs);
                game.spawn(new GObjects.Fire(), player.x, player.y);
                player.gainExperience("Fire-making", 10);
            }
        }
    };
    private static ItemAction Dig = new ItemAction() {
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
    private static ItemAction Mine = new ItemAction() {
        @Override
        void perform(WorldQuest game, Tile tile, Player player, int shovel, int na) {
            if (tile.type == Rock) {
                player.gainExperience("Mining", 15);
                player.inventory.add(RockShards.copy());
            }
        }
    };

    static Map<String, TileType> tileTypes = new HashMap<>();
    static {
        tileTypes.put("Grass", GameData.Grass);
        tileTypes.put("Wall", GameData.Wall);
        tileTypes.put("Floor", GameData.Floor);
        tileTypes.put("Door", GameData.Door);
        tileTypes.put("Dirt", GameData.Dirt);
        tileTypes.put("Rock", GameData.Rock);
    }

    static Map<String, NPCType> npcTypes = new HashMap<>();
    static {
        npcTypes.put("Goblin", GameData.Goblin);
        npcTypes.put("GoblinKing", GameData.GoblinKing);
        npcTypes.put("Shopkeeper", GameData.ShopKeeper);
        npcTypes.put("King", GameData.King);
    }
    static Map<String, ItemAction> itemUses = new HashMap<>();
    static {
        itemUses.put("Steel & flint,Oak logs", GameData.Firemaking);
    }
    static Map<String, ItemAction> tileItemUses = new HashMap<>();
    static {
        tileItemUses.put("Grass,Shovel", GameData.Dig);
        tileItemUses.put("Dirt,Shovel", GameData.Dig);
        tileItemUses.put("Rock,Pickaxe", GameData.Mine);
    }

}
