package com.whiuk.philip.worldquest;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WorldQuestWindowAdapter extends WindowAdapter {
    private final WorldQuest game;

    WorldQuestWindowAdapter(WorldQuest game) {
        this.game = game;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        if (game.isGameRunning()) {
            String[] options = {"Save", "Quit", "Cancel"};
            int option = JOptionPane.showOptionDialog(
                    game,
                    "Save before quitting?",
                    "Save?",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]);
            switch (option) {
                case 0:
                    game.saveGame();
                    System.exit(0);
                case 1:
                    System.exit(0);
                case 2:
            }
        } else {
            System.exit(0);
        }
    }
}
