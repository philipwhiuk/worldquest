package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopsProvider {

    static Map<String, Shop> loadShopsFromBuffer(ScenarioData data, BufferedReader buffer) throws IOException {
        int shopsCount = Integer.parseInt(buffer.readLine());
        Map<String, Shop> shops = new HashMap<>();
        for (int s = 0; s < shopsCount; s++) {
            String[] shopData = buffer.readLine().split(",");
            String id = shopData[0];
            String name = shopData[1];
            int itemCount = Integer.parseInt(shopData[2]);
            List<ShopListing> shopListings = new ArrayList<>();
            for (int i = 0; i < itemCount; i++) {
                String[] shopListingData = buffer.readLine().split(",");

                shopListings.add(new ShopListing(
                        data.item(shopListingData[0]).copy(),
                        Integer.parseInt(shopListingData[1]),
                        Integer.parseInt(shopListingData[2]),
                        Integer.parseInt(shopListingData[3])
                ));
            }
            shops.put(id, new Shop(name, shopListings));
        }
        return shops;
    }
}
