package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.whiuk.philip.worldquest.JsonUtils.intFromObj;

public class NPCLoader {

    static List<NPC> loadNPCs(Map<String, NPCType> npcTypes, JSONArray data) throws IOException {
        List<NPC> npcs = new ArrayList<>(data.size());
        for (Object nO : data) {
            JSONObject npcData = (JSONObject) nO;
            String type = (String) npcData.get("type");
            NPCType npcType = npcTypes.get(type);
            if (npcType == null) {
                throw new IllegalArgumentException("Unknown NPC type: " + type);
            }
            npcs.add(new NPC(npcType, intFromObj(npcData.get("x")), intFromObj(npcData.get("y")),
                    MovementStrategy.parseStrategy((JSONObject) npcData.get("movementStrategy"))));
        }
        return npcs;
    }

    public static void saveNPCs(Map<String, NPCType> npcTypes, List<NPC> npcs) throws IOException {
        //TODO:
    }
}
