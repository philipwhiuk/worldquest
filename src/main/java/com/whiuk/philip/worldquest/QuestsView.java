package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.Component;

import java.awt.*;
import java.awt.event.MouseEvent;
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
        Set<Map.Entry<String, QuestProgress>> questList = game.player.quests.entrySet();
        Iterator<Map.Entry<String, QuestProgress>> questIterator = questList.iterator();
        for (int i = 0; i < questList.size(); i++) {
            QuestProgress questProgress = questIterator.next().getValue();
            listQuest(g, game.player, questProgress, offset, i);
            offset += 20;
        }
    }

    @Override
    public void onClick(MouseEvent e) {
        System.out.println(e.getPoint());

        Set<Map.Entry<String, QuestProgress>> questList = game.player.quests.entrySet();
        Iterator<Map.Entry<String, QuestProgress>> questIterator = questList.iterator();
        for (int i = 0; i < questList.size(); i++) {
            QuestProgress questProgress = questIterator.next().getValue();
            Rectangle questName = new Rectangle(440, 175, 100, 15);
            if (questName.contains(e.getPoint())) {
                if (questProgress.isFinished()) {
                    game.eventMessage(questProgress.quest.name + ": COMPLETE");
                } else {
                    QuestStepProgress step = questProgress.currentQuestStepProgress;
                    if (step.isFinished()) {
                        game.eventMessage(questProgress.quest.name + ": Step complete");
                    } else {
                        questProgress.currentQuestStepProgress.killsRemaining.forEach((key, value) -> {
                            String killsText = value > 1 ?
                                "Kill "+value+" "+key+"s" :
                                "Kill "+value;
                            game.eventMessage(questProgress.quest.name + ": "+killsText);
                        });
                    }
                }
                return;
            }
        }
    }

    private void listQuest(Graphics2D g, Player player, QuestProgress questProgress, int offset, int index) {
        g.setColor(questProgress.isFinished() ? Color.GREEN : Color.YELLOW);
        g.drawString(questProgress.quest.name, 20, offset);
    }
}
