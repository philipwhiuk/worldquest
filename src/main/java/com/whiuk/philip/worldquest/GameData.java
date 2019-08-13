package com.whiuk.philip.worldquest;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class GameData {
    private Map<String, Item> items = new HashMap<>();
    private Map<String, QuestStep> questSteps = new HashMap<>();
    Map<String, Quest> quests = new HashMap<>();
    Map<Integer, TileType> tileTypes = new HashMap<>();
    Map<String, TileType> tileTypesByName = new HashMap<>();
    private Map<String, Shop> shops = new HashMap<>();
    Map<String, NPCType> npcTypes = new HashMap<>();
    Map<String, ConversationChoice> conversationChoices = new HashMap<>();
    Map<String, ItemAction> itemActions = new HashMap<>();
    private Map<String, List<Recipe>> recipeList = new HashMap<>();
    private Map<String, ResourceGathering> resourceGathering = new HashMap<>();
    public int playerStartX;
    public int playerStartY;
    HashMap<String, ItemAction> itemUses;
    Map<String, ItemAction> tileItemUses;
    Map<String, ItemAction> objectItemUses;

    GameData(String scenario) {

        try(
                InputStream mapDataStream = new FileInputStream(GameFileUtils.resourceInScenarioFolder(scenario, "scenario"));
                BufferedReader buffer = new BufferedReader(new InputStreamReader(mapDataStream))){
            loadItems(buffer);
            loadQuestSteps(buffer);
            loadQuests(buffer);
            loadTileTypes(buffer);
            loadConversationChoices(buffer);
            loadShops(buffer);
            loadNPCTypes(buffer);
            loadItemActions(buffer);
            loadRecipes(buffer);
            loadResourceGathering(buffer);
            loadUses(buffer);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load scenario: " + e.getMessage(), e);
        }
    }

    private void loadResourceGathering(BufferedReader buffer) {
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
    }

    private void loadConversationChoices(BufferedReader buffer) throws IOException {
        int choicesCount = Integer.parseInt(buffer.readLine());
        for (int c = 0 ; c < choicesCount; c++) {
            String[] choiceData = buffer.readLine().split(",");
            String id = choiceData[0];
            String playerText = choiceData[1];
            String npcText = choiceData[2];
            boolean hasNpcAction = Boolean.parseBoolean(choiceData[3]);
            boolean hasCanSee = Boolean.parseBoolean(choiceData[4]);
            NPCAction npcAction = null;
            Predicate<QuestState> canSee = null;
            if (hasNpcAction) {
                String[] npcActionData = buffer.readLine().split(",");
                switch (npcActionData[0]) {
                    case "ShopDialog":
                        npcAction = new ShopDisplay();
                        break;
                    case "ConversationChoiceSelection":
                        int qSCount = Integer.parseInt(npcActionData[1]);
                        List<String> choices = new ArrayList<>();
                        for (int q = 0; q < qSCount ; q++) {
                            choices.add(buffer.readLine());
                        }
                        npcAction = new ConversationChoiceSelection(choices);
                        break;
                    case "QuestStartAction":
                        npcAction = new QuestStartAction(npcActionData[1]);
                        break;
                    case "QuestFinishAction":
                        npcAction = new QuestFinishAction(npcActionData[1]);
                }
            }
            if (hasCanSee) {
                String[] canSeeData = buffer.readLine().split(",");
                switch (canSeeData[0]) {
                    case "Always":
                        canSee = (state) -> true;
                        break;
                    case "QuestState":
                        int qSCount = Integer.parseInt(canSeeData[1]);
                        HashMap<String, QuestStatus> questState = new HashMap<>();
                        for (int q = 0; q < qSCount ; q++) {
                            String[] qsData = buffer.readLine().split(",");
                            questState.put(qsData[0], QuestStatus.valueOf(qsData[1]));
                        }
                        canSee = new QuestStatePredicate(questState);
                        break;
                }
            }

            conversationChoices.put(id, new ConversationChoice(playerText, npcText, npcAction, canSee));
        }
    }

    private void loadItems(BufferedReader buffer) throws IOException {
            int itemCount = Integer.parseInt(buffer.readLine());
            for (int i = 0; i < itemCount; i++) {
                String[] itemData = buffer.readLine().split(",");
                String itemID = itemData[0];
                String itemClass = itemData[1];
                switch(itemClass) {
                    case "Item":
                        items.put(itemID, new Item(itemData[2], Boolean.parseBoolean(itemData[3])));
                        break;
                    case "Weapon":
                        items.put(itemID, new Weapon(itemData[2],
                                Boolean.parseBoolean(itemData[3]), itemData[4], Integer.parseInt(itemData[5])));
                        break;
                    case "Hatchet":
                        items.put(itemID, new Hatchet(itemData[2],
                                Boolean.parseBoolean(itemData[3]), Integer.parseInt(itemData[4])));
                        break;
                    case "Armour":
                        items.put(itemID, new Armour(itemData[2], Boolean.parseBoolean(itemData[3]),
                                Slot.valueOf(itemData[4]), Integer.parseInt(itemData[5])));
                        break;
                }

            }
    }

    private void loadQuestSteps(BufferedReader buffer) throws IOException {
        int questStepsCount = Integer.parseInt(buffer.readLine());
        for (int i = 0; i < questStepsCount; i++) {
            String[] questStepData = buffer.readLine().split(",");
            String questStepID = questStepData[0];
            int killTypesCount = Integer.parseInt(questStepData[1]);
            Map<String, Integer> killCounts = new HashMap<>();
            for (int j = 0; j < killTypesCount; j++) {
                String[] killCountData = buffer.readLine().split(",");
                String killType = killCountData[0];
                int count = Integer.parseInt(killCountData[1]);
                killCounts.put(killType, count);
            }
            questSteps.put(questStepID, new QuestStep(killCounts));
        }
    }

    private void loadQuests(BufferedReader buffer) throws IOException {
        int questsCount = Integer.parseInt(buffer.readLine());
        for (int i = 0; i < questsCount; i++) {
            String[] questData = buffer.readLine().split(",");
            String questID = questData[0];
            String questName = questData[1];
            int stepsCount = Integer.parseInt(questData[2]);
            int stepIndex = Integer.parseInt(questData[3]);
            QuestStatus status = QuestStatus.valueOf(questData[4]);
            List<QuestStep> steps = new ArrayList<>();
            for (int s = 0; s < stepsCount; s++) {
                steps.add(questSteps.get(buffer.readLine()));
            }
            quests.put(questID, new Quest(questName,steps,stepIndex,status));
        }
    }

    private void loadTileTypes(BufferedReader buffer) throws IOException {
        int tileTypesCount = Integer.parseInt(buffer.readLine());
        for (int t = 0; t < tileTypesCount; t++) {
            String[] tileTypeData = buffer.readLine().split(",");
            String tileRefId =  tileTypeData[0];
            int id =  Integer.parseInt(tileTypeData[1]);
            String name = tileTypeData[2];
            Color color = new Color(Integer.parseInt(tileTypeData[3]),Integer.parseInt(tileTypeData[4]),Integer.parseInt(tileTypeData[5]));
            Color fowColor = new Color(Integer.parseInt(tileTypeData[6]),Integer.parseInt(tileTypeData[7]),Integer.parseInt(tileTypeData[8]));
            boolean canMoveTo = Boolean.parseBoolean(tileTypeData[9]);
            boolean isOutdoors = Boolean.parseBoolean(tileTypeData[10]);
            boolean blocksView = Boolean.parseBoolean(tileTypeData[11]);
            TileType tileType = new TileType(
                    id,
                    name,
                    color,
                    fowColor,
                    canMoveTo,
                    isOutdoors,
                    blocksView);
            tileTypes.put(id, tileType);
            tileTypesByName.put(tileRefId, tileType);
        }
    }

    private void loadShops(BufferedReader buffer) throws IOException {
        int shopsCount = Integer.parseInt(buffer.readLine());
        for (int s = 0; s < shopsCount; s++) {
            String[] shopData = buffer.readLine().split(",");
            String id = shopData[0];
            String name = shopData[1];
            int itemCount = Integer.parseInt(shopData[2]);
            List<ShopListing> shopListings = new ArrayList<>();
            for (int i = 0; i < itemCount; i++) {
                String[] shopListingData = buffer.readLine().split(",");

                shopListings.add(new ShopListing(
                    items.get(shopListingData[0]).copy(),
                    Integer.parseInt(shopListingData[1]),
                    Integer.parseInt(shopListingData[2]),
                    Integer.parseInt(shopListingData[3])
                ));
            }
            shops.put(id, new Shop(name, shopListings));
        }
    }

    private void loadNPCTypes(BufferedReader buffer) throws IOException {
        int npcTypeCount = Integer.parseInt(buffer.readLine());
        for (int n = 0; n < npcTypeCount; n++) {
            String[] npcTypeData = buffer.readLine().split(",");
            String id = npcTypeData[0];
            String name = npcTypeData[1];
            Color color = new Color(Integer.parseInt(npcTypeData[2]),Integer.parseInt(npcTypeData[3]),Integer.parseInt(npcTypeData[4]));
            boolean canMove = Boolean.parseBoolean(npcTypeData[5]);
            boolean canFight = Boolean.parseBoolean(npcTypeData[6]);
            boolean isAggressive = Boolean.parseBoolean(npcTypeData[7]);
            int health = Integer.parseInt(npcTypeData[8]);
            int damage = Integer.parseInt(npcTypeData[9]);
            int itemDropCount = Integer.parseInt(npcTypeData[10]);
            GObjects.ItemDrop[] dropTable = new GObjects.ItemDrop[itemDropCount];
            for (int i = 0; i < itemDropCount ; i ++ ) {
                String itemDropData[] = buffer.readLine().split(",");
                switch (itemDropData[0]) {
                    case "Item":
                        dropTable[i] = new GObjects.ItemDrop(items.get(itemDropData[1]).copy());
                        break;
                    case "Money":
                        dropTable[i] = new GObjects.ItemDrop(Integer.parseInt(itemDropData[1]));
                        break;
                }
            }
            boolean canTalk = Boolean.parseBoolean(npcTypeData[11]);
            Conversation conversation = null;
            if (canTalk) {
                String[] conversationData = buffer.readLine().split(",");
                switch (conversationData[0]) {
                    case "QuestTreeSwitchConversation":
                        String questName = conversationData[1];
                        int statusCount = Integer.parseInt(conversationData[2]);
                        Map<QuestStatus, String> statusMap = new HashMap<>();
                        for(int s = 0 ; s < statusCount; s++) {
                            String[] questStatusData = buffer.readLine().split(",");
                            statusMap.put(QuestStatus.valueOf(questStatusData[0]), questStatusData[1]);
                        }
                        conversation = new Conversation(new QuestTreeSwitchSelector(questName, statusMap));
                        break;
                    case "ConversationChoice":
                        conversation = new Conversation(state -> conversationChoices.get(conversationData[1]));
                        break;
                }
            }
            String shop = npcTypeData[12];
            NPCType npcType = new NPCType(
                    id, name, color, canMove, canFight, isAggressive, health, damage, dropTable, canTalk, conversation, shops.get(shop));
            npcTypes.put(id, npcType);
        }
    }

    public Player newPlayer() {
        List playerItems = Arrays.asList(
                items.get("BronzeDagger").copy(),
                items.get("BronzeHatchet").copy(),
                items.get("SteelFlint").copy(),
                items.get("Shovel").copy(),
                items.get("Pickaxe").copy(),
                items.get("Hammer").copy());
        return new Player(
                10, 10,
                0, Collections.emptyMap(), Collections.emptyMap(), null, Collections.emptyMap(), playerItems, Collections.emptyMap(),
                playerStartX, playerStartY);
    }

    private class QuestTreeSwitchSelector implements Function<QuestState, ConversationChoice> {
        private final Map<QuestStatus, String> statusMap;
        private final String questName;

        QuestTreeSwitchSelector(String questName, Map<QuestStatus, String> statusMap) {
            this.questName = questName;
            this.statusMap = statusMap;
        }

        @Override
        public ConversationChoice apply(QuestState questState) {
            return conversationChoices.get(statusMap.get(questState.player.getQuestState(questName)));
        }
    }

    private void loadItemActions(BufferedReader buffer) {
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
                game.attemptResourceGathering(resourceGathering.get("DigGrass"), tile);
            }
        });
        itemActions.put("DigDirt", new ItemAction() {
            @Override
            void perform(WorldQuest game, Tile tile, Player player, int shovel, int na) {
                game.attemptResourceGathering(resourceGathering.get("DigDirt"), tile);
            }
        });
        itemActions.put("Mine", new ItemAction() {
            @Override
            void perform(WorldQuest game, Tile tile, Player player, int shovel, int na) {
                game.attemptResourceGathering(resourceGathering.get("Mine"), tile);
            }
        });
        itemActions.put("Smelt", new ItemAction() {
            @Override
            void perform(WorldQuest game, Tile tile, Player player, int ore1, int na) {
                game.showCrafting(new CraftingOptions("Smelting", recipeList.get("Smelt")));
            }
        });
        itemActions.put("Smith", new ItemAction() {
            @Override
            void perform(WorldQuest game, Tile tile, Player player, int bar, int na) {
                game.showCrafting(new CraftingOptions("Smithing", recipeList.get("Smith")));
            }
        });
    }

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
            if (tile.type == tileTypesByName.get(tileType)) {
                if (player.inventory.hasSpaceForItem(items.get(baseProduct))) {
                    player.gainExperience(skill, experience);
                    player.inventory.add(items.get(baseProduct).copy());
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
                        game.changeTileType(tile, tileTypesByName.get(newTileType));
                    }
                } else {
                    game.eventMessage("No space in your inventory");
                }
            }
        }
    }

    private void loadRecipes(BufferedReader buffer) {
        Recipe Bronze = new Recipe(
                Arrays.asList(
                        Recipe.RecipeItem.one(items.get("CopperOre")),
                        Recipe.RecipeItem.one(items.get("TinOre"))),
                Arrays.asList(Recipe.RecipeItem.one(items.get("BronzeBar"))),
                "Bronze Bar",
                100,
                Collections.emptyMap(),
                Collections.singletonMap("Smelting", 10));
        recipeList.put("Smelt", Arrays.asList(Bronze));
        Recipe BronzeSword = new Recipe(
                Arrays.asList(new Recipe.RecipeItem(items.get("BronzeBar"), 2)),
                Arrays.asList(Recipe.RecipeItem.one(items.get("BronzeSword")),
                "Bronze Sword",
                100,
                Collections.emptyMap(),
                Collections.singletonMap("Smithing", 10));
        recipeList.put("Smith", Arrays.asList(BronzeSword));
    }

    private void loadUses(BufferedReader buffer) {
        itemUses = new HashMap<>();
        {
            itemUses.put("Steel & flint,Oak logs", itemActions.get("Firemaking"));
        }
        tileItemUses = new HashMap<>();
        {
            tileItemUses.put("Grass,Shovel", itemActions.get("DigGrass"));
            tileItemUses.put("Dirt,Shovel", itemActions.get("DigDirt"));
            tileItemUses.put("Rock,Pickaxe", itemActions.get("Mine"));
        }
        objectItemUses = new HashMap<>();
        {
            objectItemUses.put("Furnace,Copper ore", itemActions.get("Smelt"));
            objectItemUses.put("Furnace,Tin ore", itemActions.get("Smelt"));
            objectItemUses.put("Anvil,Hammer", itemActions.get("Smith"));
        }
    }
}
