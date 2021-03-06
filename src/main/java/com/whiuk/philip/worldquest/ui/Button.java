package com.whiuk.philip.worldquest.ui;

import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class Button extends ClickableUI {
    private Color primaryColor;
    private Color backgroundColor;
    private String symbol;

    public Button(Color primaryColor, Color backgroundColor, String symbol, int x, int y) {

        super(x, y, symbol.length()* 15, 15);
        this.primaryColor = primaryColor;
        this.backgroundColor = backgroundColor;
        this.symbol = symbol;
    }

    public void render(Graphics2D g) {
        ButtonPainter.paintButton(g, primaryColor, backgroundColor, x, y, symbol);
    }

    public final void onClick(MouseEvent e) {
        if (this.contains(e.getPoint())) {
            e.consume();
            handleClick(e);
        }
    }

    protected abstract void handleClick(MouseEvent e);

}