package com.whiuk.philip.worldquest.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class ListSelect implements UI {
    private final Color primary;
    private final Color secondary;
    private String selectedItem;
    private final List<String> items;
    private final List<Rectangle> itemBounds;
    private final Function<String, String> renderFunction;
    private final int x;
    private final int y;

    public ListSelect(
            int x,
            int y,
            Color primary,
            Color secondary,
            String selectedItem,
            List<String> items,
            Function<String, String> renderFunction) {
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
        g.setColor(primary);
        g.drawRect(x,y, 100, items.size()*20);
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            boolean itemSelected = isSelected(item);
            if (itemSelected) {
                g.setColor(primary);
                g.fill(itemBounds.get(i));
            }
            g.setColor(itemSelected ? secondary : primary);
            String title = renderFunction.apply(item);
            g.drawString(title, x+5, y+15+(i*20));
        }
    }

    @Override
    public void onClick(MouseEvent e) {
        for (int i = 0; i < itemBounds.size(); i++) {
            if (itemBounds.get(i).contains(e.getPoint())) {
                onSelect(items.get(i));
                e.consume();
            }
        }
    }

    protected abstract void onSelect(String item);

    private boolean isSelected(String item) {
        return item.equals(selectedItem);
    }

    public void setSelected(String item) {
        this.selectedItem = item;
    }
}
