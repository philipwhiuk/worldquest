package com.whiuk.philip.worldquest;

import java.awt.*;

abstract class Button extends Rectangle implements UI {
    private Color primaryColor;
    private Color backgroundColor;
    private String symbol;

    public void render(Graphics2D g) {
        ButtonPainter.paintButton(g, primaryColor, backgroundColor, x, y, symbol);
    }
}