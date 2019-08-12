package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;

class NPCsTab extends SidebarTab {
    WorldQuest game;

    NPCsTab(WorldQuest game) {
        this.game = game;
    }

    @Override
    public void render(Graphics2D g) {
        int offset = y;
        g.drawString("NPCs", 425, offset);

        offset += 20;

        for (NPC npc : game.visibleNpcs) {
            if (npc.canTalk()) {
                ButtonPainter.paintButton(g,
                        npc.currentConversation != null ? Color.DARK_GRAY : Color.BLUE,
                        npc.currentConversation != null ? Color.BLUE : Color.DARK_GRAY,
                        433,
                        offset-13,
                        "o"
                );
            }
            String npcHealth = npc.canFight() ? ": " + npc.health + "/" + npc.type.health : "";
            String npcInfo = npc.type.name + npcHealth;
            g.setColor(Color.WHITE);
            g.drawString(npcInfo, 500, offset);
            offset += 20;
        }
    }

    @Override
    public void onClick(MouseEvent e) {
        for (int i = 0; i < game.visibleNpcs.size(); i++) {
            if (npcButtonTalkLocation(i).contains(e.getPoint())) {
                game.talkTo(i);
            }
        }
    }

    private Rectangle npcButtonTalkLocation(int index) {
        int offset = 20*index;
        return new Rectangle(433,155+offset, 15, 15);
    }
}
