package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

class ShopWindow extends Window {
    private Shop shop;

    ShopWindow(Shop shop) {
        super(10, 10, 400, 200, shop.name);
        this.shop = shop;
    }

    @Override
    public void renderWindowFrame(Graphics2D g) {
        paintShopItems(g, shop.items);
    }

    @Override
    public void onClick(MouseEvent e) {
        System.out.println("Click in shop");
        super.onClick(e);
    }

    private void paintShopItems(Graphics2D g, List<ShopListing> items) {
        for (int i = 0; i < items.size(); i++) {
            ShopListing listing = items.get(i);
            String text = listing.item.name
                    + " ("+listing.quantity+"/"+listing.maxQuantity+")"
                    + "         " + listing.getPrice()+" coins";
            g.setColor(Color.WHITE);
            g.drawString(text, 40, 100+(i*20));

            ButtonPainter.paintButton(g, Color.GREEN, Color.BLACK, 250, (i*20)+88, "+");
        }
    }
}
