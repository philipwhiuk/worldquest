package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuestProvider {
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

        Boolean finished = Boolean.parseBoolean(buffer.readLine());
        return new Quest(questName, questSteps, stepIndex, finished);
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
        buffer.write(Boolean.toString(quest.isFinished()));
        buffer.newLine();
    }
}
