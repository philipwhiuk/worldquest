package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.whiuk.philip.worldquest.JsonUtils.intFromObj;

public class RoomLoader {
    public static List<Room> loadRooms(JSONArray roomsData, Tile[][] newMap) {
        List<Room> rooms = new ArrayList<>(roomsData.size());
        for (Object rO : roomsData) {
            JSONObject roomData = (JSONObject) rO;
            Room room = new Room((String) roomData.get("name"),
                    intFromObj(roomData.get("x")),
                    intFromObj(roomData.get("y")),
                    intFromObj(roomData.get("width")),
                    intFromObj(roomData.get("height")));
            rooms.add(room);
            for (int x = room.getMinX(); x < room.getMaxX(); x++) {
                for (int y = room.getMinY(); y < room.getMaxY(); y++) {
                    newMap[x][y].room = room;
                }
            }
        }
        return rooms;
    }

    public static void saveRooms(BufferedWriter buffer, List<Room> rooms) throws IOException {
        buffer.write(Integer.toString(rooms.size()));
        buffer.newLine();
        for (Room room: rooms) {
            buffer.write(room.name);
            buffer.newLine();
            buffer.write(room.x+","+room.y+","+room.width+","+room.height);
            buffer.newLine();
        }
    }
}
