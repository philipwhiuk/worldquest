package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QuestStepsProvider {

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
