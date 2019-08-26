package com.whiuk.philip.worldquest;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TileTypesProvider {
    static Map<Integer, TileType> loadTileTypesFromBuffer(BufferedReader buffer) throws IOException {
        int tileTypesCount = Integer.parseInt(buffer.readLine());
        Map<Integer, TileType> tileTypes = new HashMap<>();
        for (int t = 0; t < tileTypesCount; t++) {
            String[] tileTypeData = buffer.readLine().split(",");
            String tileRefId =  tileTypeData[0];
            int id =  Integer.parseInt(tileTypeData[1]);
            String name = tileTypeData[2];
            Color color = new Color(Integer.parseInt(tileTypeData[3]),Integer.parseInt(tileTypeData[4]),Integer.parseInt(tileTypeData[5]));
            Color fowColor = new Color(Integer.parseInt(tileTypeData[6]),Integer.parseInt(tileTypeData[7]),Integer.parseInt(tileTypeData[8]));
            boolean canMoveTo = Boolean.parseBoolean(tileTypeData[9]);
            boolean isOutdoors = Boolean.parseBoolean(tileTypeData[10]);
            boolean blocksView = Boolean.parseBoolean(tileTypeData[11]);
            TileType tileType = new TileType(
                    id,
                    name,
                    color,
                    fowColor,
                    canMoveTo,
                    isOutdoors,
                    blocksView);
            tileTypes.put(id, tileType);
        }
        return tileTypes;
    }
}
