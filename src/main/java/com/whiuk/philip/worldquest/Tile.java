package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class Tile {
    final TileType type;
    final List<GObjects.GameObject> objects;
    private boolean hasSeen = false;
    final int x;
    final int y;

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

    Color getColor(boolean isVisible) {
        if (isVisible) {
            hasSeen = true;
            return type.color;
        } else if (hasSeen) {
            return type.fowColor;
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
}
