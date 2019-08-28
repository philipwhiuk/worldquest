package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Uses {
    static class Provider {
        static HashMap<String, ItemAction> loadItemUsesFromBuffer(ScenarioData data, BufferedReader buffer) throws IOException {
            int itemUsesCount = Integer.parseInt(buffer.readLine());
            HashMap<String, ItemAction> itemUses = new HashMap<>();
            for (int i = 0; i < itemUsesCount; i++) {
                String[] itemUseData = buffer.readLine().split(":");
                itemUses.put(itemUseData[0], data.itemActions.get(itemUseData[1]));
            }
            return itemUses;
        }

        static Map<String, ItemAction> loadTileItemUsesFromBuffer(ScenarioData data, BufferedReader buffer) throws IOException {
            int tileItemUsesCount = Integer.parseInt(buffer.readLine());
            Map<String, ItemAction> tileItemUses = new HashMap<>();
            for (int t = 0; t < tileItemUsesCount; t++) {
                String[] tileItemUseData = buffer.readLine().split(":");
                tileItemUses.put(tileItemUseData[0], data.itemActions.get(tileItemUseData[1]));
            }
            return tileItemUses;
        }

        static Map<String, ItemAction> loadObjectItemUsesFromBuffer(ScenarioData data, BufferedReader buffer) throws IOException {
            int objectItemUsesCount = Integer.parseInt(buffer.readLine());
            Map<String, ItemAction> objectItemUses = new HashMap<>();
            for (int t = 0; t < objectItemUsesCount; t++) {
                String[] objectItemUseData = buffer.readLine().split(":");
                objectItemUses.put(objectItemUseData[0], data.itemActions.get(objectItemUseData[1]));
            }
            return objectItemUses;
        }
    }

    public static class Persistor {
        public static void saveTileItemUsesToBuffer(Map<String, ItemAction> tileItemUses, BufferedWriter buffer) throws IOException {
            buffer.write(Integer.toString(tileItemUses.size()));
            buffer.newLine();
        }

        public static void saveObjectItemUsesToBuffer(Map<String, ItemAction> objectItemUses, BufferedWriter buffer) throws IOException {
            buffer.write(Integer.toString(objectItemUses.size()));
            buffer.newLine();
        }

        public static void saveItemUsesToBuffer(HashMap<String, ItemAction> itemUses, BufferedWriter buffer) throws IOException {
            buffer.write(Integer.toString(itemUses.size()));
            buffer.newLine();
        }
    }
}
