package com.whiuk.philip.worldquest;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TileType {
    public static class Provider {
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

    public static class Persistor {
        public static void saveTileTypesToBuffer(Map<Integer, TileType> tileTypes, BufferedWriter buffer) throws IOException {
            buffer.write(Integer.toString(tileTypes.size()));
            buffer.newLine();
            for (Map.Entry<Integer, TileType> tileTypeEntry : tileTypes.entrySet()) {
                TileType tileType = tileTypeEntry.getValue();
                buffer.write(String.join(",", new String[]{
                        tileType.name,
                        Integer.toString(tileType.id),
                        tileType.name,
                        Integer.toString(tileType.color.getRed()),
                        Integer.toString(tileType.color.getGreen()),
                        Integer.toString(tileType.color.getBlue()),
                        Integer.toString(tileType.fowColor.getRed()),
                        Integer.toString(tileType.fowColor.getGreen()),
                        Integer.toString(tileType.fowColor.getBlue()),
                        Boolean.toString(tileType.canMoveTo),
                        Boolean.toString(tileType.isOutdoors),
                        Boolean.toString(tileType.blocksView)
                }));
                buffer.newLine();
            }
        }
    }

    final int id;
    final String name;
    final Color color;
    final Color fowColor;
    private final boolean canMoveTo;
    public boolean isOutdoors;
    public boolean blocksView;

    TileType(int id, String name, Color color, Color fowColor, boolean canMoveTo, boolean isOutdoors, boolean blocksView) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.fowColor = fowColor;
        this.canMoveTo = canMoveTo;
        this.isOutdoors = isOutdoors;
        this.blocksView = blocksView;
    }

    public boolean canMoveTo(Direction directionMoving) {
        return canMoveTo;
    }

}