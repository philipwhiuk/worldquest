package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.ClickableUI;
import com.whiuk.philip.worldquest.ui.UI;
import com.whiuk.philip.worldquest.ui.Window;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Stack;

class EditorScreen extends Screen {
    private final Rectangle mainViewSize;
    private final EditorSidebar sidebar;
    private final WorldQuest.MessageDisplay messageDisplay;
    private Stack<ClickableUI> mainViewStack = new Stack<>();

    EditorScreen(ClickableUI initialView, EditorSidebar sidebar, WorldQuest.MessageDisplay messageDisplay) {
        mainViewSize = new Rectangle(0, 0, 420, 420);
        mainViewStack.push(initialView);
        this.sidebar = sidebar;
        this.messageDisplay = messageDisplay;
    }

    public void render(Graphics2D g) {
        mainViewStack.peek().render(g);
        sidebar.render(g);
        messageDisplay.render(g);
    }

    @Override
    public void onClick(MouseEvent e) {
        if (mainViewSize.contains(e.getPoint())) {
            mainViewStack.peek().onClick(e);
        } else if (sidebar.contains(e.getPoint())) {
            sidebar.onClick(e);
        } else if (messageDisplay.contains(e.getPoint())) {
            messageDisplay.onClick(e);
        }
    }

    public void showWindow(Window window) {
        window.setOnClose(() -> mainViewStack.remove(window));
        mainViewStack.push(window);
    }
}