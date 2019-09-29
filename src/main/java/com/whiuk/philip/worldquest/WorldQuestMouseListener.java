package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.ClickableUI;
import com.whiuk.philip.worldquest.ui.UI;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class WorldQuestMouseListener implements MouseListener {

    private final WorldQuest game;
    private final ClickableUI ui;

    WorldQuestMouseListener(WorldQuest game, ClickableUI ui) {
        this.game = game;
        this.ui = ui;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        ui.onClick(e);
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
