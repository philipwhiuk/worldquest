package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class Tile {
    static Color ROOF_COLOR = new Color(80,38,14);
    static Color ROOF_FOW_COLOR = new Color(40,19,7);

    final TileType type;
    final List<GObjects.GameObject> objects;
    private boolean hasSeen = false;
    final int x;
    final int y;
    Room room;

    Tile(TileType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        objects = new ArrayList<>();
    }

    boolean canMoveTo(Direction directionMoving) {
        if (type.canMoveTo(directionMoving)) {
            boolean canMove = true;
            for (GObjects.GameObject obj : objects) {
                if (!obj.canMoveTo(directionMoving)) {
                    canMove = false;
                }
            }
            return canMove;
        }
        return false;
    }

    Color getColor(boolean isVisible, Room playerRoom) {
        if (isVisible) {
            hasSeen = true;
            if (isOutdoors() || room == playerRoom) {
                return type.color;
            } else {
                return ROOF_COLOR;
            }
        } else if (hasSeen) {
            if (isOutdoors() || room == playerRoom) {
                return type.fowColor;
            } else {
                return ROOF_FOW_COLOR;
            }
        } else {
            return Color.BLACK;
        }
    }

    void onMoveTo(Player player) {
        objects.forEach(obj -> obj.onMoveTo(player));
        objects.removeIf(GObjects.GameObject::isDeleted);
    }

    boolean isOutdoors() {
        return type.isOutdoors;
    }

    boolean blocksView() {
        return type.blocksView;
    }
}
