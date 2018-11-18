package com.whiuk.philip.worldquest;

import java.util.List;
import java.util.function.Predicate;

class ConversationChoice {
    final String playerText;
    final String npcResponse;
    NPCAction npcAction;
    final Predicate<QuestState> canSee;

    ConversationChoice(String player, String npcResponse, NPCAction npcAction, Predicate<QuestState> canSee) {
        this.playerText = player;
        this.npcResponse = npcResponse;
        this.npcAction = npcAction;
        this.canSee = canSee;
    }
}

interface NPCAction {
    void doAction(WorldQuest game, NPC npc);
}

class ConversationChoiceSelection implements NPCAction {
    List<ConversationChoice> conversationOptions;

    ConversationChoiceSelection(List<ConversationChoice> conversationOptions) {
        this.conversationOptions = conversationOptions;
    }

    public void doAction(WorldQuest game, NPC npc) {
        game.setMessageState(WorldQuest.MessageState.CONVERSATION_OPTION);
    }
}

class ShopDisplay implements NPCAction {

    public void doAction(WorldQuest game, NPC npc) {
        game.endConversation(npc);
        game.showShop(npc.shop);
    }
}

class QuestStartAction implements NPCAction {
    private final String questName;

    QuestStartAction(String questName) {
        this.questName = questName;
    }

    public void doAction(WorldQuest game, NPC npc) {
        game.startQuest(questName);
        game.endConversation(npc);
    }
}
