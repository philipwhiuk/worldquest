package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ConversationChoicesProvider {
    static Map<String, ConversationChoice> loadConversationChoicesFromBuffer(BufferedReader buffer) throws IOException {
        int choicesCount = Integer.parseInt(buffer.readLine());
        Map<String, ConversationChoice> conversationChoices = new HashMap<>();
        for (int c = 0; c < choicesCount; c++) {
            String[] choiceData = buffer.readLine().split(",");
            String id = choiceData[0];
            String playerText = choiceData[1];
            String npcText = choiceData[2];
            boolean hasNpcAction = Boolean.parseBoolean(choiceData[3]);
            boolean hasCanSee = Boolean.parseBoolean(choiceData[4]);
            NPCAction npcAction = null;
            Predicate<QuestState> canSee = null;
            if (hasNpcAction) {
                String[] npcActionData = buffer.readLine().split(",");
                switch (npcActionData[0]) {
                    case "ShopDisplay":
                        npcAction = new ShopDisplay();
                        break;
                    case "ConversationChoiceSelection":
                        int qSCount = Integer.parseInt(npcActionData[1]);
                        List<String> choices = new ArrayList<>();
                        for (int q = 0; q < qSCount ; q++) {
                            choices.add(buffer.readLine());
                        }
                        npcAction = new ConversationChoiceSelection(choices);
                        break;
                    case "QuestStartAction":
                        npcAction = new QuestStartAction(npcActionData[1]);
                        break;
                    case "QuestFinishAction":
                        npcAction = new QuestFinishAction(npcActionData[1]);
                }
            }
            if (hasCanSee) {
                String[] canSeeData = buffer.readLine().split(",");
                switch (canSeeData[0]) {
                    case "Always":
                        canSee = (state) -> true;
                        break;
                    case "QuestState":
                        int qSCount = Integer.parseInt(canSeeData[1]);
                        HashMap<String, QuestStatus> questState = new HashMap<>();
                        for (int q = 0; q < qSCount ; q++) {
                            String[] qsData = buffer.readLine().split(",");
                            questState.put(qsData[0], QuestStatus.valueOf(qsData[1]));
                        }
                        canSee = new QuestStatePredicate(questState);
                        break;
                }
            }

            conversationChoices.put(id, new ConversationChoice(playerText, npcText, npcAction, canSee));
        }
        return conversationChoices;
    }
}
