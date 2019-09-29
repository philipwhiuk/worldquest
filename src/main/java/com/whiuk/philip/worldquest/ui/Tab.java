package com.whiuk.philip.worldquest.ui;

import java.awt.*;
import java.awt.event.MouseEvent;

public class Tab extends Component {
    public final String tabName;
    private final Component child;

    public Tab(String tabName, int x, int y, int width, int height, Component child) {
        super(x,y,width,height);
        this.tabName = tabName;
        this.child = child;
        this.children.add(child);
    }

    @Override public void render(Graphics2D g) {
        g.translate(x,y);
        child.render(g);
        g.translate(-x,-y);
    }

    @Override public void onClick(MouseEvent e) {
        child.onClick(e);
    }
}
