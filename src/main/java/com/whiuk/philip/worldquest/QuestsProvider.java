package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestsProvider {

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
}
