package com.whiuk.philip.worldquest;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

class ShopWindow extends Window {
    private Shop shop;

    ShopWindow(Shop shop) {
        super(0, 0, 0, 0, shop.name);
        this.shop = shop;
    }

    @Override
    public void renderWindowFrame(Graphics2D g) {
        ShopPainter.paintShop(g, shop);
    }

    @Override
    public void onClick(MouseEvent e) {
    }
}
