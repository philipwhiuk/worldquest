package com.whiuk.philip.worldquest.ui;

import java.awt.*;

abstract public class ToggleButton extends ClickableUI {

    private final Color primaryColor;
    private final Color secondaryColour;
    private final int x;
    private final int y;
    private final String text;
    private boolean isToggled;

    public ToggleButton(Color primaryColor, Color secondaryColour, int x, int y, String text, boolean isToggled) {
        super(x,y, 45, 20);
        this.primaryColor = primaryColor;
        this.secondaryColour = secondaryColour;
        this.x = x;
        this.y = y;
        this.text = text;
        this.isToggled = isToggled;
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(isToggled ? primaryColor : secondaryColour);
        g.fillRoundRect(x, y, 45, 20, 2, 2);
        g.setColor(isToggled ? secondaryColour : primaryColor);
        g.drawRoundRect(x, y , 45, 20, 2, 2);
        g.drawString(text, x+5, y + 14);
    }

    public void changeToggleState(boolean newState) {
        this.isToggled = newState;
    }
}
