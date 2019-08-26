package com.whiuk.philip.worldquest;

import java.util.Map;
import java.util.function.Function;

class QuestTreeSwitchSelector implements Function<QuestState, ConversationChoice> {
    private final Map<QuestStatus, String> statusMap;
    private final String questName;

    QuestTreeSwitchSelector(String questName, Map<QuestStatus, String> statusMap) {
        this.questName = questName;
        this.statusMap = statusMap;
    }

    @Override
    public ConversationChoice apply(QuestState questState) {
        return questState.scenario.conversationChoices.get(statusMap.get(questState.player.getQuestState(questName)));
    }
}
