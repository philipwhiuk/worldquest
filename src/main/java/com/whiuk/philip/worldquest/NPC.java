package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPC extends GameCharacter {
    final NPCType type;
    final int experience = 10;
    ConversationChoice currentConversation = null;
    Shop shop;
    MovementStrategy movementStrategy;

    NPC(NPCType type, int x, int y, MovementStrategy movementStrategy) {
        super(type.color, x, y, type.health, type.health);
        this.type = type;
        this.shop = type.shop != null ? type.shop.copy() : null;
        this.movementStrategy = movementStrategy;
    }

    boolean canMove() { return type.canMove; }

    boolean canFight() {
        return type.canFight;
    }

    boolean canTalk() { return type.canTalk; }

    void startConversation(WorldQuest game) {
        currentConversation = type.conversation.selector.apply(new QuestState(game));
    }

    @Override
    void actionOnNpc(WorldQuest game, NPC npc) {
    }

    @Override
    int calculateDamage() {
        return RandomSource.getRandom().nextInt(type.damage);
    }

    @Override
    boolean isHit() {
        return RandomSource.getRandom().nextBoolean();
    }

    public GObjects.ItemDrop dropItem() {
        return type.dropTable[RandomSource.getRandom().nextInt(type.dropTable.length)].copy();
    }

    public boolean isAggressive() {
        return type.isAggressive;
    }

    public boolean hasBeenAttacked() {
        return false;
    }
}

class Shop {
    final String name;
    List<ShopListing> items;

    Shop(String name, List<ShopListing> items) {
        this.name = name;
        this.items = items;
    }

    public Shop copy() {
        ArrayList<ShopListing> items = new ArrayList<>();
        this.items.stream().forEach(item -> items.add(item.copy()));
        return new Shop(name, items);
    }

    static class Provider {
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

    public static class Persistor {
        public static void saveShopsToBuffer(Map<String, Shop> shops, BufferedWriter buffer) throws IOException {
            buffer.write(Integer.toString(shops.size()));
            buffer.newLine();
        }
    }
}

class ShopListing {
    Item item;
    int maxQuantity;
    int quantity;
    int basePrice;

    public ShopListing(Item item, int maxQuantity, int quantity, int basePrice) {
        this.item = item;
        this.maxQuantity = maxQuantity;
        this.quantity = quantity;
        this.basePrice = basePrice;
    }

    public ShopListing copy() {
        return new ShopListing(item.copy(), maxQuantity, quantity, basePrice);
    }

    public int getPrice() {
        return basePrice;
    }
}

class CraftingOptions {

    public final String name;
    public final List<Recipe> recipes;

    public CraftingOptions(String name, List<Recipe> recipes) {
        this.name = name;
        this.recipes = recipes;
    }
}