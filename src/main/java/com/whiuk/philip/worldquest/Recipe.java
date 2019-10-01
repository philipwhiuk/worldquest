package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.whiuk.philip.worldquest.JsonUtils.intFromObj;

class Recipe {
    final String id;
    final List<RecipeItem> input;
    final List<RecipeItem> output;
    final int percentageSuccessChance;
    final Map<String, Integer> skillRequirements;
    final Map<String, Integer> experienceGained;
    public String outputName;

    Recipe(String id, List<RecipeItem> input, List<RecipeItem> output, String outputName,
           int percentageSuccessChance,
           Map<String, Integer> skillRequirements, Map<String, Integer> experienceGained) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.outputName = outputName;
        this.percentageSuccessChance = percentageSuccessChance;
        this.skillRequirements = skillRequirements;
        this.experienceGained = experienceGained;
    }

    public boolean isSuccess() {
        return RandomSource.getRandom().nextInt(100) < percentageSuccessChance;
    }

    public boolean canBeDone(Player player) {
        boolean canBeDone = true;
        for (RecipeItem i : input) {
            canBeDone &= player.inventory.contains(i.item, i.quantity);
        }
        if (!canBeDone) {
            return false;
        }
        for (Map.Entry<String, Integer> skillReq : skillRequirements.entrySet()) {
            canBeDone &= player.skills.getOrDefault(skillReq.getKey(), Experience.NoExperience()).level >= skillReq.getValue();
        }
        int totalCount = output.stream().map(r -> r.quantity).mapToInt(x -> x).sum();
        if (!player.inventory.hasSpaceForItems(totalCount)) {
            return false;
        }
        return canBeDone;
    }

    public void perform(Player player) {
        if (canBeDone(player)) {
            for (RecipeItem recipeItem : input) {
                for (int i = 0; i < recipeItem.quantity; i++) {
                    //TODO: Use item to determine output quality
                    player.inventory.remove(recipeItem.item);
                }
            }
            boolean success = isSuccess();
            if (success) {
                for (Map.Entry<String, Integer> experience : experienceGained.entrySet()) {
                    String skill = experience.getKey();
                    int expGain = experience.getValue();
                    Experience skillXp = player.skills.getOrDefault(skill, Experience.NoExperience());
                    skillXp.experience += expGain;
                    player.skills.put(skill, skillXp);
                }
                for (RecipeItem recipeItem: output) {
                    for (int i = 0; i < recipeItem.quantity; i++) {
                        //TODO: Provide quality
                        player.inventory.add(recipeItem.item.create());
                    }
                }
            }
        }
    }

    static class RecipeItem {
        final ItemType item;
        final int quantity;

        public RecipeItem(ItemType item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }
    }

    public static class Persistor {
        public static JSONArray saveRecipesToJson(Map<String, List<Recipe>> recipeCollections) throws IOException {
            //TODO:
            return new JSONArray();
        }
    }

    static class Provider {
        static Map<String, List<Recipe>> loadRecipesFromJson(
                ScenarioData data,
                JSONArray recipesData,
                JSONArray recipeCollectionsData) throws IOException {
            Map<String,Recipe> allRecipes = new HashMap<>();
            for (Object rO : recipesData) {
                JSONObject recipeData = (JSONObject) rO;
                String recipeId = (String) recipeData.get("id");
                String outputName = (String) recipeData.get("outputName");
                int successChance = intFromObj(recipeData.get("successChance"));
                ArrayList<RecipeItem> inputItems = new ArrayList<>();

                JSONArray inputItemsData = (JSONArray) recipeData.get("inputItems");
                for (Object iIO : inputItemsData) {
                    JSONObject inputItemData = (JSONObject) iIO;
                    Recipe.RecipeItem recipeItem = new Recipe.RecipeItem(
                            data.itemType((String) inputItemData.get("item")),
                            intFromObj(inputItemData.get("quantity")));
                    inputItems.add(recipeItem);
                }
                JSONArray outputItemsData = (JSONArray) recipeData.get("outputItems");
                ArrayList<Recipe.RecipeItem> outputItems = new ArrayList<>();
                for (Object oIO : outputItemsData) {
                    JSONObject outputItemData = (JSONObject) oIO;
                    Recipe.RecipeItem recipeItem = new Recipe.RecipeItem(
                            data.itemType((String) outputItemData.get("item")),
                            intFromObj(outputItemData.get("quantity")));
                    outputItems.add(recipeItem);
                }
                JSONArray skillRequirementsData = (JSONArray) recipeData.getOrDefault("skillRequirements", new JSONArray());
                Map<String, Integer> skillRequirements = new HashMap<>();
                for (Object sRO : skillRequirementsData) {
                    JSONObject skillRequirementData = (JSONObject) sRO;
                    skillRequirements.put((String) skillRequirementData.get("skill"),
                            intFromObj(skillRequirementData.get("level")));
                }

                JSONArray experienceGainedData = (JSONArray) recipeData.get("experienceGained");
                Map<String, Integer> experienceGained = new HashMap<>();
                for (Object eGO : experienceGainedData) {
                    JSONObject experienceGainData = (JSONObject) eGO;
                    skillRequirements.put((String) experienceGainData.get("skill"),
                            intFromObj(experienceGainData.get("exp")));
                }
                Recipe recipe = new Recipe(recipeId, inputItems, outputItems, outputName, successChance, skillRequirements, experienceGained);
                allRecipes.put(recipeId, recipe);
            }

            Map<String, List<Recipe>> recipeList = new HashMap<>();
            for (Object rcO : recipeCollectionsData) {
                JSONObject recipeCollectionData = (JSONObject) rcO;

                JSONArray colRecipesData = (JSONArray) recipeCollectionData.get("recipes");
                List<Recipe> recipes = new ArrayList<>();
                for (Object rO : colRecipesData) {
                    String recipe = (String) rO;
                    recipes.add(allRecipes.get(recipe));
                }
                recipeList.put((String) recipeCollectionData.get("name"), recipes);
            }
            return recipeList;
        }
    }

}
