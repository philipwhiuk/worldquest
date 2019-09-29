package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class QuestStep {
    static class Provider {
        static Map<String, QuestStep> loadQuestStepsFromBuffer(BufferedReader buffer) throws IOException {
            Map<String, QuestStep> questSteps = new HashMap<>();
            int questStepsCount = Integer.parseInt(buffer.readLine());
            for (int i = 0; i < questStepsCount; i++) {
                String[] questStepData = buffer.readLine().split(",");
                String questStepID = questStepData[0];
                int killTypesCount = Integer.parseInt(questStepData[1]);
                Map<String, Integer> killCounts = new HashMap<>();
                for (int j = 0; j < killTypesCount; j++) {
                    String[] killCountData = buffer.readLine().split(",");
                    String killType = killCountData[0];
                    int count = Integer.parseInt(killCountData[1]);
                    killCounts.put(killType, count);
                }
                questSteps.put(questStepID, new QuestStep(killCounts));
            }
            return questSteps;
        }
    }

    static class Persistor {
        public static void saveQuestStepsToBuffer(Map<String, QuestStep> questSteps, BufferedWriter buffer) throws IOException {
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
        }
    }
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
    static class Provider {
        static Map<String, Quest> loadQuestsFromBuffer(ScenarioData data, BufferedReader buffer) throws IOException {
            Map<String, Quest> quests = new HashMap<>();
            int questsCount = Integer.parseInt(buffer.readLine());
            for (int i = 0; i < questsCount; i++) {
                String[] questData = buffer.readLine().split(",");
                String questID = questData[0];
                String questName = questData[1];
                int stepsCount = Integer.parseInt(questData[2]);
                int stepIndex = Integer.parseInt(questData[3]);
                QuestStatus status = QuestStatus.valueOf(questData[4]);
                List<QuestStep> steps = new ArrayList<>();
                for (int s = 0; s < stepsCount; s++) {
                    steps.add(data.questSteps.get(buffer.readLine()));
                }
                quests.put(questID, new Quest(questName,steps,stepIndex,status));
            }
            return quests;
        }

        public static Quest parseQuest(BufferedReader buffer) throws IOException {
            String questName = buffer.readLine();
            int stepCount = Integer.parseInt(buffer.readLine());
            ArrayList<QuestStep> questSteps = new ArrayList<>();
            for (int s = 0; s < stepCount; s++) {
                HashMap<String, Integer> killsRequired = new HashMap<>();
                int killRequiredCount = Integer.parseInt(buffer.readLine());
                for (int i = 0; i < killRequiredCount; i++) {
                    String[] entry = buffer.readLine().split(",");
                    killsRequired.put(entry[0], Integer.parseInt(entry[1]));
                }
                questSteps.add(new QuestStep(killsRequired));
            }
            int stepIndex = Integer.parseInt(buffer.readLine());

            QuestStatus status = QuestStatus.valueOf(buffer.readLine());
            return new Quest(questName, questSteps, stepIndex, status);
        }

        public static void writeQuest(BufferedWriter buffer, Quest quest) throws IOException {
            buffer.write(quest.name);
            buffer.newLine();
            buffer.write(Integer.toString(quest.steps.size()));
            buffer.newLine();
            for (QuestStep step: quest.steps) {
                buffer.write(Integer.toString(step.killsRequired.size()));
                buffer.newLine();
                for (Map.Entry<String, Integer> entry : step.killsRequired.entrySet()) {
                    buffer.write(entry.getKey() + "," + entry.getValue());
                    buffer.newLine();
                }
            }
            buffer.write(Integer.toString(quest.stepIndex));
            buffer.newLine();
            buffer.write(quest.status.name());
            buffer.newLine();
        }
    }

    public static class Persistor {
        public static void saveQuestsToBuffer(Map<String, Quest> quests, BufferedWriter buffer) throws IOException {
            buffer.write(Integer.toString(quests.size()));
            buffer.newLine();
        }
    }

    private QuestStatus status;
    String name;
    int stepIndex;
    List<QuestStep> steps;

    Quest(String name, List<QuestStep> steps, int stepIndex, QuestStatus status) {
        this.name = name;
        this.steps = steps;
        this.stepIndex = stepIndex;
        this.status = status;
    }

    void finish(Player player) {
        this.status = QuestStatus.FINISHED;
    }

    public boolean isFinished() {
        return this.status == QuestStatus.FINISHED;
    }

    public void npcDeath(String name) {
        steps.get(stepIndex).npcDeath(name);
        if (isQuestComplete()) {
            status = QuestStatus.COMPLETE;
        }
    }

    private boolean isQuestComplete() {
        return stepIndex + 1 == steps.size() && steps.get(stepIndex).isFinished();
    }

    public Quest start() {
        return new Quest(name, steps, stepIndex, QuestStatus.STARTED);
    }

    public boolean hasStatus(QuestStatus value) {
        return status == value;
    }

}
