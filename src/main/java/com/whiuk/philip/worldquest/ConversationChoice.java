package com.whiuk.philip.worldquest;

import java.util.List;
import java.util.Map;

class ConversationChoice {
    final String playerText;
    final String npcResponse;
    NPCAction npcAction;
    ConversationChoice(String player, String npcResponse, NPCAction npcAction) {
        this.playerText = player;
        this.npcResponse = npcResponse;
        this.npcAction = npcAction;
    }
}

interface NPCAction {
    void doAction(WorldQuest game, NPC npc);
}

class ConversationChoiceSelection implements NPCAction {
    private List<ConversationChoice> conversationOptions;

    ConversationChoiceSelection(List<ConversationChoice> conversationOptions) {
        this.conversationOptions = conversationOptions;
    }

    public void doAction(WorldQuest game, NPC npc) {
        game.showOptions(conversationOptions);
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
    }
}
