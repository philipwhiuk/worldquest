package com.whiuk.philip.worldquest;

import java.util.List;
import java.util.Map;

class Recipe {
    final List<Item> input;
    final List<Item> output;
    final int percentageSuccessChance;
    final Map<String, Integer> skillRequirements;
    final Map<String, Integer> experienceGained;
    public String outputName;

    Recipe(List<Item> input, List<Item> output, String outputName,
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

    public void perform(Player player) {
        if (canBeDone(player)) {
            for (Item item : input) {
                player.inventory.remove(item);
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
                for (Item item: output) {
                    player.inventory.add(item.copy());
                }
            }
        }
    }
}
