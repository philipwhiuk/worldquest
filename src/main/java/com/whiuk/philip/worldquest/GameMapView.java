package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.function.Consumer;

import static com.whiuk.philip.worldquest.MapConstants.*;

public class GameMapView extends MapView {

    GameMapView(WorldQuest game, Consumer<Tile> tileClickHandler) {
        super(game, tileClickHandler);
    }

    @Override
    protected boolean showObjects(int x, int y) {
        return game.isVisible(x, y);
    }

    @Override
    public Color determineTileColor(int x, int y) {
        Room playerRoom = game.map[game.player.x][game.player.y].room;
        return game.map[x][y].getColor(game.isVisible(x, y), playerRoom);
    }

    @Override
    void paintNPCs(Graphics2D g) {
        game.visibleNpcs.forEach(npc -> {
            paintCharacter(g, npc);
        });
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