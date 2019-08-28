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

        public static ScenarioData loadScenarioFromBase() {
            try(
                    InputStream mapDataStream = new FileInputStream(
                            GameFileUtils.resourceInBaseFolder("scenario"));
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(mapDataStream))){
                return loadScenarioFromBuffer(buffer);
            } catch (Exception e) {
                throw new RuntimeException("Unable to load scenario: " + e.getMessage(), e);
            }
        }

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
            scenario.questSteps = QuestStep.Provider.loadQuestStepsFromBuffer(buffer);
            scenario.quests = Quest.Provider.loadQuestsFromBuffer(scenario, buffer);
            scenario.tileTypes = TileType.Provider.loadTileTypesFromBuffer(buffer);
            scenario.tileTypesByName = scenario.tileTypes.values().stream().collect(Collectors.toMap(
                    t -> t.name, t -> t));
            scenario.conversationChoices = ConversationChoice.Provider.loadConversationChoicesFromBuffer(buffer);
            scenario.shops = Shop.Provider.loadShopsFromBuffer(scenario, buffer);
            scenario.npcTypes = NPCType.Provider.loadNPCTypesFromBuffer(scenario, buffer);
            scenario.itemActions = ItemAction.Provider.loadItemActionsFromBuffer(scenario, buffer);
            scenario.recipeList = Recipe.Provider.loadRecipesFromBuffer(scenario, buffer);
            scenario.resourceGathering = ResourceGathering.Provider.loadResourceGatheringFromBuffer(buffer);
            scenario.itemUses = Uses.Provider.loadItemUsesFromBuffer(scenario, buffer);
            scenario.objectItemUses = Uses.Provider.loadObjectItemUsesFromBuffer(scenario, buffer);
            scenario.tileItemUses = Uses.Provider.loadTileItemUsesFromBuffer(scenario, buffer);
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

        private static void saveScenarioToBuffer(ScenarioData scenario, BufferedWriter buffer) throws IOException {
            Item.Persistor.saveItemsToBuffer(scenario.items, buffer);
            QuestStep.Persistor.saveQuestStepsToBuffer(scenario.questSteps, buffer);
            Quest.Persistor.saveQuestsToBuffer(scenario.quests, buffer);
            TileType.Persistor.saveTileTypesToBuffer(scenario.tileTypes, buffer);
            ConversationChoice.Persistor.saveConversationChoicesToBuffer(scenario.conversationChoices, buffer);
            Shop.Persistor.saveShopsToBuffer(scenario.shops, buffer);
            NPCType.Persistor.saveNPCTypesToBuffer(scenario.npcTypes, buffer);
            ItemAction.Persistor.saveItemActionsToBuffer(scenario.itemActions, buffer);
            Recipe.Persistor.saveRecipesToBuffer(scenario.recipeList, buffer);
            ResourceGathering.Persistor.saveResourceGatheringToBuffer(scenario.resourceGathering, buffer);
            Uses.Persistor.saveItemUsesToBuffer(scenario.itemUses, buffer);
            Uses.Persistor.saveObjectItemUsesToBuffer(scenario.objectItemUses, buffer);
            Uses.Persistor.saveTileItemUsesToBuffer(scenario.tileItemUses, buffer);
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
