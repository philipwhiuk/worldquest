package com.whiuk.philip.worldquest;

import java.awt.*;

public class ButtonPainter {
    public static void paintButton(Graphics2D g, Color primaryColor, Color backgroundColor, int x, int y, String symbol) {
        g.setColor(backgroundColor);
        g.fillRoundRect(x, y, 15, 14, 2, 2);
        g.setColor(primaryColor);
        g.drawRoundRect(x, y , 15, 14, 2, 2);
        g.drawString(symbol, x+4, y + 10);
    }

    public static void paintTextButton(
            Graphics2D g, Color primaryColor,
            Color backgroundColor, int x, int y, String text, boolean toggle) {

    }
}
