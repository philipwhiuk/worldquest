package com.whiuk.philip.worldquest;

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
    private Map<String, ConversationChoice> conversationOptions;

    public void doAction(WorldQuest game, NPC npc) {
       game.showOptions(conversationOptions);
    }
}

class ShopDisplay implements NPCAction {

    public void doAction(WorldQuest game, NPC npc) {
        game.showShop(npc.shop);
    }
}
