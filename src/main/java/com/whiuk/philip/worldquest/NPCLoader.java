package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
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
            npcs.add(new NPC(npcType, Integer.parseInt(npcData[1]), Integer.parseInt(npcData[2])));
        }
        return npcs;
    }
}
