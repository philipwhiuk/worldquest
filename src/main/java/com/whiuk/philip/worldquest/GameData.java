package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GameData {
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
    private static Item CopperOre = new Item("Copper ore", true);
    private static Item TinOre = new Item("Tin ore", true);
    private static Item BronzeBar = new Item("Bronze bar", true);
    private static Item OakLogs = new Item("Oak logs", true);
    private static Item Feathers = new Item("Feathers", true);
    private static Item Bones = new Item("Bones", true);
    private static Item RawChicken = new Item("Raw chicken", true);
    private static HashMap<String, Integer> GoblinSlayerKills = new HashMap<>();
    static {
        GoblinSlayerKills.put("Goblin", 5);
    }
    private static QuestStep GoblinSlaying = new QuestStep(GoblinSlayerKills);
    private static Quest GoblinSlayer = new Quest(
            "Goblin Slayer",
            Arrays.asList(GoblinSlaying),
            0,
            false);

    private static TileType Grass = new TileType(
            "Grass",
            new Color(0,100,0),
            new Color(0,40,0),
            true,
            true,
            false);
    private static TileType Dirt = new TileType(
            "Dirt",
            new Color(100,68,8),
            new Color(50,34,4),
            true,
            false,
            false);
    private static TileType Rock = new TileType(
            "Rock",
            new Color(50,50,50),
            new Color(25,25,25),
            true,
            true,
            false);
    private static TileType Floor = new TileType(
            "Floor",
            new Color(100,11,0),
            new Color(52,6,0),
            true,
            false,
            false);
    private static TileType Wall = new TileType(
            "Wall",
            new Color(100,100,100),
            new Color(75,75,75),
            false,
            true,
            true);
    private static NPCType Chicken = new NPCType(
            "Chicken",
            Color.WHITE,
            true,
            true,
            false,
            2,
            1,
            new GObjects.ItemDrop[]{
                    new GObjects.ItemDrop(Feathers.copy()),
                    new GObjects.ItemDrop(Bones.copy()),
                    new GObjects.ItemDrop(RawChicken.copy())
            },
            false,
            null,
            null);
    private static NPCType Rat = new NPCType(
            "Rat",
            Color.LIGHT_GRAY,
            true,
            true,
            false,
            2,
            1,
            new GObjects.ItemDrop[]{
            },
            false,
            null,
            null);
    private static NPCType GiantRat = new NPCType(
            "Giant Rat",
            Color.GRAY,
            true,
            true,
            false,
            5,
            1,
            new GObjects.ItemDrop[]{
            },
            false,
            null,
            null);
    private static NPCType Goblin = new NPCType(
            "Goblin",
            Color.RED,
            true,
            true,
            true,
            5,
            5,
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
            10,
            new GObjects.ItemDrop[]{
                    new GObjects.ItemDrop(100),
            },
            false,
            null,
            null);
    private static ConversationChoice ShopDialog = new ConversationChoice(
            null,
            "Hello and welcome to my shop!",
            new ShopDisplay(),
            (state) -> true
    );
    private static Shop GeneralStore = new Shop("General Store", Arrays.asList(
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
    ));
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
            new Conversation(state -> ShopDialog),
            GeneralStore
    );

    private static ConversationChoice King_StartGoblinSlayerDialog = new ConversationChoice(
            "What can I do for you my King?",
            "The goblins are causing havoc - kill them!",
            new ConversationChoiceSelection(Arrays.asList(
                    new ConversationChoice(
                            "Okay I'll take the job.",
                            "Thank you!",
                            new QuestStartAction(GoblinSlayer.name),
                            (state) -> true
                    ),
                    new ConversationChoice(
                            "Sorry I like Goblins",
                            "Bah! Off with you.",
                            null,
                            (state) -> true
                    )
            )),
            (state -> state.player.getQuestState(GoblinSlayer.name).equals(QuestStatus.NOT_STARTED))
    );

    private static ConversationChoice King_ShopQuestion = new ConversationChoice(
            "Is there a shop nearby?",
            "Gerald runs a store just across the road from the castle",
            null,
            (state) -> true
    );

    private static ConversationChoice King_DefaultOpening = new ConversationChoice(
            "Greetings Your Majesty",
            "Hello player!",
            new ConversationChoiceSelection(Arrays.asList(
                    King_StartGoblinSlayerDialog,
                    King_ShopQuestion
            )),
            (state) -> true
    );

    private static ConversationChoice King_NotFinishedYetSorry = new ConversationChoice(
            "I'm not finished yet...",
            "Well do hurry please, they're a real menace",
            null,
            (state -> !state.player.getQuest(GoblinSlayer.name).isQuestComplete())
    );

    private static ConversationChoice King_Finished = new ConversationChoice(
            "I've finished!",
            "Congratulations. Thanks for helping!",
            new QuestFinishAction(GoblinSlayer.name),
            (state -> state.player.getQuest(GoblinSlayer.name).isQuestComplete())
    );

    private static ConversationChoice King_SomethingElse = new ConversationChoice(
            "Actually I'm here about something else",
            "Oh - well what is it?",
            new ConversationChoiceSelection(Arrays.asList(
                    King_ShopQuestion
            )),
            (state -> true)
    );

    private static ConversationChoice King_GoblinSlayerProgressRequest = new ConversationChoice(
            "Greetings Your Majesty",
            "Hello! How's the goblin hunting going?",
            new ConversationChoiceSelection(Arrays.asList(
                    King_NotFinishedYetSorry,
                    King_Finished,
                    King_SomethingElse
            )),
            state -> true
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
            new Conversation(
                    state -> {
                        switch (state.player.getQuestState(GoblinSlayer.name)) {
                            case NOT_STARTED:
                            case FINISHED:
                                return King_DefaultOpening;
                            case STARTED:
                                return King_GoblinSlayerProgressRequest;
                        }
                        return King_DefaultOpening;
                    }
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
                if (player.inventory.hasSpaceForItem(PileOfDirt)) {
                    game.changeTileType(tile, Dirt);
                    player.gainExperience("Digging", 10);
                    player.inventory.add(PileOfDirt.copy());
                } else {
                    game.eventMessage("You don't have space in your inventory");
                }
            } else if (tile.type == Dirt) {
                if (player.inventory.hasSpaceForItem(PileOfDirt)) {
                    game.changeTileType(tile, Rock);
                    player.gainExperience("Digging", 10);
                    player.inventory.add(PileOfDirt.copy());
                } else {
                    game.eventMessage("You don't have space in your inventory");
                }
            }
        }
    };
    private static ItemAction Mine = new ItemAction() {
        @Override
        void perform(WorldQuest game, Tile tile, Player player, int shovel, int na) {
            if (tile.type == Rock) {
                if (player.inventory.hasSpaceForItem(RockShards)) {
                    player.gainExperience("Mining", 15);
                    player.inventory.add(RockShards.copy());
                    for (GObjects.GameObject object : tile.objects) {
                        if (object instanceof GObjects.MineralVein) {
                            GObjects.MineralVein vein = (GObjects.MineralVein) object;
                            if (player.inventory.hasSpaceForItem(vein.resource)) {
                                player.inventory.add(vein.mine());
                            } else {
                                game.eventMessage("No space to take the " + vein.resource.name);
                            }
                        }
                    }
                } else {
                    game.eventMessage("No space in your inventory");
                }
            }
        }
    };
    private static ItemAction Smelt = new ItemAction() {
        @Override
        void perform(WorldQuest game, Tile tile, Player player, int ore1, int na) {
            for (Recipe recipe : recipeList.get("Smelt")) {
                if (recipe.input.contains(player.inventory.get(ore1)) && recipe.canBeDone(player)) {
                    for (Item item : recipe.input) {
                        player.inventory.remove(item);
                    }
                    boolean success = recipe.isSuccess();
                    if (success) {
                        for (Map.Entry<String, Integer> experience : recipe.experienceGained.entrySet()) {
                            String skill = experience.getKey();
                            int expGain = experience.getValue();
                            Experience skillXp = player.skills.getOrDefault(skill, Experience.NoExperience());
                            skillXp.experience += expGain;
                            player.skills.put(skill, skillXp);
                        }
                        for (Item item: recipe.output) {
                            player.inventory.add(item.copy());
                        }
                    }
                }
            }
        }
    };
    private static ItemAction Smith = new ItemAction() {
        @Override
        void perform(WorldQuest game, Tile tile, Player player, int bar, int na) {
            if (player.inventory.containsItem("Hammer")) {
                //TODO: Show smithing screen for bar.
                System.out.println("Smithing WIP");
            }
        }
    };


    private static Recipe Bronze = new Recipe(
            Arrays.asList(CopperOre, TinOre),
            Arrays.asList(BronzeBar),
            100,
            Collections.emptyMap(),
            Collections.singletonMap("Smelting", 10));

    private static Map<String, List<Recipe>> recipeList = new HashMap<>();
    static {
        recipeList.put("Smelt", Arrays.asList(Bronze));
    }

    static Map<Integer, TileType> tileTypes = new HashMap<>();
    static {
        tileTypes.put(1, GameData.Grass);
        tileTypes.put(2, GameData.Wall);
        tileTypes.put(3, GameData.Floor);
        tileTypes.put(4, GameData.Dirt);
        tileTypes.put(5, GameData.Rock);
    }

    static Map<String, NPCType> npcTypes = new HashMap<>();
    static {
        npcTypes.put("Goblin", GameData.Goblin);
        npcTypes.put("GoblinKing", GameData.GoblinKing);
        npcTypes.put("Shopkeeper", GameData.ShopKeeper);
        npcTypes.put("King", GameData.King);
        npcTypes.put("GiantRat", GameData.GiantRat);
        npcTypes.put("Chicken", GameData.Chicken);
        npcTypes.put("Rat", GameData.Rat);
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
    static Map<String, ItemAction> objectItemUses = new HashMap<>();
    static {
        objectItemUses.put("Furnace,Copper ore", GameData.Smelt);
        objectItemUses.put("Furnace,Tin ore", GameData.Smelt);
        objectItemUses.put("Anvil,Bronze bar", GameData.Smith);
    }
    static Map<String, Quest> questList = new HashMap<>();
    static {
        questList.put(GoblinSlayer.name, GoblinSlayer);
    }
}
