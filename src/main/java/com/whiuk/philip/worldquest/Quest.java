package com.whiuk.philip.worldquest;

import java.util.Map;

public class Quest {
    private boolean finished;
    String name;
    Map<String, Integer> killsRequired;

    Quest(String name, Map<String, Integer> killsRequired, boolean finished) {
        this.name = name;
        this.killsRequired = killsRequired;
        this.finished = finished;
    }

    void finish(Player player) {
        this.finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public void npcDeath(String name) {
        if (killsRequired.containsKey(name)) {
            int kills = killsRequired.get(name);
            if (kills == 1) {
                killsRequired.remove(name);
            } else {
                killsRequired.put(name, kills -1);
            }
        }
    }

    public boolean isQuestComplete() {
        return killsRequired.isEmpty();
    }

    public Quest start() {
        return new Quest(name, killsRequired, false);
    }
}
