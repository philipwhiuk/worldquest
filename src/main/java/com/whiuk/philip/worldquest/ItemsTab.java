package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;

class ItemsTab extends SidebarTab {
    WorldQuest game;

    ItemsTab(WorldQuest game) {
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
                    433,
                    offset - 13,
                    "o"
            );
        }

        if (item.canEquip()) {
            ButtonPainter.paintButton(g,
                    Color.GREEN,
                    Color.DARK_GRAY,
                    450,
                    offset - 13,
                    "+"
            );
        }

        ButtonPainter.paintButton(g,
                Color.RED,
                Color.DARK_GRAY,
                467,
                offset - 13,
                "-"
        );

        g.setColor(Color.WHITE);
        g.drawString(item.name, 500, offset);
    }

    @Override
    public void onClick(MouseEvent e) {
        for (int i = 0; i < game.player.inventory.size(); i++) {
            if (inventoryButtonLocation(i, 0).contains(e.getPoint())) {
                boolean actionPerformed = game.useItem(i);
                if (!actionPerformed) {
                    game.eventMessage("Nothing happens");
                }
                return;
            } else if (inventoryButtonLocation(i, 1).contains(e.getPoint())) {
                game.equipItem(i);
                return;
            } else if (inventoryButtonLocation(i, 2).contains(e.getPoint())) {
                game.dropItem(i);
                return;
            }
        }
    }

    private Rectangle inventoryButtonLocation(int index, int button) {
        int xOffset = button*17;
        int offset = y+(20*index);
        return new Rectangle(433+xOffset, offset, 15, 15);
    }
}
