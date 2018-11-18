package com.whiuk.philip.worldquest;

import java.awt.*;

public class TileType {
    final String name;
    final Color color;
    final Color fowColor;
    private final boolean canMoveTo;
    public boolean isOutdoors;

    TileType(String name, Color color, Color fowColor, boolean canMoveTo, boolean isOutdoors) {
        this.name = name;
        this.color = color;
        this.fowColor = fowColor;
        this.canMoveTo = canMoveTo;
        this.isOutdoors = isOutdoors;
    }

    public boolean canMoveTo(Direction directionMoving) {
        return canMoveTo;
    }
}