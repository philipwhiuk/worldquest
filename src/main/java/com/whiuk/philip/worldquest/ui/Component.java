package com.whiuk.philip.worldquest.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Component extends ClickableUI implements FocusableUI {

    protected final List<UI> children = new ArrayList<>();
    protected boolean focused = false;

    public Component() {
        super();
    }

    public Component(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void render(Graphics2D g) {
        ArrayList<UI> renderList = new ArrayList<>(children);
        Collections.reverse(renderList);
        renderList.forEach(ui -> ui.render(g));
    }

    @Override
    public void onClick(MouseEvent e) {
        for (UI ui: children) {
            if (ui instanceof ClickableUI) {
                ClickableUI clickableUI = (ClickableUI) ui;
                if (clickableUI.contains(e.getPoint())) {
                    clickableUI.onClick(e);
                    if (e.isConsumed()) {
                        break;
                    }
                } else if (ui instanceof FocusableUI) {
                    FocusableUI fUI = (FocusableUI) ui;
                    if (fUI.hasFocus()) {
                        fUI.loseFocus();
                    }
                }
            }
        }
    }

    @Override
    public boolean hasFocus() {
        if (focused) {
            return true;
        }

        for (UI ui: children) {
            if (ui instanceof FocusableUI) {
                FocusableUI fUI = (FocusableUI) ui;
                if (fUI.hasFocus()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void loseFocus() {
        focused = false;
        for (UI ui: children) {
            if (ui instanceof FocusableUI) {
                FocusableUI fUI = (FocusableUI) ui;
                if (fUI.hasFocus()) {
                    fUI.loseFocus();
                }
            }
        }
    }
}
