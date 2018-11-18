package com.whiuk.philip.worldquest;

import java.util.List;
import java.util.Map;

class QuestStep {
    Map<String, Integer> killsRequired;

    QuestStep(Map<String, Integer> killsRequired) {
        this.killsRequired = killsRequired;
    }


    public void npcDeath(String name) {
        if (killsRequired.containsKey(name)) {
            int kills = killsRequired.get(name);
            if (kills == 1) {
                killsRequired.remove(name);
            } else {
                killsRequired.put(name, kills -1);
            }
        }
    }

    public boolean isFinished() {
        return killsRequired.isEmpty();
    }
}

public class Quest {
    private boolean finished;
    String name;
    int stepIndex;
    List<QuestStep> steps;

    Quest(String name, List<QuestStep> steps, int stepIndex, boolean finished) {
        this.name = name;
        this.steps = steps;
        this.stepIndex = stepIndex;
        this.finished = finished;
    }

    void finish(Player player) {
        this.finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public void npcDeath(String name) {
        steps.get(stepIndex).npcDeath(name);
    }

    public boolean isQuestComplete() {
        return stepIndex + 1 == steps.size() && steps.get(stepIndex).isFinished();
    }

    public Quest start() {
        return new Quest(name, steps, stepIndex, false);
    }
}
