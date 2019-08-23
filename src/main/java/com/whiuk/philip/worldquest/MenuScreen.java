package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.Button;
import com.whiuk.philip.worldquest.ui.ClickableUI;
import com.whiuk.philip.worldquest.ui.ListSelect;
import com.whiuk.philip.worldquest.ui.UI;

import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class MenuScreen extends Rectangle implements Screen {
    private Stack<ClickableUI> visibleUI = new Stack<>();
    private String selectedScenario = "default";
    private String selectedSave = "save";

    MenuScreen(WorldQuest worldQuest) {
        super(0, 0, 420, 420);
        Button newGameButton = new Button(Color.WHITE, Color.BLACK, "New Game", 100, 100) {
            @Override
            public void handleClick(MouseEvent e) {
                worldQuest.newSaveGame(selectedScenario);
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
        Button loadGameButton = new Button(Color.WHITE, Color.BLACK, "Load Game", 100, 200) {
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
        Button newScenarioButton = new Button(Color.WHITE, Color.BLACK, "New Scenario", 100, 300) {
            @Override
            public void handleClick(MouseEvent e) {
                worldQuest.newScenario();
            }
        };
        Button editScenarioButton = new Button(Color.WHITE, Color.BLACK, "Edit Scenario", 100, 350) {
            @Override
            public void handleClick(MouseEvent e) {
                worldQuest.editScenario(selectedScenario);
            }
        };

        visibleUI.add(newGameButton);
        visibleUI.add(loadGameButton);
        visibleUI.add(scenarioSelector);
        visibleUI.add(saveSelector);
        visibleUI.add(newScenarioButton);
        visibleUI.add(editScenarioButton);
        visibleUI.add(scenarioSelector);
    }

    public void render(Graphics2D g) {
        visibleUI.forEach(ui -> ui.render(g));
    }

    @Override
    public void onClick(MouseEvent e) {
        for (ClickableUI ui: visibleUI) {
            if (ui.contains(e.getPoint())) {
                ui.onClick(e);
                if (e.isConsumed()) {
                    break;
                }
            }
        }
    }
}
