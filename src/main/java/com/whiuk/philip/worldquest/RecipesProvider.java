package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipesProvider {

    static Map<String, List<Recipe>> loadRecipesFromBuffer(ScenarioData data, BufferedReader buffer) throws IOException {
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
                Recipe.RecipeItem recipeItem = new Recipe.RecipeItem(data.item(inputItemData[0]), Integer.parseInt(inputItemData[1]));
                inputItems.add(recipeItem);
            }
            ArrayList<Recipe.RecipeItem> outputItems = new ArrayList<>(outputItemCount);
            for (int i = 0; i < outputItemCount; i++) {
                String[] outputItemData = buffer.readLine().split(",");
                Recipe.RecipeItem recipeItem = new Recipe.RecipeItem(data.item(outputItemData[0]), Integer.parseInt(outputItemData[1]));
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
        Map<String, List<Recipe>> recipeList = new HashMap<>();
        for (int rc = 0; rc < recipeCollectionsCount; rc++) {
            String[] recipeCollectionData = buffer.readLine().split(",");
            int collectionRecipesCount = Integer.parseInt(recipeCollectionData[1]);
            List<Recipe> recipes = new ArrayList<>();
            for (int r = 0; r < collectionRecipesCount; r++) {
                recipes.add(allRecipes.get(buffer.readLine()));
            }
            recipeList.put(recipeCollectionData[0], recipes);
        }
        return recipeList;
    }
}
