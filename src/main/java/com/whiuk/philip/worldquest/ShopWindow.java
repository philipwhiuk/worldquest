package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.ButtonPainter;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

class ShopWindow extends com.whiuk.philip.worldquest.ui.Window {
    private Shop shop;
    private WorldQuest game;

    ShopWindow(WorldQuest game, Shop shop) {
        super(10, 10, 400, 200, shop.name);
        this.game = game;
        this.shop = shop;
    }

    @Override
    public void renderWindowFrame(Graphics2D g) {
        paintShopItems(g, shop.items);
    }

    @Override
    public void onContentClick(MouseEvent e) {
        for (int i = 0; i < shop.items.size(); i++) {
            if (shopButton(i).contains(e.getPoint())) {
                game.buyItem(shop, i);
            }
        }
    }

    private Rectangle shopButton(int i) {
        return new Rectangle(250, (i*20)+88, 15, 15);
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

    @Override
    protected void handleClose() {
        game.closeShop();
    }
}
