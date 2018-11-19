package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class QuestsTab extends SidebarTab {
    WorldQuest game;

    QuestsTab(WorldQuest game) {
        this.game = game;
    }

    @Override
    public void render(Graphics2D g) {
        int offset = y + 13;
        g.setColor(Color.WHITE);
        Set<Map.Entry<String, Quest>> questList = game.player.quests.entrySet();
        Iterator<Map.Entry<String, Quest>> questIterator = questList.iterator();
        for (int i = 0; i < questList.size(); i++) {
            Quest quest = questIterator.next().getValue();
            listQuest(g, game.player, quest, offset, i);
            offset += 20;
        }
    }

    @Override
    public void onClick(MouseEvent e) {
    }

    private void listQuest(Graphics2D g, Player player, Quest quest, int offset, int index) {
        g.setColor(quest.isFinished() ? Color.GREEN : Color.YELLOW);
        g.drawString(quest.name, 450, offset);
    }
}
