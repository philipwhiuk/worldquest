package com.whiuk.philip.worldquest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class QuestStatePredicate implements Predicate<QuestState> {
    private final HashMap<String, QuestStatus> requiredQuestState;

    public QuestStatePredicate(HashMap<String, QuestStatus> questState) {
        this.requiredQuestState = questState;
    }

    @Override
    public boolean test(QuestState questState) {
        for(Map.Entry<String, QuestStatus> requiredQuest: requiredQuestState.entrySet()) {
            Quest playerQuest = questState.player.quests.get(requiredQuest.getKey());

            if (playerQuest == null) {
                if (requiredQuest.getValue() != QuestStatus.NOT_STARTED) {
                    return false;
                }
            } else if (!playerQuest.hasStatus(requiredQuest.getValue())) {
                return false;
            }
        }
        return true;
    }
}
