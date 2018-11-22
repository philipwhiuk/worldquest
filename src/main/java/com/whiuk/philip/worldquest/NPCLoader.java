package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NPCLoader {

    static List<NPC> loadNPCs(Map<String, NPCType> npcTypes, BufferedReader buffer) throws IOException {
        int npcCount = Integer.parseInt(buffer.readLine());
        List<NPC> npcs = new ArrayList<>(npcCount);
        for (int i = 0; i < npcCount; i++) {
            String[] npcData = buffer.readLine().split(",");
            NPCType npcType = npcTypes.get(npcData[0]);
            if (npcType == null) {
                throw new IllegalArgumentException("Unknown NPC type: " + npcData[0]);
            }
            npcs.add(new NPC(npcType, Integer.parseInt(npcData[1]), Integer.parseInt(npcData[2]),
                    MovementStrategy.parseStrategy(npcData[3].replaceAll("\\|",","))));
        }
        return npcs;
    }

    public static void saveNPCs(Map<String, NPCType> npcTypes, List<NPC> npcs, BufferedWriter buffer) throws IOException {
        buffer.write(Integer.toString(npcs.size()));
        buffer.newLine();
        for (NPC npc: npcs) {
            String npcData = npc.type.id+","+npc.x+","+npc.y+","+npc.movementStrategy.asString().replaceAll(",","\\|");
            buffer.write(npcData);
            buffer.newLine();
        }
    }
}
