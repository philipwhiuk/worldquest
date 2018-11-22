package com.whiuk.philip.worldquest;

import java.awt.*;

public class TileType {
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