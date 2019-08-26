package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.function.Consumer;

import static com.whiuk.philip.worldquest.MapConstants.*;

public class EditorMapView extends MapView {

    EditorMapView(WorldQuest game, Consumer<Tile> tileClickHandler) {
        super(game, tileClickHandler);
    }

    @Override
    protected boolean showObjects(int x, int y) {
        return true;
    }

    @Override
    public Color determineTileColor(int x, int y) {
        return game.map[x][y].type.color;
    }

    @Override
    void paintNPCs(Graphics2D g) {
        game.npcs.forEach(npc -> paintCharacter(g, npc));
    }

    private void paintCharacter(Graphics2D g, GameCharacter c) {
        if (c != null) {
            g.setColor(Color.BLACK);
            g.drawOval(MAP_SPACING + (c.x * TILE_WIDTH), MAP_SPACING + (c.y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT);
            g.setColor(c.color);
            g.fillOval(MAP_SPACING + (c.x * TILE_WIDTH), MAP_SPACING + (c.y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT);
        }
    }
}