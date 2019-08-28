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
        Tile tile = app.tileSelected;
        if (tile != null) {
            g.setColor(Color.WHITE);
            g.drawString("Tile", 425, 20);

            g.setColor(Color.WHITE);
            g.drawString("Type: " + tile.type.name, 450, 40);
        }
    }

    @Override
    public void onClick(MouseEvent e) {

    }
}
