package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.Sidebar;

import java.awt.*;
import java.awt.event.MouseEvent;

public class EditorSidebar extends Sidebar {

    private final WorldQuest app;

    EditorSidebar(WorldQuest app) {
        this.app = app;
    }

    @Override
    public void render(Graphics2D g) {
        showTileDetails(g);
    }

    private void showTileDetails(Graphics2D g) {
        int yRow = 20;
        Tile tile = app.tileSelected;
        if (tile != null) {
            g.setColor(Color.WHITE);
            g.drawString("Tile", 425, yRow);
            yRow += 20;

            g.setColor(Color.WHITE);
            g.drawString("Type: " + tile.type.name, 450, yRow);
            yRow += 20;

            g.drawString("Room: " + (tile.room != null ? tile.room.name : "<None>"), 450, yRow);
            yRow += 20;

            g.drawString("Objects: ", 450, yRow);
            yRow += 20;
            if (tile.objects.size() > 0) {
                for (int i = 0; i < tile.objects.size(); i++) {
                    GObjects.GameObject object = tile.objects.get(i);
                    g.drawString(object.id(), 475, yRow);
                    yRow += 20;
                }
            } else {
                g.drawString("<None>", 475, yRow);
                yRow += 20;
            }
        }
    }

    @Override
    public void onClick(MouseEvent e) {

    }
}
