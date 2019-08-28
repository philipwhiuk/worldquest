package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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
        public static void saveConversationChoicesToBuffer(Map<String, ConversationChoice> conversationChoices, BufferedWriter buffer) throws IOException {
            buffer.write(Integer.toString(conversationChoices.size()));
            buffer.newLine();
        }
    }
    static class Provider {
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
