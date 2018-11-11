package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Stack;

class GameScreen extends Rectangle implements UI {
    private Stack<UI> visibleUI = new Stack<>();

    GameScreen(UI baseLayer) {
        super(0, 0, 420, 420);
        visibleUI.push(baseLayer);
    }

    public void render(Graphics2D g) {
        visibleUI.peek().render(g);
    }

    @Override
    public void onClick(MouseEvent e) {
        visibleUI.peek().onClick(e);
    }

    public void showWindow(Window window) {
        window.setOnClose(() -> visibleUI.remove(window));
        visibleUI.push(window);
    }
}