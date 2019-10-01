package com.whiuk.philip.worldquest.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class ListSelect<T> extends ClickableUI implements FocusableUI {
    private final Color primary;
    private final Color secondary;
    private T selectedItem;
    private final List<T> items;
    private final List<Rectangle> itemBounds;
    private final Function<T, String> renderFunction;
    private final int x;
    private final int y;
    private boolean showOptions;

    public ListSelect(
            int x,
            int y,
            Color primary,
            Color secondary,
            T selectedItem,
            List<T> items,
            Function<T, String> renderFunction) {
        super(x,y, 100, items.size()*20);
        this.x = x;
        this.y = y;
        this.primary = primary;
        this.secondary = secondary;
        this.selectedItem = selectedItem;
        this.items = items;
        itemBounds = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            itemBounds.add(new Rectangle(x, y+(i*20), 100, 20));
        }
        this.renderFunction = renderFunction;
    }

    @Override
    public void render(Graphics2D g) {
        if (showOptions) {
            for (int i = 0; i < items.size(); i++) {
                T item = items.get(i);
                boolean itemSelected = isSelected(item);
                if (itemSelected) {
                    g.setColor(primary);
                    g.fill(itemBounds.get(i));
                } else {
                    g.setColor(secondary);
                    g.fill(itemBounds.get(i));
                }
                g.setColor(itemSelected ? secondary : primary);
                String title = renderFunction.apply(item);
                g.drawString(title, x + 5, y + 15 + (i * 20));
            }
            g.setColor(primary);
            g.draw(this);
        } else if (itemBounds.size() >= 1) {
            g.setColor(primary);
            g.draw(itemBounds.get(0));
            g.setColor(primary);
            String title = renderFunction.apply(selectedItem);
            g.drawString(title, x+5, y+15);
        } else {
            g.draw(new Rectangle(x, y, 100, 20));
            g.drawString(" - None -", x+5, y+15);
        }
    }

    @Override
    public void onClick(MouseEvent e) {
        if (!showOptions) {
            if (itemBounds.get(0).contains(e.getPoint())) {
                showOptions = true;
                e.consume();
            }
        } else {
            showOptions = false;
            for (int i = 0; i < itemBounds.size(); i++) {
                if (itemBounds.get(i).contains(e.getPoint())) {
                    onSelect(items.get(i));
                    e.consume();
                }
            }
        }
    }

    protected abstract void onSelect(T item);

    private boolean isSelected(T item) {
        return item.equals(selectedItem);
    }

    public void setSelected(T item) {
        this.selectedItem = item;
    }

    @Override
    public boolean hasFocus() {
        return showOptions;
    }

    @Override
    public void loseFocus() {
        showOptions = false;
    }
}
