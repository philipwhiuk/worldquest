package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;

import static com.whiuk.philip.worldquest.MapConstants.BORDER_HEIGHT;
import static com.whiuk.philip.worldquest.MapConstants.BORDER_WIDTH;

public class LoadingScreen extends Screen {

    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.drawRect(9,9, BORDER_WIDTH, BORDER_HEIGHT);
        g.drawString("Loading game", 200, 200);
    }

    @Override
    public void onClick(MouseEvent e) {}
}