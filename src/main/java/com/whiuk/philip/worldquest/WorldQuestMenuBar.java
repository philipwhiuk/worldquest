package com.whiuk.philip.worldquest;

import javax.swing.*;
import java.awt.*;

import static com.whiuk.philip.worldquest.WorldQuest.LICENSE_TEXT;

class WorldQuestMenuBar extends JMenuBar {
    WorldQuestMenuBar(Component parent) {
        JMenu menu = new JMenu("WorldQuest");
        JMenuItem licenseItem = new JMenuItem("License");
        licenseItem.addActionListener(e -> JOptionPane.showMessageDialog(
                parent,
                LICENSE_TEXT));
        menu.add(licenseItem);
        add(menu);
    }
}