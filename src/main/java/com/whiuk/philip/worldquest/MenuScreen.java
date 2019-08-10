package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class MenuScreen extends Rectangle implements UI {
    private Stack<UI> visibleUI = new Stack<>();

    MenuScreen(WorldQuest worldQuest) {
        super(0, 0, 420, 420);
        Button newGameButton = new Button(Color.WHITE, Color.BLACK, "New", 100, 100) {
            @Override
            public void handleClick(MouseEvent e) {
                worldQuest.newSaveGame("default", "save");
            }
        };
        Button loadGameButton = new Button(Color.WHITE, Color.BLACK, "Load", 200, 100) {
            @Override
            public void handleClick(MouseEvent e) {
                worldQuest.loadSave("default", "save");
            }
        };
        visibleUI.add(newGameButton);
        visibleUI.add(loadGameButton);
    }

    public void render(Graphics2D g) {
        visibleUI.forEach(ui -> ui.render(g));
    }

    @Override
    public void onClick(MouseEvent e) {
        for (UI ui: visibleUI) {
            ui.onClick(e);
            if (e.isConsumed()) {
                break;
            }
        }
    }
}
