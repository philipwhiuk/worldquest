package com.whiuk.philip.worldquest;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class WorldQuestKeyListener implements KeyListener {

    private final WorldQuest game;

    WorldQuestKeyListener(WorldQuest game) {
        this.game = game;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        Action a = game.getKeyMappedAction(e.getKeyChar());
        if (a != null) {
            game.processAction(a);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Action a = game.getKeypressMappedAction(e.getKeyCode());
        if (a != null) {
            game.processAction(a);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
