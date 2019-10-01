package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import static com.whiuk.philip.worldquest.JsonUtils.intFromObj;

public class GameObjectLoader {

    static void loadGameObjects(Map<String, GObjects.GameObjectBuilder> gameObjectBuilders, JSONArray gameObjectsData, Tile[][] newMap) {
        for (Object gO : gameObjectsData) {
            try {
                JSONObject gameObjectData = (JSONObject) gO;

                int x = intFromObj(gameObjectData.get("x"));
                int y = intFromObj(gameObjectData.get("y"));
                String objectType = (String) gameObjectData.get("type");
                GObjects.GameObjectBuilder builder = gameObjectBuilders.get(objectType);
                if (builder == null) {
                    throw new IllegalArgumentException("Unknown object type: " + objectType);
                }
                newMap[x][y].objects.add(builder.build(gameObjectData));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid object format for game object: "+gO, e);
            }
        }
    }

    public static void saveGameObjects(Tile[][] map) throws IOException {
        //TODO:
        /**
        int count = 0;
        for (Tile[] tileColumn : map) {
            for(Tile tile: tileColumn) {
                count += tile.objects.size();
            }
        }
        buffer.write(Integer.toString(count));
        buffer.newLine();
        for (int x = 0; x < map.length;  x++) {
            for(int y = 0; y < map[x].length;  y++) {
                for (GObjects.GameObject gameObject: map[x][y].objects) {
                    String objectData = x+","+y+","+gameObject.id()+","+gameObject.asString().replaceAll(",",":");
                    buffer.write(objectData);
                    buffer.newLine();
                }
            }
        }
         **/
    }
}
