package com.whiuk.philip.worldquest;

class Room {
    final String name;
    final int x;
    final int y;
    final int width;
    final int height;

    Room(String name, int x, int y, int width, int height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getMinX() {
        return x;
    }

    public int getMinY() {
        return y;
    }

    public int getMaxX() {
        return x+width;
    }

    public int getMaxY() {
        return y+height;
    }
}
