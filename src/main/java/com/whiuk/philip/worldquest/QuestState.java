package com.whiuk.philip.worldquest;

class QuestState {

    QuestState(WorldQuest game) {
        this.player = game.player;
        this.scenario = game.scenarioData;
    }

    final Player player;
    final ScenarioData scenario;
}
