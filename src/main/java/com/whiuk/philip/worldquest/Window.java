package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;

abstract class Window extends Rectangle implements UI {
    private final Button closeButton;
    private final String title;
    private Runnable onCloseListener = null;

    Window(int x, int y, int width, int height, String title) {
        super(x, y, width, height);
        this.title = title;
        closeButton = new Button(Color.ORANGE, Color.BLACK, "x", x+width-30, y+10) {
            @Override
            public void onClick(MouseEvent e) {
                Window.this.close();
            }
        };
    }

    public void onClick(MouseEvent e) {
        if (closeButton.contains(e.getPoint())) {
            closeButton.onClick(e);
        }
    }

    public void render(Graphics2D g) {
        g.setColor(Color.ORANGE);
        g.draw(this);
        g.draw(new Rectangle(x,y, width, 40));
        g.setColor(Color.WHITE);
        g.drawString(title,x+25, y+25);
        closeButton.render(g);
        renderWindowFrame(g);
    }

    abstract void renderWindowFrame(Graphics2D g);

    void setOnClose(Runnable onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    private void close() {
        if (onCloseListener != null) {
            onCloseListener.run();
        }
    }
}