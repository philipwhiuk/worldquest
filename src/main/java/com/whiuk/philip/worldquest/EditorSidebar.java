package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.ListSelect;
import com.whiuk.philip.worldquest.ui.Sidebar;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

public class EditorSidebar extends Sidebar implements TileSelectionListener {

    private final WorldQuest app;
    private final ListSelect<Integer> tileTypeSelector;
    private Tile selectedTile;

    EditorSidebar(WorldQuest app) {
        this.app = app;
        app.registerTileSelectionListener(this);
        tileTypeSelector = new ListSelect<Integer>(
                490, 18,
                Color.WHITE, Color.BLACK, MapTileLoader.DEFAULT_TILE_ID,
                new ArrayList<>(app.scenarioData.tileTypes.keySet()),
                (id -> app.scenarioData.tileTypes.get(id).name)) {
            @Override
            protected void onSelect(Integer item) {
                selectTileType(item);
                this.setSelected(item);
            }
        };
    }

    @Override
    public void tileSelected(Tile tile) {
        this.selectedTile = tile;
        tileTypeSelector.setSelected(tile.type.id);
    }

    private void selectTileType(Integer item) {
        app.changeTileType(selectedTile, app.scenarioData.tileTypes.get(item));
    }

    @Override
    public void render(Graphics2D g) {
        showTileDetails(g);
    }

    private void showTileDetails(Graphics2D g) {
        int yRow = 20;
        Tile tile = selectedTile;
        if (tile != null) {
            g.setColor(Color.WHITE);
            g.drawString("Tile", 425, yRow);
            yRow += 20;

            g.setColor(Color.WHITE);
            g.drawString("Type:", 450, yRow);
            tileTypeSelector.render(g);

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
        if (tileTypeSelector.contains(e.getPoint())) {
            tileTypeSelector.onClick(e);
        }
    }
}
