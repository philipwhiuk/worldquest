package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import static com.whiuk.philip.worldquest.MapConstants.MAP_TILES_HEIGHT;
import static com.whiuk.philip.worldquest.MapConstants.MAP_TILES_WIDTH;

public class MapTileLoader {

    static Tile[][] loadMapTiles(Map<Integer, TileType> tileTypes, BufferedReader buffer) throws IOException {
        Tile[][] newMap = new Tile[MAP_TILES_WIDTH][MAP_TILES_HEIGHT];
        int mapLines = Integer.parseInt(buffer.readLine());
        if (mapLines != MAP_TILES_HEIGHT) {
            throw new RuntimeException("Invalid map.dat size");
        }
        for (int y = 0; y < MAP_TILES_HEIGHT; y++) {
            String mapLine = buffer.readLine();
            if (mapLine != null) {
                processMapLine(tileTypes, newMap, y, mapLine);
            }
        }
        return newMap;
    }

    private static void processMapLine(Map<Integer, TileType> tileTypes, Tile[][] map, int y, String mapLine) {
        String[] tileData = mapLine.split(",");
        for (int x = 0; x < tileData.length; x++) {
            int tileTypeId = Integer.parseInt(tileData[x]);
            TileType tileType = tileTypes.get(tileTypeId);
            if (tileType == null) {
                throw new RuntimeException("Invalid tile type: " + tileTypeId);
            }
            map[x][y] = new Tile(tileType, x, y);
        }
    }
}
