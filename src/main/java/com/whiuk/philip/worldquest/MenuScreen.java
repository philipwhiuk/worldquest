package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.*;

import java.awt.Color;
import java.awt.event.MouseEvent;

public class MenuScreen extends Screen {
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
        ListSelect<String> scenarioSelector = new ListSelect<String>(
                200, 100,
                Color.WHITE, Color.BLACK, selectedScenario, GameFileUtils.scenarioList(), (s -> s)) {
            @Override
            protected void onSelect(String item) {
                selectedScenario = item;
                this.setSelected(item);
            }
        };
        Button loadGameButton = new Button(Color.WHITE, Color.BLACK, "Load Game", 100, 130) {
            @Override
            public void handleClick(MouseEvent e) {
                worldQuest.loadSave(selectedSave);
            }
        };
        ListSelect<String> saveSelector = new ListSelect<String>(
                200, 130,
                Color.WHITE, Color.BLACK, selectedSave, GameFileUtils.saveList(), (s -> s)) {
            @Override
            protected void onSelect(String item) {
                selectedSave = item;
                this.setSelected(item);
            }
        };
        Button newScenarioButton = new Button(Color.WHITE, Color.BLACK, "New Scenario", 100, 160) {
            @Override
            public void handleClick(MouseEvent e) {
                worldQuest.newScenario();
            }
        };
        Button editScenarioButton = new Button(Color.WHITE, Color.BLACK, "Edit Scenario", 100, 190) {
            @Override
            public void handleClick(MouseEvent e) {
                worldQuest.editScenario(selectedScenario);
            }
        };

        children.add(newGameButton);
        children.add(loadGameButton);
        children.add(scenarioSelector);
        children.add(saveSelector);
        children.add(newScenarioButton);
        children.add(editScenarioButton);
        children.add(scenarioSelector);
    }
}
