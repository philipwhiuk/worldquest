package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import static com.whiuk.philip.worldquest.MapConstants.MAP_HEIGHT;
import static com.whiuk.philip.worldquest.MapConstants.MAP_WIDTH;

public class MapTileLoader {

    static Tile[][] loadMapTiles(Map<String, TileType> tileTypes, BufferedReader buffer) throws IOException {
        Tile[][] newMap = new Tile[MAP_WIDTH][MAP_HEIGHT];
        int mapLines = Integer.parseInt(buffer.readLine());
        if (mapLines != MAP_HEIGHT) {
            throw new RuntimeException("Invalid map.dat size");
        }
        for (int y = 0; y < MAP_HEIGHT; y++) {
            String mapLine = buffer.readLine();
            if (mapLine != null) {
                processMapLine(tileTypes, newMap, y, mapLine);
            }
        }
        return newMap;
    }

    private static void processMapLine(Map<String, TileType> tileTypes, Tile[][] map, int y, String mapLine) {
        String[] tileData = mapLine.split(",");
        for (int x = 0; x < tileData.length; x++) {
            //Currently the per tile data is just the tileType.
            String tileTypeName = tileData[x];
            TileType tileType = tileTypes.get(tileTypeName);
            if (tileType == null) {
                throw new RuntimeException("Invalid tile type: " + tileTypeName);
            }
            map[x][y] = new Tile(tileType);
        }
    }
}
