package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class DeathScreen extends Screen {

    private final List<String> eventHistory;

    DeathScreen(List<String> eventHistory) {
        this.eventHistory = eventHistory;
    }

    @Override
    public void render(Graphics2D g) {
        paintEventHistory(g);
    }

    private void paintEventHistory(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(40, 40, 560, 400);
        g.setColor(Color.WHITE);
        g.drawRect(40, 40, 560, 400);
        g.drawString("Events", 60, 60);
        for (int i = 0; i < eventHistory.size() && i < 15; i++) {
            g.drawString(eventHistory.get(i), 80, 80+(i*20));
        }
        g.drawString("New Game? Y/N", 60, 400);
    }

    @Override
    public void onClick(MouseEvent e) {

    }
}
