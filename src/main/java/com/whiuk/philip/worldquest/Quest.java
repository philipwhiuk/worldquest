package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class QuestStep {
    static class Provider {
        static Map<String, QuestStep> loadQuestStepsFromJson(JSONArray questStepsData) {
            Map<String, QuestStep> questSteps = new HashMap<>();
            for (Object qSO : questStepsData) {
                JSONObject questStepData = (JSONObject) qSO;
                String questStepID = (String) questStepData.get("id");
                JSONArray killsRequired = (JSONArray) questStepData.get("killsRequired");
                Map<String, Integer> killCounts = new HashMap<>();
                for (Object kRO : killsRequired) {
                    JSONObject killRequiredData = (JSONObject) kRO;
                    String killType = (String) killRequiredData.get("npc");
                    int count = ((Long) killRequiredData.get("quantity")).intValue();
                    killCounts.put(killType, count);
                }
                questSteps.put(questStepID, new QuestStep(killCounts));
            }
            return questSteps;
        }
    }

    static class Persistor {
        public static JSONArray saveQuestStepsToJson(Map<String, QuestStep> questSteps) {
            //TODO:
            /**
            buffer.write(Integer.toString(questSteps.size()));
            buffer.newLine();
            for (Map.Entry<String, QuestStep> questStep : questSteps.entrySet()) {
                buffer.write(questStep.getKey());
                buffer.write(",");
                buffer.write(",");
                buffer.write(questStep.getValue().killsRequired.size());
                buffer.newLine();
                for (Map.Entry<String, Integer> requiredKill: questStep.getValue().killsRequired.entrySet()) {
                    buffer.write(requiredKill.getKey()+","+requiredKill.getValue());
                    buffer.newLine();
                }
            }
             **/
            return new JSONArray();
        }
    }
    Map<String, Integer> killsRequired;

    QuestStep(Map<String, Integer> killsRequired) {
        this.killsRequired = killsRequired;
    }
}

public class Quest {
    static class Provider {
        static Map<String, Quest> loadQuestsFromJson(ScenarioData data, JSONArray questsData) {
            Map<String, Quest> quests = new HashMap<>();
            for (Object qO: questsData) {
                JSONObject questData = (JSONObject) qO;
                String questID = (String) questData.get("id");
                String questName = (String) questData.get("name");
                List<QuestStep> steps = new ArrayList<>();
                JSONArray stepData = (JSONArray) questData.get("steps");
                for (Object sO : stepData) {
                    String stepName = (String) sO;
                    steps.add(data.questSteps.get(stepName));
                }
                quests.put(questID, new Quest(questName,steps));
            }
            return quests;
        }
    }

    public static class Persistor {
        public static JSONArray saveQuestsToJson(Map<String, Quest> quests) throws IOException {
            //TODO:
            return new JSONArray();
        }
    }

    private QuestStatus status;
    String name;
    int stepIndex;
    List<QuestStep> steps;

    Quest(String name, List<QuestStep> steps) {
        this.name = name;
        this.steps = steps;
    }

    public QuestProgress start() {
        return QuestProgress.start(this);
    }

    public boolean hasStatus(QuestStatus value) {
        return status == value;
    }

}
