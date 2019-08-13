package com.whiuk.philip.worldquest;

import java.util.List;
import java.util.Map;

class Recipe {
    final List<RecipeItem> input;
    final List<RecipeItem> output;
    final int percentageSuccessChance;
    final Map<String, Integer> skillRequirements;
    final Map<String, Integer> experienceGained;
    public String outputName;

    Recipe(List<RecipeItem> input, List<RecipeItem> output, String outputName,
           int percentageSuccessChance,
           Map<String, Integer> skillRequirements, Map<String, Integer> experienceGained) {
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
}
