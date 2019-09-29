package com.whiuk.philip.worldquest.ui;

import java.awt.*;
import java.awt.event.MouseEvent;

public class TabView extends Component {

    private Tab activeTab;
    private Tab[] tabs;
    private ToggleButton[] tabButtons;

    public TabView(Tab[] tabs, int x, int y, int maxX) {
        this.tabs = tabs;
        this.tabButtons = new ToggleButton[tabs.length];
        int currentX = x;
        int currentY = y;
        for (int i = 0; i < tabs.length; i++ ) {
            final int index = i;
            tabButtons[i] = new ToggleButton(
                    Color.GREEN, Color.BLACK, currentX, currentY,
                    tabs[i].tabName, false) {
                @Override
                public void onClick(MouseEvent e) {
                    setActiveTab(index);
                }
            };
            if (currentX < maxX) {
                currentX += 50;
            } else {
                currentY += 25;
                currentX = x;
            }
        }
    }

    public void setActiveTab(int activeTab) {
        this.activeTab = tabs[activeTab];
        for(int i = 0; i < tabButtons.length; i++) {
            tabButtons[i].changeToggleState(i == activeTab);
        }
    }

    @Override
    public void onClick(MouseEvent e) {
        for (ToggleButton button : tabButtons) {
            if (button.contains(e.getPoint())) {
                button.onClick(e);
                return;
            }
        }
        if (activeTab.contains(e.getPoint())) {
            activeTab.onClick(e);
        }
    }

    @Override
    public void render(Graphics2D g) {
        for (ToggleButton button : tabButtons) {
            button.render(g);
        }
        activeTab.render(g);
    }
}
