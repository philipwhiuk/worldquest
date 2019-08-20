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
    private Map<String, TileType> tileTypesByName = new HashMap<>();
    private Map<String, Shop> shops = new HashMap<>();
    Map<String, NPCType> npcTypes = new HashMap<>();
    Map<String, ConversationChoice> conversationChoices = new HashMap<>();
    private Map<String, ItemAction> itemActions = new HashMap<>();
    private Map<String, List<Recipe>> recipeList = new HashMap<>();
    private Map<String, ResourceGathering> resourceGathering = new HashMap<>();
    private int playerStartX;
    private int playerStartY;
    HashMap<String, ItemAction> itemUses = new HashMap<>();
    Map<String, ItemAction> tileItemUses = new HashMap<>();
    Map<String, ItemAction> objectItemUses = new HashMap<>();

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

    @SuppressWarnings("unused") //TODO: Load Resource Gathering Methods from buffer
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
        resourceGathering.put("Fish", new ResourceGathering(
                "Water",
                "Water",
                "RawCatfish",
                Collections.emptyList(),
                "Fishing",
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
                    case "ShopDisplay":
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
                        items.put(itemID, new Item(itemData[2], Item.parseActions(itemData[3])));
                        break;
                    case "Weapon":
                        items.put(itemID, new Weapon(itemData[2],
                                Item.parseActions(itemData[3]), itemData[4], Integer.parseInt(itemData[5])));
                        break;
                    case "Hatchet":
                        items.put(itemID, new Hatchet(itemData[2],
                                Item.parseActions(itemData[3]), Integer.parseInt(itemData[4])));
                        break;
                    case "Armour":
                        items.put(itemID, new Armour(itemData[2], Item.parseActions(itemData[3]),
                                Slot.valueOf(itemData[4]), Integer.parseInt(itemData[5])));
                        break;
                    case "Consumable":
                        items.put(itemID, new Consumable(itemData[2], Item.parseActions(itemData[3]),
                                Consumable.parseStatChanges(itemData[4])));
                        break;
                    default:
                        throw new IllegalArgumentException(itemClass);
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
        List<Item> playerItems = Arrays.asList(
                items.get("BronzeDagger").copy(),
                items.get("BronzeHatchet").copy(),
                items.get("SteelFlint").copy(),
                items.get("Shovel").copy(),
                items.get("Pickaxe").copy(),
                items.get("Hammer").copy());
        return new Player(
                10, 10,
                100, 100,
                0, Collections.emptyMap(), Collections.emptyMap(), null, Collections.emptyMap(),
                playerItems, Collections.emptyMap(),
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

    @SuppressWarnings("unused") //TODO: Load ItemActions from buffer
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
            void perform(WorldQuest game, Tile tile, Player player, int pickaxe, int na) {
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
            void perform(WorldQuest game, Tile tile, Player player, int hammer, int na) {
                game.showCrafting(new CraftingOptions("Smithing", recipeList.get("Smith")));
            }
        });
        itemActions.put("Fish", new ItemAction() {
            @Override
            void perform(WorldQuest game, Tile tile, Player player, int rod, int na) {
                game.attemptResourceGathering(resourceGathering.get("Fish"), tile);
            }
        });
        itemActions.put("Cook", new ItemAction() {
            @Override
            void perform(WorldQuest game, Tile tile, Player player, int food, int na) {
                game.showCrafting(new CraftingOptions("Cooking", recipeList.get("Cook")));
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

    private void loadRecipes(BufferedReader buffer) throws IOException {
        int recipesCount = Integer.parseInt(buffer.readLine());
        Map<String,Recipe> allRecipes = new HashMap<>();
        for (int r = 0; r < recipesCount; r++) {
            String[] recipeData = buffer.readLine().split(",");
            String recipeName = recipeData[0];
            int inputItemCount = Integer.parseInt(recipeData[1]);
            int outputItemCount = Integer.parseInt(recipeData[2]);
            String outputName = recipeData[3];
            int successChance = Integer.parseInt(recipeData[4]);
            int skillRequirementCount = Integer.parseInt(recipeData[5]);
            int experienceGainedCount = Integer.parseInt(recipeData[6]);
            ArrayList<Recipe.RecipeItem> inputItems = new ArrayList<>(inputItemCount);
            for (int i = 0; i < inputItemCount; i++) {
                String[] inputItemData = buffer.readLine().split(",");
                Recipe.RecipeItem recipeItem = new Recipe.RecipeItem(items.get(inputItemData[0]), Integer.parseInt(inputItemData[1]));
                inputItems.add(recipeItem);
            }
            ArrayList<Recipe.RecipeItem> outputItems = new ArrayList<>(outputItemCount);
            for (int i = 0; i < outputItemCount; i++) {
                String[] outputItemData = buffer.readLine().split(",");
                Recipe.RecipeItem recipeItem = new Recipe.RecipeItem(items.get(outputItemData[0]), Integer.parseInt(outputItemData[1]));
                outputItems.add(recipeItem);
            }
            Map<String, Integer> skillRequirements = new HashMap<>(skillRequirementCount);
            for (int i = 0; i < skillRequirementCount; i++) {
                String[] skillRequirementData = buffer.readLine().split(",");
                skillRequirements.put(skillRequirementData[0], Integer.parseInt(skillRequirementData[1]));
            }
            Map<String, Integer> experienceGained = new HashMap<>(experienceGainedCount);
            for (int i = 0; i < experienceGainedCount; i++) {
                String[] experienceGainData = buffer.readLine().split(",");
                experienceGained.put(experienceGainData[0], Integer.parseInt(experienceGainData[1]));
            }
            Recipe recipe = new Recipe(inputItems, outputItems, outputName, successChance, skillRequirements, experienceGained);
            allRecipes.put(recipeName, recipe);
        }
        int recipeCollectionsCount = Integer.parseInt(buffer.readLine());
        for (int rc = 0; rc < recipeCollectionsCount; rc++) {
            String[] recipeCollectionData = buffer.readLine().split(",");
            int collectionRecipesCount = Integer.parseInt(recipeCollectionData[1]);
            List<Recipe> recipes = new ArrayList<>();
            for (int r = 0; r < collectionRecipesCount; r++) {
                recipes.add(allRecipes.get(buffer.readLine()));
            }
            recipeList.put(recipeCollectionData[0], recipes);
        }
    }

    private void loadUses(BufferedReader buffer) throws IOException {
        int itemUsesCount = Integer.parseInt(buffer.readLine());
        for (int i = 0; i < itemUsesCount; i++) {
            String[] itemUseData = buffer.readLine().split(":");
            itemUses.put(itemUseData[0], itemActions.get(itemUseData[1]));
        }

        int tileItemUsesCount = Integer.parseInt(buffer.readLine());
        for (int t = 0; t < tileItemUsesCount; t++) {
            String[] tileItemUseData = buffer.readLine().split(":");
            tileItemUses.put(tileItemUseData[0], itemActions.get(tileItemUseData[1]));
        }

        int objectItemUsesCount = Integer.parseInt(buffer.readLine());
        for (int t = 0; t < objectItemUsesCount; t++) {
            String[] objectItemUseData = buffer.readLine().split(":");
            objectItemUses.put(objectItemUseData[0], itemActions.get(objectItemUseData[1]));
        }
    }
}
