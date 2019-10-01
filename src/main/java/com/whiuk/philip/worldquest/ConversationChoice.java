package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

class Conversation {
    Function<QuestState, ConversationChoice> selector;

    Conversation(Function<QuestState, ConversationChoice> selector) {
        this.selector = selector;
    }
}

class ConversationChoice {
    static class Persistor {
        public static JSONArray saveConversationChoicesToJson(Map<String, ConversationChoice> conversationChoices) {
            //TODO:
            return new JSONArray();
        }
    }
    static class Provider {
        static Map<String, ConversationChoice> loadConversationChoicesFromJson(JSONArray conversationChoicesData) {
            Map<String, ConversationChoice> conversationChoices = new HashMap<>();
            for (Object cCO: conversationChoicesData) {
                JSONObject choiceData = (JSONObject) cCO;
                String id = (String) choiceData.get("id");
                String playerText = (String) choiceData.get("playerText");
                String npcText = (String) choiceData.get("npcText");
                boolean hasNpcAction = (Boolean) choiceData.get("hasNpcAction");
                boolean hasCanSee = (Boolean) choiceData.get("hasCanSee");
                NPCAction npcAction = null;
                Predicate<QuestState> canSee = null;
                if (hasNpcAction) {
                    String npcActionType = (String) choiceData.get("npcAction");
                    switch (npcActionType) {
                        case "ShopDisplay":
                            npcAction = new ShopDisplay();
                            break;
                        case "ConversationChoiceSelection":
                            JSONArray selectionData = (JSONArray) choiceData.get("conversationChoices");
                            List<String> choices = new ArrayList<>();
                            for (Object option : selectionData) {
                                choices.add((String) option);
                            }
                            npcAction = new ConversationChoiceSelection(choices);
                            break;
                        case "QuestStartAction":
                            npcAction = new QuestStartAction((String) choiceData.get("quest"));
                            break;
                        case "QuestFinishAction":
                            npcAction = new QuestFinishAction((String) choiceData.get("quest"));
                    }
                }
                if (hasCanSee) {
                    String canSeeType = (String) choiceData.get("canSee");
                    switch (canSeeType) {
                        case "Always":
                            canSee = (state) -> true;
                            break;
                        case "QuestState":
                            JSONArray questStatusData = (JSONArray) choiceData.get("questStatus");
                            HashMap<String, QuestStatus> questState = new HashMap<>();
                            for (Object sO : questStatusData) {
                                JSONObject status = (JSONObject) sO;
                                questState.put((String) status.get("quest"), QuestStatus.valueOf((String) status.get("state")));
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
    List<String> conversationOptions;

    ConversationChoiceSelection(List<String> conversationOptions) {
        this.conversationOptions = conversationOptions;
    }

    public void doAction(WorldQuest game, NPC npc) {
        game.setMessageState(MessageState.CONVERSATION_OPTION);
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

class QuestFinishAction implements NPCAction {
    private final String questName;

    QuestFinishAction(String questName) {
        this.questName = questName;
    }

    public void doAction(WorldQuest game, NPC npc) {
        game.player.getQuest(questName).finish(game.player);
        game.endConversation(npc);
    }
}
