package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

import static com.whiuk.philip.worldquest.JsonUtils.intFromObj;
import static com.whiuk.philip.worldquest.MapConstants.MAP_TILES_HEIGHT;
import static com.whiuk.philip.worldquest.MapConstants.MAP_TILES_WIDTH;

public class MapTileLoader {

    static final int DEFAULT_TILE_ID = 1;

    static Tile[][] loadMapTiles(Map<Integer, TileType> tileTypes, JSONArray mapTileData) throws IOException {
        Tile[][] newMap = new Tile[MAP_TILES_WIDTH][MAP_TILES_HEIGHT];
        if (mapTileData.size() != MAP_TILES_HEIGHT) {
            throw new RuntimeException("Invalid map.json size");
        }

        TileFromDataFunction createTileFunction = (data, x, y) -> {
            int tileTypeId = data;
            TileType tileType = tileTypes.get(tileTypeId);
            if (tileType == null) {
                throw new RuntimeException("Invalid tile type: " + tileTypeId);
            }
            newMap[x][y] = new Tile(tileType, x, y);
        };

        processMapData(mapTileData, createTileFunction);

        return newMap;
    }

    static void markExploration(final Tile[][] map, JSONArray mapExplorationData) throws IOException {
        if (mapExplorationData.size() != MAP_TILES_HEIGHT) {
            throw new RuntimeException("Invalid map.json exploration size");
        }
        processMapData(mapExplorationData, (data, x, y) -> map[x][y].hasSeen = data == 1);
    }

    private static void processMapData(JSONArray mapData, TileFromDataFunction mapFunction) throws IOException {
        for (int y = 0; y < MAP_TILES_HEIGHT; y++) {
            JSONArray mapLine = (JSONArray) mapData.get(y);
            if (mapLine != null) {
                for (int x = 0; x < mapLine.size(); x++) {
                    mapFunction.apply(intFromObj(mapLine.get(x)), x, y);
                }
            }
        }
    }

    public static Tile[][] newMap(Map<Integer, TileType> tileTypes) {
        Tile[][] newMap = new Tile[MAP_TILES_WIDTH][MAP_TILES_HEIGHT];
        TileCreateFunction createTileFunction = (x, y) -> {
            TileType tileType = tileTypes.get(DEFAULT_TILE_ID);
            if (tileType == null) {
                throw new RuntimeException("Invalid tile type: " + DEFAULT_TILE_ID);
            }
            newMap[x][y] = new Tile(tileType, x, y);
        };

        for (int y = 0; y < MAP_TILES_HEIGHT; y++) {
            for (int x = 0; x < MAP_TILES_WIDTH; x++) {
                createTileFunction.apply(x, y);
            }
        }

        return newMap;
    }

    @FunctionalInterface
    interface TileFromDataFunction {
        void apply(Integer data, Integer x, Integer y);
    }

    @FunctionalInterface
    interface TileCreateFunction {
        void apply(Integer x, Integer y);
    }

    public static void saveMapTiles(Tile[][] map) throws IOException {
        //TODO:
        /**
        buffer.append(Integer.toString(map[0].length));
        buffer.newLine();
        for (int y = 0; y < map[0].length; y++) {
            StringBuilder mapLine = new StringBuilder(79);
            for (int x = 0; x < map.length; x++) {
                mapLine.append(map[x][y].type.id);
                mapLine.append(',');
            }
            buffer.append(mapLine);
            buffer.newLine();
        }
         **/
    }

    public static void saveMapExploration(Tile[][] map) throws IOException {
        //TODO:
        /**
        buffer.append(Integer.toString(map[0].length));
        buffer.newLine();
        for (int y = 0; y < map[0].length; y++) {
            StringBuilder mapLine = new StringBuilder(79);
            for (int x = 0; x < map.length; x++) {
                mapLine.append(map[x][y].hasSeen ? "1" : "0");
                mapLine.append(',');
            }
            buffer.append(mapLine);
            buffer.newLine();
        }
         **/
    }
}
