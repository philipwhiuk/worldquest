package com.whiuk.philip.worldquest;

import java.awt.*;

import static com.whiuk.philip.worldquest.MapConstants.BORDER_WIDTH;
import static com.whiuk.philip.worldquest.MapConstants.SHOP_HEIGHT;
import static com.whiuk.philip.worldquest.MapConstants.SHOP_Y;

public class ShopPainter {
    public static void paintShop(Graphics2D g, Shop shop) {
        g.setColor(Color.ORANGE);
        g.drawRect(9+10, SHOP_Y, BORDER_WIDTH-10, SHOP_HEIGHT);
        g.drawRect(9+10, SHOP_Y, BORDER_WIDTH-10, 35);
        g.setColor(Color.WHITE);
        g.drawString(shop.name,25, SHOP_Y+25);

        for (int i = 0; i < shop.items.size(); i++) {
            ShopListing listing = shop.items.get(i);
            String text = listing.item.name + " ("+listing.quantity+"/"+listing.maxQuantity+")         "+listing.getPrice()+" coins";
            g.drawString(text, 40, 100+(i*20));
        }
    }
}
