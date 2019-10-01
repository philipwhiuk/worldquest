package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ScenarioData {
    public TileType tileTypeByName(String tileType) {
        return tileTypesByName.get(tileType);
    }

    ItemType itemType(String item) {
        return itemTypes.get(item);
    }

    static class Provider {

        public static ScenarioData loadScenarioFromBase() {
            JSONParser jsonParser = new JSONParser();
            try(FileReader reader = new FileReader(GameFileUtils.resourceInBaseFolder("scenario"))) {
                return loadScenarioFromJson((JSONObject) jsonParser.parse(reader));
            } catch (Exception e) {
                throw new RuntimeException("Unable to load scenario: " + e.getMessage(), e);
            }
        }

        static ScenarioData loadScenarioByName(String scenario) {
            JSONParser jsonParser = new JSONParser();
            try(FileReader reader = new FileReader(GameFileUtils.resourceInScenarioFolder(scenario, "scenario"))) {
                return loadScenarioFromJson((JSONObject) jsonParser.parse(reader));
            } catch (Exception e) {
                throw new RuntimeException("Unable to load scenario: " + e.getMessage(), e);
            }
        }

        static ScenarioData loadScenarioFromJson(JSONObject scenarioData) throws IOException {
            ScenarioData scenario = new ScenarioData();
            scenario.itemTypes = ItemTypeDao.Provider.loadItemTypesFromJson((JSONArray) scenarioData.get("itemTypes"));
            scenario.questSteps = QuestStep.Provider.loadQuestStepsFromJson((JSONArray) scenarioData.get("questSteps"));
            scenario.quests = Quest.Provider.loadQuestsFromJson(scenario, (JSONArray) scenarioData.get("quests"));
            scenario.tileTypes = TileType.Provider.loadTileTypesFromJson((JSONArray) scenarioData.get("tileTypes"));
            scenario.tileTypesByName = scenario.tileTypes.values().stream().collect(Collectors.toMap(
                    t -> t.name, t -> t));
            scenario.conversationChoices = ConversationChoice.Provider.loadConversationChoicesFromJson(
                    (JSONArray) scenarioData.get("conversationChoices"));
            scenario.shops = Shop.Provider.loadShopsFromJson(scenario, (JSONArray) scenarioData.get("shops"));
            scenario.npcTypes = NPCType.Provider.loadNPCTypesFromJson(scenario,
                    (JSONArray) scenarioData.get("npcTypes"));
            scenario.itemActivities = ItemActivity.Provider.loadItemActivitiesFromJson(scenario,
                    (JSONArray) scenarioData.get("itemActivities"));
            scenario.recipeList = Recipe.Provider.loadRecipesFromJson(scenario,
                    (JSONArray) scenarioData.get("recipes"),
                    (JSONArray) scenarioData.get("recipeCollections"));
            scenario.resourceGathering = ResourceGathering.Provider.loadResourceGatheringFromJson(
                    (JSONArray) scenarioData.get("resourceGathering"));
            scenario.structureCreation = StructureCreation.Provider.loadStructureCreationFromJson(
                    (JSONArray) scenarioData.get("structureCreation"));
            scenario.itemUses = Uses.Provider.loadItemUsesFromJson(scenario,
                    (JSONArray) scenarioData.get("itemUses"));
            scenario.objectItemUses = Uses.Provider.loadObjectItemUsesFromJson(scenario,
                    (JSONArray) scenarioData.get("objectItemUses"));
            scenario.tileItemUses = Uses.Provider.loadTileItemUsesFromJson(scenario, (JSONArray) scenarioData.get("tileItemUses"));
            scenario.initialItems = loadInitialItemsFromJson((JSONArray) scenarioData.get("initialItems"));
            return scenario;
        }

        private static List<String> loadInitialItemsFromJson(JSONArray initialItems) throws IOException {
            ArrayList<String> items = new ArrayList<>();
            for (Object initialItem : initialItems) {
                items.add((String) initialItem);
            }
            return items;
        }
    }
    static class Persistor {
        static void saveScenario(ScenarioData data, String scenario) {
            try(FileWriter writer = new FileWriter(GameFileUtils.resourceInScenarioFolder(scenario, "scenario"))) {
                writer.write(saveScenarioToJson(data).toJSONString());
            } catch (Exception e) {
                throw new RuntimeException("Unable to save scenario: " + e.getMessage(), e);
            }
        }

        private static JSONObject saveScenarioToJson(ScenarioData scenario) throws IOException {
            JSONObject scenarioData = new JSONObject();
            scenarioData.put("itemTypes", ItemTypeDao.Persistor.saveItemTypesToJson(scenario.itemTypes));
            scenarioData.put("questSteps", QuestStep.Persistor.saveQuestStepsToJson(scenario.questSteps));
            scenarioData.put("quests", Quest.Persistor.saveQuestsToJson(scenario.quests));
            scenarioData.put("tileTypes", TileType.Persistor.saveTileTypesToJson(scenario.tileTypes));
            scenarioData.put("conversationChoices", ConversationChoice.Persistor.saveConversationChoicesToJson(scenario.conversationChoices));
            scenarioData.put("shops", Shop.Persistor.saveShopsToJson(scenario.shops));
            scenarioData.put("npcTypes", NPCType.Persistor.saveNPCTypesToJson(scenario.npcTypes));
            scenarioData.put("itemActivities", ItemActivity.Persistor.saveItemActivitiesToJson(scenario.itemActivities));
            scenarioData.put("recipes", Recipe.Persistor.saveRecipesToJson(scenario.recipeList));
            scenarioData.put("resourceGathering", ResourceGathering.Persistor.saveResourceGatheringToJson(scenario.resourceGathering));
            scenarioData.put("itemUses", Uses.Persistor.saveItemUsesToJson(scenario.itemUses));
            scenarioData.put("objectItemUses", Uses.Persistor.saveObjectItemUsesToJson(scenario.objectItemUses));
            scenarioData.put("tileItemUses", Uses.Persistor.saveTileItemUsesToJson(scenario.tileItemUses));
            return scenarioData;
        }
    }

    Map<String, ItemType> itemTypes = new HashMap<>();
    Map<String, QuestStep> questSteps = new HashMap<>();
    Map<String, Quest> quests = new HashMap<>();
    Map<Integer, TileType> tileTypes = new HashMap<>();
    private Map<String, TileType> tileTypesByName = new HashMap<>();
    Map<String, Shop> shops = new HashMap<>();
    Map<String, NPCType> npcTypes = new HashMap<>();
    Map<String, ConversationChoice> conversationChoices = new HashMap<>();
    Map<String, ItemActivity> itemActivities = new HashMap<>();
    Map<String, List<Recipe>> recipeList = new HashMap<>();
    Map<String, ResourceGathering> resourceGathering = new HashMap<>();
    Map<String, StructureCreation> structureCreation = new HashMap<>();
    private int playerStartX;
    private int playerStartY;
    Map<String, Map<String, ItemActivity>> itemUses = new HashMap<>();
    Map<String, Map<String, ItemActivity>> tileItemUses = new HashMap<>();
    Map<String, Map<String, ItemActivity>> objectItemUses = new HashMap<>();
    List<String> initialItems = new ArrayList<>();

    ScenarioData() {}

    public Player newPlayer() {
        //TODO: Initial items should be actual items not type list have quality etc.
        List<Item> playerItems = new ArrayList<>();
        for (String initialItem : initialItems) {
            playerItems.add(itemType(initialItem).create());
        }
        Arrays.asList(
                itemType("BronzeDagger").create(),
                itemType("BronzeHatchet").create(),
                itemType("SteelFlint").create(),
                itemType("Shovel").create(),
                itemType("Pickaxe").create(),
                itemType("Hammer").create());
        return new Player(
                10, 10,
                100, 100,
                0, Collections.emptyMap(), Collections.emptyMap(), null, Collections.emptyMap(),
                playerItems, Collections.emptyMap(),
                playerStartX, playerStartY);
    }

}
