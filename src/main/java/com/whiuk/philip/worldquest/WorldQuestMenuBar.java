package com.whiuk.philip.worldquest;

import javax.swing.*;
import java.awt.*;

import static com.whiuk.philip.worldquest.WorldQuest.LICENSE_TEXT;

class WorldQuestMenuBar extends JMenuBar {
    private Component parent;

    WorldQuestMenuBar(Component parent) {
        this.parent = parent;
        showStartScreenItems();
    }

    private void showStartScreenItems() {
        removeAll();
        JMenu appMenu = buildAppMenu();
        add(appMenu);
    }

    private JMenu buildAppMenu() {
        JMenu appMenu = new JMenu("WorldQuest");
        JMenuItem licenseItem = new JMenuItem("License");
        licenseItem.addActionListener(e -> JOptionPane.showMessageDialog(
                parent,
                LICENSE_TEXT));
        appMenu.add(licenseItem);
        return appMenu;
    }

    void showGameScreenItems(WorldQuest game) {
        removeAll();
        JMenu appMenu = buildAppMenu();
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveGame = new JMenuItem("Save Game");
        saveGame.addActionListener(e -> game.saveGame());
        fileMenu.add(saveGame);
        add(appMenu);
        add(fileMenu);
        invalidate();
    }

    void showEditorScreenItems(WorldQuest editor) {
        removeAll();
        JMenu appMenu = buildAppMenu();

        JMenu fileMenu = new JMenu("File");
        JMenuItem saveScenario = new JMenuItem("Save Scenario");
        saveScenario.addActionListener(e -> editor.saveScenario());
        fileMenu.add(saveScenario);

        JMenu scenarioMenu = new JMenu("Scenario");
        JMenuItem itemManager = new JMenuItem("Item Manager");
        itemManager.addActionListener(e -> editor.showItemManager());
        scenarioMenu.add(itemManager);

        add(appMenu);
        add(fileMenu);
        invalidate();
        repaint();
    }
}