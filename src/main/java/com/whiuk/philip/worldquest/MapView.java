package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.ClickableUI;
import com.whiuk.philip.worldquest.ui.UI;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import static com.whiuk.philip.worldquest.MapConstants.*;
import static com.whiuk.philip.worldquest.MapConstants.MAP_SPACING;
import static com.whiuk.philip.worldquest.MapConstants.TILE_HEIGHT;

public abstract class MapView extends ClickableUI {

    protected WorldQuest game;
    private Consumer<Tile> tileClickHandler;

    MapView(WorldQuest game, Consumer<Tile> tileClickHandler) {
        this.game = game;
        this.tileClickHandler = tileClickHandler;
    }

    @Override
    public void render(Graphics2D g) {
        paintMap(g);
        paintNPCs(g);
        paintPlayer(g);
    }

    private void paintMap(Graphics2D g) {
        Tile[][] map = game.map;
        g.setColor(Color.WHITE);
        g.drawRect(9,9, BORDER_WIDTH, BORDER_HEIGHT);
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                if (map[x][y] != null) {
                    g.setColor(determineTileColor(x,y));
                    g.fillRect(
                            MAP_SPACING + (x * TILE_WIDTH),
                            MAP_SPACING + (y * TILE_HEIGHT),
                            TILE_WIDTH, TILE_HEIGHT);
                    if (showObjects(x, y)) {
                        for (GObjects.GameObject object : map[x][y].objects) {
                            object.draw(g, x, y);
                        }
                    }
                }
            }
        }
    }

    protected abstract boolean showObjects(int x, int y);


    abstract Color determineTileColor(int x, int y);

    abstract void paintNPCs(Graphics2D g);

    private void paintPlayer(Graphics2D g) {
        paintCharacter(g, game.player);
    }

    private void paintCharacter(Graphics2D g, GameCharacter c) {
        if (c != null) {
            g.setColor(Color.BLACK);
            g.drawOval(MAP_SPACING + (c.x * TILE_WIDTH), MAP_SPACING + (c.y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT);
            g.setColor(c.color);
            g.fillOval(MAP_SPACING + (c.x * TILE_WIDTH), MAP_SPACING + (c.y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT);
        }
    }

    @Override
    public void onClick(MouseEvent e) {
        Tile t = checkForTileInterception(e.getPoint());
        if (t != null) {
            tileClickHandler.accept(t);
        }
    }

    Tile checkForTileInterception(Point p) {
        double x = p.getX();
        double y = p.getY();
        if (x > MAP_SPACING && x < MAP_SPACING+MAP_WIDTH & y > MAP_SPACING & y < MAP_SPACING+MAP_HEIGHT) {
            int tileX = (int) Math.floor((x - MAP_SPACING)/TILE_WIDTH);
            int tileY = (int) Math.floor((y - MAP_SPACING)/TILE_HEIGHT);
            return game.map[tileX][tileY];
        }
        return null;
    }
}