package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Uses {
    static class Provider {
        static Map<String, Map<String, ItemActivity>> loadItemUsesFromJson(ScenarioData data, JSONArray itemUsesData) {
            HashMap<String, Map<String, ItemActivity>> itemUses = new HashMap<>();
            for (Object iUO : itemUsesData) {
                JSONObject itemUseData = (JSONObject) iUO;
                String item1 = (String) itemUseData.get("item1");
                String item2 = (String) itemUseData.get("item2");
                String use = (String) itemUseData.get("use");
                Map<String, ItemActivity> item1Uses;
                if (itemUses.containsKey(item1)) {
                    item1Uses = itemUses.get(item1);
                } else {
                    item1Uses = new HashMap<>();
                    itemUses.put(item1, item1Uses);
                }
                item1Uses.put(item2, data.itemActivities.get(use));
            }
            return itemUses;
        }

        static Map<String, Map<String, ItemActivity>> loadTileItemUsesFromJson(ScenarioData data, JSONArray tileItemUsesData) {
            Map<String, Map<String, ItemActivity>> tileItemUses = new HashMap<>();
            for (Object tIUO : tileItemUsesData) {
                JSONObject tileItemUseData = (JSONObject) tIUO;
                String tile = (String) tileItemUseData.get("tile");
                String item = (String) tileItemUseData.get("item");
                String use = (String) tileItemUseData.get("use");
                Map<String, ItemActivity> tileUses;
                if (tileItemUses.containsKey(tile)) {
                    tileUses = tileItemUses.get(tile);
                } else {
                    tileUses = new HashMap<>();
                    tileItemUses.put(tile, tileUses);
                }
                tileUses.put(item, data.itemActivities.get(use));
            }
            return tileItemUses;
        }

        static Map<String, Map<String, ItemActivity>> loadObjectItemUsesFromJson(ScenarioData data, JSONArray objectItemUsesData) {
            Map<String, Map<String, ItemActivity>> objectItemUses = new HashMap<>();
            for (Object oIUO : objectItemUsesData) {
                JSONObject objectItemUseData = (JSONObject) oIUO;
                String object = (String) objectItemUseData.get("object");
                String item = (String) objectItemUseData.get("item");
                String use = (String) objectItemUseData.get("use");
                Map<String, ItemActivity> objectUses;
                if (objectItemUses.containsKey(object)) {
                    objectUses = objectItemUses.get(object);
                } else {
                    objectUses = new HashMap<>();
                    objectItemUses.put(object, objectUses);
                }
                objectUses.put(item, data.itemActivities.get(use));
            }
            return objectItemUses;
        }
    }

    public static class Persistor {
        public static JSONArray saveTileItemUsesToJson(Map<String, Map<String, ItemActivity>> tileItemUses) {
            //TODO:
            return new JSONArray();
        }

        public static JSONArray saveObjectItemUsesToJson(Map<String, Map<String, ItemActivity>> objectItemUses) {
            //TODO:
            return new JSONArray();
        }

        public static JSONArray saveItemUsesToJson(Map<String, Map<String, ItemActivity>> itemUses) {
            //TODO:
            return new JSONArray();
        }
    }
}
