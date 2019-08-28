package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                        player.inventory.add(recipeItem.item.copy());
                    }
                }
            }
        }
    }

    static class RecipeItem {
        final Item item;
        final int quantity;

        public RecipeItem(Item item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }

        public static RecipeItem one(Item item) {
            return new RecipeItem(item, 1);
        }
    }

    public static class Persistor {
        public static void saveRecipesToBuffer(Map<String, List<Recipe>> recipeCollections, BufferedWriter buffer) throws IOException {
            Map<String, Recipe> allRecipes = new HashMap<>();
            recipeCollections.values().forEach(rL -> rL.forEach(r -> allRecipes.put(r.id, r)));

            buffer.write(Integer.toString(allRecipes.size()));
            buffer.newLine();

            buffer.write(Integer.toString(recipeCollections.size()));
            buffer.newLine();
        }
    }

    static class Provider {
        static Map<String, List<Recipe>> loadRecipesFromBuffer(ScenarioData data, BufferedReader buffer) throws IOException {
            int recipesCount = Integer.parseInt(buffer.readLine());
            Map<String,Recipe> allRecipes = new HashMap<>();
            for (int r = 0; r < recipesCount; r++) {
                String[] recipeData = buffer.readLine().split(",");
                String recipeId = recipeData[0];
                int inputItemCount = Integer.parseInt(recipeData[1]);
                int outputItemCount = Integer.parseInt(recipeData[2]);
                String outputName = recipeData[3];
                int successChance = Integer.parseInt(recipeData[4]);
                int skillRequirementCount = Integer.parseInt(recipeData[5]);
                int experienceGainedCount = Integer.parseInt(recipeData[6]);
                ArrayList<RecipeItem> inputItems = new ArrayList<>(inputItemCount);
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
                Recipe recipe = new Recipe(recipeId, inputItems, outputItems, outputName, successChance, skillRequirements, experienceGained);
                allRecipes.put(recipeId, recipe);
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

}
