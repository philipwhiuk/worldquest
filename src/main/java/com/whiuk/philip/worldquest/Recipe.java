package com.whiuk.philip.worldquest;

import java.util.List;
import java.util.Map;

class Recipe {
    final List<Item> input;
    final List<Item> output;
    final int percentageSuccessChance;
    final Map<String, Integer> skillRequirements;
    final Map<String, Integer> experienceGained;

    Recipe(List<Item> input, List<Item> output,
           int percentageSuccessChance,
           Map<String, Integer> skillRequirements, Map<String, Integer> experienceGained) {
        this.input = input;
        this.output = output;
        this.percentageSuccessChance = percentageSuccessChance;
        this.skillRequirements = skillRequirements;
        this.experienceGained = experienceGained;
    }

    public boolean isSuccess() {
        return RandomSource.getRandom().nextInt(100) < percentageSuccessChance;
    }

    public boolean canBeDone(Player player) {
        boolean canBeDone = true;
        for (Item i : input) {
            canBeDone &= player.inventory.contains(i);
        }
        if (!canBeDone) {
            return false;
        }
        for (Map.Entry<String, Integer> skillReq : skillRequirements.entrySet()) {
            canBeDone &= player.skills.getOrDefault(skillReq.getKey(), Experience.NoExperience()).level >= skillReq.getValue();
        }
        if (!player.inventory.hasSpaceForItems(output)) {
            return false;
        }
        return canBeDone;
    }
}
