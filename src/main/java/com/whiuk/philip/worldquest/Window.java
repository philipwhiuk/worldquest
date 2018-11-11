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
        closeButton = new Button() {
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
        g.draw(this);
        g.drawString(title, x, y);
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