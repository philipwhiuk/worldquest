package com.whiuk.philip.worldquest;

import java.awt.*;

public class TileType {
    final Color color;
    final Color fowColor;
    private final boolean canMoveTo;

    TileType(Color color, Color fowColor, boolean canMoveTo) {
        this.color = color;
        this.fowColor = fowColor;
        this.canMoveTo = canMoveTo;
    }

    public boolean canMoveTo() {
        return canMoveTo;
    }
}