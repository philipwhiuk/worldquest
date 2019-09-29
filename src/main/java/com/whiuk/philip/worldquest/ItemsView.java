package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.ButtonPainter;
import com.whiuk.philip.worldquest.ui.Component;

import java.awt.*;
import java.awt.event.MouseEvent;

class ItemsView extends Component {
    WorldQuest game;

    ItemsView(WorldQuest game) {
        this.game = game;
    }

    @Override
    public void render(Graphics2D g) {
        int offset = y + 13;
        g.setColor(Color.WHITE);
        for (int i = 0; i < game.player.inventory.size(); i++) {
            listItem(g, game.player, game.player.inventory.get(i), offset, i);
            offset += 20;
        }
    }

    private void listItem(Graphics2D g, Player player, Item item, int offset, int index) {
        if (item.canUse()) {
            ButtonPainter.paintButton(g,
                    player.itemBeingUsed != index ? Color.BLUE : Color.DARK_GRAY,
                    player.itemBeingUsed != index ? Color.DARK_GRAY : Color.BLUE,
                    13,
                    offset - 13,
                    "o"
            );
        }

        if (item.hasAction()) {
            ButtonPainter.paintButton(g,
                    Color.GREEN,
                    Color.DARK_GRAY,
                    30,
                    offset - 13,
                    "+"
            );
        }

        ButtonPainter.paintButton(g,
                Color.RED,
                Color.DARK_GRAY,
                47,
                offset - 13,
                "-"
        );

        g.setColor(Color.WHITE);
        g.drawString(item.name, 80, offset);
    }

    @Override
    public void onClick(MouseEvent e) {
        for (int i = 0; i < game.player.inventory.size(); i++) {
            System.out.println(e.getPoint());
            if (inventoryButtonLocation(i, 0).contains(e.getPoint())) {
                game.useItem(i);
                return;
            } else if (inventoryButtonLocation(i, 1).contains(e.getPoint())) {
                game.actionItem(i);
                return;
            } else if (inventoryButtonLocation(i, 2).contains(e.getPoint())) {
                if (game.inShop()) {
                    game.sellItem(i);
                } else {
                    game.dropItem(i);
                }
                return;
            }
        }
    }

    private Rectangle inventoryButtonLocation(int index, int button) {
        int xOffset = button*17;
        int offset = 170+(20*index);
        return new Rectangle(433+xOffset, offset, 15, 15);
    }
}
