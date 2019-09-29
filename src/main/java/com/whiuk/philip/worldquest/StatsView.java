package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.Component;
import com.whiuk.philip.worldquest.ui.Tab;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class StatsView extends Component {
    WorldQuest game;

    StatsView(WorldQuest game) {
        super();
        this.game = game;
    }

    @Override
    public void render(Graphics2D g) {
        int offset = y;
        Set<Map.Entry<String, Experience>> statEntries = game.player.stats.entrySet();
        Iterator<Map.Entry<String, Experience>> statI = statEntries.iterator();
        for (int i = 0; i < statEntries.size(); i++) {
            Map.Entry<String, Experience> stat = statI.next();
            Experience experience = stat.getValue();
            String statDescriptor = stat.getKey() + ": " + experience.level + " (" + experience.experience + "xp)";
            g.setColor(Color.WHITE);
            g.drawString(statDescriptor, 30, offset);
            offset += 20;
        }
    }

    @Override
    public void onClick(MouseEvent e) {

    }
}
