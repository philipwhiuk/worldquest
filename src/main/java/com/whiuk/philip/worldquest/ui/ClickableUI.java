package com.whiuk.philip.worldquest.ui;

import java.awt.*;
import java.awt.event.MouseEvent;

abstract public class ClickableUI extends Rectangle implements UI {
    public ClickableUI() {
        super();
    }
    public ClickableUI(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    public abstract void onClick(MouseEvent e);
}
