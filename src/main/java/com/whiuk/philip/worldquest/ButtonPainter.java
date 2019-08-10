package com.whiuk.philip.worldquest;

import java.awt.*;

public class ButtonPainter {
    public static void paintButton(Graphics2D g, Color primaryColor, Color backgroundColor, int x, int y, String symbol) {
        g.setColor(backgroundColor);
        g.fillRoundRect(x, y, (symbol.length()*8)+9, 15, 2, 2);
        g.setColor(primaryColor);
        g.drawRoundRect(x, y , (symbol.length()*8)+9, 15, 2, 2);
        g.drawString(symbol, x+4, y + 12);
    }

    public static void paintToggleButton(
            Graphics2D g, Color primaryColor,
            Color backgroundColor, int x, int y, String text, boolean toggle) {

    }
}
