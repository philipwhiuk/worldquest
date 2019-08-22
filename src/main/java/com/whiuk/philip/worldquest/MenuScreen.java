package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.Button;
import com.whiuk.philip.worldquest.ui.ListSelect;
import com.whiuk.philip.worldquest.ui.UI;

import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class MenuScreen extends Rectangle implements Screen {
    private Stack<UI> visibleUI = new Stack<>();
    private String selectedScenario = "default";
    private String selectedSave = "save";

    MenuScreen(WorldQuest worldQuest) {
        super(0, 0, 420, 420);
        Button newGameButton = new Button(Color.WHITE, Color.BLACK, "New", 100, 100) {
            @Override
            public void handleClick(MouseEvent e) {
                worldQuest.newSaveGame(selectedScenario, selectedSave);
            }
        };
        ListSelect scenarioSelector = new ListSelect(
                200, 100,
                Color.WHITE, Color.BLACK, selectedScenario, GameFileUtils.scenarioList(), (s -> s)) {
            @Override
            protected void onSelect(String item) {
                selectedScenario = item;
                this.setSelected(item);
            }
        };
        Button loadGameButton = new Button(Color.WHITE, Color.BLACK, "Load", 100, 200) {
            @Override
            public void handleClick(MouseEvent e) {
                worldQuest.loadSave(selectedSave);
            }
        };
        ListSelect saveSelector = new ListSelect(
                200, 200,
                Color.WHITE, Color.BLACK, selectedSave, GameFileUtils.saveList(), (s -> s)) {
            @Override
            protected void onSelect(String item) {
                selectedSave = item;
                this.setSelected(item);
            }
        };
        visibleUI.add(newGameButton);
        visibleUI.add(loadGameButton);
        visibleUI.add(scenarioSelector);
        visibleUI.add(saveSelector);
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
