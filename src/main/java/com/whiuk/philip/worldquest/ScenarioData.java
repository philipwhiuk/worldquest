package com.whiuk.philip.worldquest;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ScenarioData {
    public TileType tileTypeByName(String tileType) {
        return tileTypesByName.get(tileType);
    }

    public Item item(String item) {
        return items.get(item);
    }

    static class Provider {
        static ScenarioData loadScenarioByName(String scenario) {
            try(
                    InputStream mapDataStream = new FileInputStream(
                            GameFileUtils.resourceInScenarioFolder(scenario, "scenario"));
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(mapDataStream))){
                return loadScenarioFromBuffer(buffer);
            } catch (Exception e) {
                throw new RuntimeException("Unable to load scenario: " + e.getMessage(), e);
            }
        }

        static ScenarioData loadScenarioFromBuffer(BufferedReader buffer) throws IOException {
            ScenarioData scenario = new ScenarioData();
            scenario.items = Item.Provider.loadItemsFromBuffer(buffer);
            scenario.questSteps = QuestStepsProvider.loadQuestStepsFromBuffer(buffer);
            scenario.quests = QuestsProvider.loadQuestsFromBuffer(scenario, buffer);
            scenario.tileTypes = TileTypesProvider.loadTileTypesFromBuffer(buffer);
            scenario.tileTypesByName = scenario.tileTypes.values().stream().collect(Collectors.toMap(
                    t -> t.name, t -> t));
            scenario.conversationChoices = ConversationChoicesProvider.loadConversationChoicesFromBuffer(buffer);
            scenario.shops = ShopsProvider.loadShopsFromBuffer(scenario, buffer);
            scenario.npcTypes = NPCTypesProvider.loadNPCTypesFromBuffer(scenario, buffer);
            scenario.itemActions = ItemActionsProvider.loadItemActionsFromBuffer(scenario, buffer);
            scenario.recipeList = RecipesProvider.loadRecipesFromBuffer(scenario, buffer);
            scenario.resourceGathering = ResourceGatheringProvider.loadResourceGatheringFromBuffer(buffer);
            scenario.itemUses = UsesProvider.loadItemUsesFromBuffer(scenario, buffer);
            scenario.objectItemUses = UsesProvider.loadObjectItemUsesFromBuffer(scenario, buffer);
            scenario.tileItemUses = UsesProvider.loadTileItemUsesFromBuffer(scenario, buffer);
            return scenario;
        }
    }
    static class Persistor {
        static void saveScenario(ScenarioData data, String scenario) {
            try(
                    OutputStream saveFileDataStream = new FileOutputStream(
                            GameFileUtils.resourceInScenarioFolder(scenario, "scenario"));
                    BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(saveFileDataStream))) {
                saveScenarioToBuffer(data, buffer);
            } catch (Exception e) {
                throw new RuntimeException("Unable to save scenario: " + e.getMessage(), e);
            }
        }

        private static void saveScenarioToBuffer(ScenarioData scenario, BufferedWriter buffer) {
            Item.Persistor.saveItemsToBuffer(scenario.items, buffer);
        }
    }

    private Map<String, Item> items = new HashMap<>();
    Map<String, QuestStep> questSteps = new HashMap<>();
    Map<String, Quest> quests = new HashMap<>();
    Map<Integer, TileType> tileTypes = new HashMap<>();
    private Map<String, TileType> tileTypesByName = new HashMap<>();
    Map<String, Shop> shops = new HashMap<>();
    Map<String, NPCType> npcTypes = new HashMap<>();
    Map<String, ConversationChoice> conversationChoices = new HashMap<>();
    Map<String, ItemAction> itemActions = new HashMap<>();
    Map<String, List<Recipe>> recipeList = new HashMap<>();
    Map<String, ResourceGathering> resourceGathering = new HashMap<>();
    private int playerStartX;
    private int playerStartY;
    HashMap<String, ItemAction> itemUses = new HashMap<>();
    Map<String, ItemAction> tileItemUses = new HashMap<>();
    Map<String, ItemAction> objectItemUses = new HashMap<>();

    ScenarioData() {}

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

}
