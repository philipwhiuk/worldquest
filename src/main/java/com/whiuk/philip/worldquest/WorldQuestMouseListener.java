package com.whiuk.philip.worldquest;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class WorldQuestMouseListener implements MouseListener {

    private final WorldQuest game;

    WorldQuestMouseListener(WorldQuest game) {
        this.game = game;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        Action action = game.checkForActionInterception(e.getPoint());
        if (action != null) {
            game.processAction(action);
            return;
        }
        Tile tile = game.checkForTileInterception(e.getPoint());
        if (tile != null) {
            game.processTileClick(tile);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
