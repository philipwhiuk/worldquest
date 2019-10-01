package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.whiuk.philip.worldquest.JsonUtils.intFromObj;
import static com.whiuk.philip.worldquest.JsonUtils.parseColor;

public class TileType {
    public static class Provider {

        static Map<Integer, TileType> loadTileTypesFromJson(JSONArray tileTypesData) {
            Map<Integer, TileType> tileTypes = new HashMap<>();
            for (Object tTO : tileTypesData) {
                JSONObject tileTypeData = (JSONObject) tTO;
                String tileRefId = (String) tileTypeData.get("id");
                int id =  intFromObj(tileTypeData.get("uid"));
                String name = (String) tileTypeData.get("name");
                Color color = parseColor((JSONObject) tileTypeData.get("color"));
                Color fowColor = parseColor((JSONObject) tileTypeData.get("fowColor"));
                boolean canMoveTo = (Boolean) tileTypeData.get("canMoveTo");
                boolean isOutdoors = (Boolean) tileTypeData.get("isOutdoors");
                boolean blocksView = (Boolean) tileTypeData.get("blocksView");
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
        public static JSONArray saveTileTypesToJson(Map<Integer, TileType> tileTypes) throws IOException {
            //TODO:
            return new JSONArray();
            /**
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
             **/
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