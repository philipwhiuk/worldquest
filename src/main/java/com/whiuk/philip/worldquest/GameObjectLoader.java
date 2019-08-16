package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

public class GameObjectLoader {

    static void loadGameObjects(Map<String, GObjects.GameObjectBuilder> gameObjectBuilders, BufferedReader buffer, Tile[][] newMap) throws IOException {
        int gameObjectsCount = Integer.parseInt(buffer.readLine());
        for (int i = 0; i < gameObjectsCount; i++) {
            try {
                String[] gameObjectData = buffer.readLine().split(",");

                int x = Integer.parseInt(gameObjectData[0]);
                int y = Integer.parseInt(gameObjectData[1]);
                String[] args = (gameObjectData.length > 3) ? gameObjectData[3].split(":") : new String[]{};
                String objectType = gameObjectData[2];
                GObjects.GameObjectBuilder builder = gameObjectBuilders.get(objectType);
                if (builder == null) {
                    throw new IllegalArgumentException("Unknown object type: " + objectType);
                }
                newMap[x][y].objects.add(builder.build(args));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid object format for game object: "+i, e);
            }
        }
    }

    public static void saveGameObjects(BufferedWriter buffer, Tile[][] map) throws IOException {
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

    }
}
