package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.UI;

import java.awt.*;
import java.awt.event.MouseEvent;

import static com.whiuk.philip.worldquest.MapConstants.*;
import static com.whiuk.philip.worldquest.MapConstants.MAP_SPACING;
import static com.whiuk.philip.worldquest.MapConstants.TILE_HEIGHT;

public class MapView implements UI {

    @Override
    public void render(Graphics2D g) {
        MapViewPainter.paintMapView(g, WorldQuest.this, map, visibleNpcs, player);
    }

    @Override
    public void onClick(MouseEvent e) {
        Tile t = checkForTileInterception(e.getPoint());
        if (t != null) {
            processTileClick(t);
        }
    }

    Tile checkForTileInterception(Point p) {
        double x = p.getX();
        double y = p.getY();
        if (x > MAP_SPACING && x < MAP_SPACING+MAP_WIDTH & y > MAP_SPACING & y < MAP_SPACING+MAP_HEIGHT) {
            int tileX = (int) Math.floor((x - MAP_SPACING)/TILE_WIDTH);
            int tileY = (int) Math.floor((y - MAP_SPACING)/TILE_HEIGHT);
            return map[tileX][tileY];
        }
        return null;
    }
}