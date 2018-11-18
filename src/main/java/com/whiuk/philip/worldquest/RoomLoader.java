package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoomLoader {
    public static List<Room> loadRooms(BufferedReader bufferedReader, Tile[][] newMap) throws IOException {
        int roomCount = Integer.parseInt(bufferedReader.readLine());
        List<Room> rooms = new ArrayList<>(roomCount);
        for (int i = 0; i < roomCount; i++) {
            String roomName = bufferedReader.readLine();
            String[] args = bufferedReader.readLine().split(",");
            Room room = new Room(roomName,
                    Integer.parseInt(args[0]), Integer.parseInt(args[1]),
                    Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            rooms.add(room);
            for (int x = (int) room.getMinX(); x < room.getMaxX(); x++) {
                for (int y = (int) room.getMinY(); y < room.getMaxY(); y++) {
                    newMap[x][y].room = room;
                }
            }
        }
        return rooms;
    }
}
