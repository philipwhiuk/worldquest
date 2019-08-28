package com.whiuk.philip.worldquest;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NPCType {
    final String id;
    final String name;
    final Color color;
    public final boolean canMove;
    public final boolean canFight;
    public final boolean isAggressive;
    public int health;
    public int damage;
    public GObjects.ItemDrop[] dropTable;
    public final boolean canTalk;
    final Conversation conversation;
    public Shop shop;

    NPCType(String id, String name, Color color,
            boolean canMove,
            boolean canFight, boolean isAggressive,
            int health, int damage,
            GObjects.ItemDrop[] dropTable,
            boolean canTalk, Conversation conversation, Shop shop) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.canMove = canMove;
        this.canFight = canFight;
        this.isAggressive = isAggressive;
        this.health = health;
        this.damage = damage;
        this.dropTable = dropTable;
        this.canTalk = canTalk;
        this.conversation = conversation;
        this.shop = shop;
    }

    static class Provider {
        static Map<String, NPCType> loadNPCTypesFromBuffer(ScenarioData data, BufferedReader buffer) throws IOException {
            int npcTypeCount = Integer.parseInt(buffer.readLine());
            Map<String, NPCType> npcTypes = new HashMap<>();
            for (int n = 0; n < npcTypeCount; n++) {
                String[] npcTypeData = buffer.readLine().split(",");
                String id = npcTypeData[0];
                String name = npcTypeData[1];
                Color color = new Color(Integer.parseInt(npcTypeData[2]),Integer.parseInt(npcTypeData[3]),Integer.parseInt(npcTypeData[4]));
                boolean canMove = Boolean.parseBoolean(npcTypeData[5]);
                boolean canFight = Boolean.parseBoolean(npcTypeData[6]);
                boolean isAggressive = Boolean.parseBoolean(npcTypeData[7]);
                int health = Integer.parseInt(npcTypeData[8]);
                int damage = Integer.parseInt(npcTypeData[9]);
                int itemDropCount = Integer.parseInt(npcTypeData[10]);
                GObjects.ItemDrop[] dropTable = new GObjects.ItemDrop[itemDropCount];
                for (int i = 0; i < itemDropCount ; i ++ ) {
                    String itemDropData[] = buffer.readLine().split(",");
                    switch (itemDropData[0]) {
                        case "Item":
                            dropTable[i] = new GObjects.ItemDrop(data.item(itemDropData[1]).copy());
                            break;
                        case "Money":
                            dropTable[i] = new GObjects.ItemDrop(Integer.parseInt(itemDropData[1]));
                            break;
                    }
                }
                boolean canTalk = Boolean.parseBoolean(npcTypeData[11]);
                Conversation conversation = null;
                if (canTalk) {
                    String[] conversationData = buffer.readLine().split(",");
                    switch (conversationData[0]) {
                        case "QuestTreeSwitchConversation":
                            String questName = conversationData[1];
                            int statusCount = Integer.parseInt(conversationData[2]);
                            Map<QuestStatus, String> statusMap = new HashMap<>();
                            for(int s = 0 ; s < statusCount; s++) {
                                String[] questStatusData = buffer.readLine().split(",");
                                statusMap.put(QuestStatus.valueOf(questStatusData[0]), questStatusData[1]);
                            }
                            conversation = new Conversation(new QuestTreeSwitchSelector(questName, statusMap));
                            break;
                        case "ConversationChoice":
                            conversation = new Conversation(state -> data.conversationChoices.get(conversationData[1]));
                            break;
                    }
                }
                String shop = npcTypeData[12];
                NPCType npcType = new NPCType(
                        id, name, color, canMove, canFight, isAggressive, health, damage, dropTable, canTalk, conversation,
                        data.shops.get(shop));
                npcTypes.put(id, npcType);
            }
            return npcTypes;
        }
    }

    public static class Persistor {
        public static void saveNPCTypesToBuffer(Map<String, NPCType> npcTypes, BufferedWriter buffer) throws IOException {
            buffer.write(Integer.toString(npcTypes.size()));
            buffer.newLine();
        }
    }
}