package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.Component;
import com.whiuk.philip.worldquest.ui.Tab;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//TODO: Make component
class QuestsView extends Component {
    WorldQuest game;

    QuestsView(WorldQuest game) {
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
        System.out.println(e.getPoint());
        Quest[] questList = game.player.quests.values().toArray(new Quest[]{});
        for (Quest quest : questList) {
            Rectangle questName = new Rectangle(440, 175, 100, 15);
            if (questName.contains(e.getPoint())) {
                if (quest.isFinished()) {
                    game.eventMessage(quest.name + ": COMPLETE");
                } else {
                    QuestStep step = quest.steps.get(quest.stepIndex);
                    if (step.isFinished()) {
                        game.eventMessage(quest.name + ": Step complete");
                    } else {
                        step.killsRequired.forEach((key, value) -> {
                            String killsText = value > 1 ?
                                "Kill "+value+" "+key+"s" :
                                "Kill "+value;
                            game.eventMessage(quest.name + ": "+killsText);
                        });
                    }
                }
                return;
            }
        }
    }

    private void listQuest(Graphics2D g, Player player, Quest quest, int offset, int index) {
        g.setColor(quest.isFinished() ? Color.GREEN : Color.YELLOW);
        g.drawString(quest.name, 20, offset);
    }
}
