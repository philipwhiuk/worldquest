package com.whiuk.philip.worldquest;

import java.awt.*;

abstract class Button extends Rectangle implements UI {
    private Color primaryColor;
    private Color backgroundColor;
    private String symbol;

    Button(Color primaryColor, Color backgroundColor, String symbol, int x, int y) {
        super(x, y, 15, 15);
        this.primaryColor = primaryColor;
        this.backgroundColor = backgroundColor;
        this.symbol = symbol;
    }

    public void render(Graphics2D g) {
        ButtonPainter.paintButton(g, primaryColor, backgroundColor, x, y, symbol);
    }
}