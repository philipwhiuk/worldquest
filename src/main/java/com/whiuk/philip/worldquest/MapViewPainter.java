package com.whiuk.philip.worldquest;

import java.awt.Graphics2D;
import java.awt.Color;
import java.util.List;

import static com.whiuk.philip.worldquest.MapConstants.*;
import static com.whiuk.philip.worldquest.MapConstants.TILE_HEIGHT;

public class MapViewPainter {
    public static void paintMapView(Graphics2D g, WorldQuest game, Tile[][] map, List<NPC> visibleNpcs, Player player) {
        paintMap(g, game, map);
        paintNPCs(g, game, visibleNpcs);
        paintPlayer(g, player);
    }

    private static void paintMap(Graphics2D g, WorldQuest game, Tile[][] map) {
        Room playerRoom = map[game.player.x][game.player.y].room;
        g.setColor(Color.WHITE);
        g.drawRect(9,9, BORDER_WIDTH, BORDER_HEIGHT);
        if (map != null) {
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    if (map[x][y] != null) {
                        g.setColor(map[x][y].getColor(game.isVisible(x, y), playerRoom));
                        g.fillRect(
                                MAP_SPACING + (x * TILE_WIDTH),
                                MAP_SPACING + (y * TILE_HEIGHT),
                                TILE_WIDTH, TILE_HEIGHT);
                        if (game.isVisible(x, y)) {
                            for (GObjects.GameObject object : map[x][y].objects) {
                                object.draw(g, x, y);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void paintNPCs(Graphics2D g, WorldQuest game, List<NPC> visibleNpcs) {
        visibleNpcs.forEach(npc -> {
            paintCharacter(g, npc);
        });
    }

    private static void paintPlayer(Graphics2D g, Player player) {
        paintCharacter(g, player);
    }

    private static void paintCharacter(Graphics2D g, GameCharacter c) {
        if (c != null) {
            g.setColor(Color.BLACK);
            g.drawOval(MAP_SPACING + (c.x * TILE_WIDTH), MAP_SPACING + (c.y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT);
            g.setColor(c.color);
            g.fillOval(MAP_SPACING + (c.x * TILE_WIDTH), MAP_SPACING + (c.y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT);
        }
    }
}
