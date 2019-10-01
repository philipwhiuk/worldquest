package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.whiuk.philip.worldquest.JsonUtils.intFromObj;

public class QuestProgress {
    Quest quest;
    QuestStatus status;
    QuestStepProgress currentQuestStepProgress;
    int stepIndex;

    public static class Provider {
        public static QuestProgress parseQuestProgress(JSONObject qO, Map<String, Quest> quests) {
            return new QuestProgress(
                    quests.get((String) qO.get("quest")),
                    QuestStatus.valueOf((String) qO.get("status")),
                    intFromObj(qO.get("stepIndex")),
                    parseQuestStepProgress((JSONObject) qO.get("stepProgress")));
        }

        private static QuestStepProgress parseQuestStepProgress(JSONObject stepProgress) {
            JSONArray killsRequired = (JSONArray) stepProgress.get("killsRemaining");
            Map<String, Integer> killCounts = new HashMap<>();
            for (Object kRO : killsRequired) {
                JSONObject killRequiredData = (JSONObject) kRO;
                String killType = (String) killRequiredData.get("npc");
                int count = ((Long) killRequiredData.get("quantity")).intValue();
                killCounts.put(killType, count);
            }
            return new QuestStepProgress(killCounts);
        }
    }

    static QuestProgress start(Quest quest) {
        return new QuestProgress(quest, QuestStatus.STARTED, 0, new QuestStepProgress(quest.steps.get(0)));
    }

    public QuestProgress(Quest quest, QuestStatus status, int stepIndex, QuestStepProgress progress) {
        this.quest = quest;
        this.status = status;
        this.stepIndex = stepIndex;
        this.currentQuestStepProgress = progress;
    }

    void finish(Player player) {
        this.status = QuestStatus.FINISHED;
    }

    public boolean hasStatus(QuestStatus value) {
        return status == value;
    }

    public boolean isFinished() {
        return this.status == QuestStatus.FINISHED;
    }

    public void npcDeath(String name) {
        currentQuestStepProgress.npcDeath(name);
        if (isQuestComplete()) {
            status = QuestStatus.COMPLETE;
        }
    }

    private boolean isQuestComplete() {
        return stepIndex + 1 == quest.steps.size() &&currentQuestStepProgress.isFinished();
    }
}

class QuestStepProgress {
    final HashMap<String, Integer> killsRemaining;

    QuestStepProgress(QuestStep questStep) {
        killsRemaining = new HashMap<>(questStep.killsRequired);
    }

    QuestStepProgress(Map<String, Integer> killsRemaining) {
        this.killsRemaining = new HashMap<>(killsRemaining);
    }

    public void npcDeath(String name) {
        if (killsRemaining.containsKey(name)) {
            int kills = killsRemaining.get(name);
            if (kills == 1) {
                killsRemaining.remove(name);
            } else {
                killsRemaining.put(name, kills -1);
            }
        }
    }

    public boolean isFinished() {
        return killsRemaining.isEmpty();
    }
}