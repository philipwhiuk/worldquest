package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.Component;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class SkillsView extends Component {
    WorldQuest game;

    SkillsView(WorldQuest game) {
        this.game = game;
    }

    @Override
    public void render(Graphics2D g) {
        int offset = y;
        Set<Map.Entry<String, Experience>> skillEntries = game.player.skills.entrySet();
        Iterator<Map.Entry<String, Experience>> skillI = skillEntries.iterator();
        for (int i = 0; i < skillEntries.size(); i++) {
            Map.Entry<String, Experience> skill = skillI.next();
            Experience experience = skill.getValue();
            String skillDescriptor = skill.getKey() + ": " + experience.level + " (" + experience.experience + "xp)";
            g.setColor(Color.WHITE);
            g.drawString(skillDescriptor, 30, offset);
            offset += 20;
        }
    }

    @Override
    public void onClick(MouseEvent e) {

    }
}
