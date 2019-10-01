package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.whiuk.philip.worldquest.JsonUtils.intFromObj;
import static com.whiuk.philip.worldquest.JsonUtils.parseColor;

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
        static Map<String, NPCType> loadNPCTypesFromJson(ScenarioData data, JSONArray npcTypesData) {
            Map<String, NPCType> npcTypes = new HashMap<>();
            for (Object nTO: npcTypesData) {
                JSONObject npcTypeData = (JSONObject) nTO;
                String id = (String) npcTypeData.get("id");
                String name = (String) npcTypeData.get("name");
                Color color = parseColor((JSONObject) npcTypeData.get("color"));
                boolean canMove = (Boolean) npcTypeData.get("canMove");
                boolean canFight = (Boolean) npcTypeData.get("canFight");
                boolean isAggressive = (Boolean) npcTypeData.get("isAggressive");
                int health = intFromObj(npcTypeData.get("health"));
                int damage = intFromObj(npcTypeData.get("damage"));

                List<GObjects.ItemDrop> dropTable = new ArrayList<>();
                JSONArray dropTableData = (JSONArray) npcTypeData.getOrDefault("dropTable", new JSONArray());
                for (Object dTO : dropTableData) {
                    JSONObject itemDropData = (JSONObject) dTO;
                    switch ((String) itemDropData.get("type")) {
                        case "Item":
                            dropTable.add(new GObjects.ItemDrop(data.itemType((String) itemDropData.get("item")).create()));
                            break;
                        case "Money":
                            dropTable.add(new GObjects.ItemDrop(intFromObj(itemDropData.get("quantity"))));
                            break;
                    }
                }
                boolean canTalk = (Boolean) npcTypeData.get("canTalk");
                Conversation conversation = null;
                if (canTalk) {
                    String conversationStarter = (String) npcTypeData.get("conversationStarter");
                    switch (conversationStarter) {
                        case "QuestTreeSwitchConversation":
                            String questName = (String) npcTypeData.get("quest");
                            JSONArray questStatusSwitchData = (JSONArray) npcTypeData.get("questStatusSwitch");
                            Map<QuestStatus, String> statusMap = new HashMap<>();
                            for(Object qSO : questStatusSwitchData) {
                                JSONObject questStatusData = (JSONObject) qSO;
                                statusMap.put(
                                        QuestStatus.valueOf((String) questStatusData.get("status")),
                                        (String) questStatusData.get("conversationStarter"));
                            }
                            conversation = new Conversation(new QuestTreeSwitchSelector(questName, statusMap));
                            break;
                        case "ConversationChoice":
                            String choice = (String) npcTypeData.get("conversationChoice");
                            conversation = new Conversation(state -> data.conversationChoices.get(choice));
                            break;
                    }
                }
                String shop = (String) npcTypeData.get("shop");
                NPCType npcType = new NPCType(
                        id, name, color, canMove, canFight, isAggressive, health, damage,
                        dropTable.toArray(new GObjects.ItemDrop[]{}), canTalk, conversation,
                        data.shops.get(shop));
                npcTypes.put(id, npcType);
            }
            return npcTypes;
        }
    }

    public static class Persistor {
        public static JSONArray saveNPCTypesToJson(Map<String, NPCType> npcTypes) throws IOException {
            //TODO:
            return new JSONArray();
        }
    }
}