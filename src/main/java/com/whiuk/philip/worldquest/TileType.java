package com.whiuk.philip.worldquest;

import java.awt.*;

public class TileType {
    final Color color;
    final Color fowColor;
    private final boolean canMoveTo;
    public boolean isOutdoors;

    TileType(Color color, Color fowColor, boolean canMoveTo, boolean isOutdoors) {
        this.color = color;
        this.fowColor = fowColor;
        this.canMoveTo = canMoveTo;
        this.isOutdoors = isOutdoors;
    }

    public boolean canMoveTo() {
        return canMoveTo;
    }
}