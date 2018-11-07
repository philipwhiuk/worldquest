package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class GameObjectLoader {

    static void loadGameObjects(Map<String, GObjects.GameObjectBuilder> gameObjectBuilders, BufferedReader buffer, Tile[][] newMap) throws IOException {
        int gameObjectsCount = Integer.parseInt(buffer.readLine());
        for (int i = 0; i < gameObjectsCount; i++) {
            String[] gameObjectData = buffer.readLine().split(",");

            int x = Integer.parseInt(gameObjectData[0]);
            int y = Integer.parseInt(gameObjectData[1]);
            String[] args = (gameObjectData.length > 3) ? gameObjectData[3].split(":") : new String[]{};
            newMap[x][y].objects.add(gameObjectBuilders.get(gameObjectData[2]).build(args));
        }
    }
}
