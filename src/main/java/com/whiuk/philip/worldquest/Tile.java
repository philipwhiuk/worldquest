package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class Tile {
    private final TileType type;
    final List<GObjects.GameObject> objects;
    private boolean hasSeen = false;

    Tile(TileType type) {
        this.type = type;
        objects = new ArrayList<>();
    }

    boolean canMoveTo() {
        if (type.canMoveTo()) {
            boolean canMove = true;
            for (GObjects.GameObject obj : objects) {
                if (!obj.canMoveTo()) {
                    canMove = false;
                }
            }
            return canMove;
        }
        return false;
    }

    public Color getColor(boolean isVisible) {
        if (isVisible) {
            hasSeen = true;
            return type.color;
        } else if (hasSeen) {
            return type.fowColor;
        } else {
            return Color.BLACK;
        }
    }

    public void onMoveTo(Player player) {
        objects.forEach(obj -> obj.onMoveTo(player));
        objects.removeIf(GObjects.GameObject::isDeleted);
    }
}
