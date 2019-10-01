package com.whiuk.philip.worldquest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.whiuk.philip.worldquest.JsonUtils.intFromObj;

public class NPC extends GameCharacter {
    final NPCType type;
    final int experience = 10;
    ConversationChoice currentConversation = null;
    Shop shop;
    MovementStrategy movementStrategy;

    NPC(NPCType type, int x, int y, MovementStrategy movementStrategy) {
        super(type.color, x, y, type.health, type.health);
        this.type = type;
        this.shop = type.shop;
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
        return type.dropTable[RandomSource.getRandom().nextInt(type.dropTable.length)].spawn();
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

    static class Provider {
        static Map<String, Shop> loadShopsFromJson(ScenarioData data, JSONArray shopsData) {
            Map<String, Shop> shops = new HashMap<>();
            for (Object sO : shopsData) {
                JSONObject shopData = (JSONObject) sO;
                String id = (String) shopData.get("id");
                String name = (String) shopData.get("name");
                List<ShopListing> shopListings = new ArrayList<>();
                JSONArray shopListingsData = (JSONArray) shopData.get("items");
                for (Object sLO : shopListingsData) {
                    JSONObject shopListingData = (JSONObject) sLO;
                    shopListings.add(new ShopListing(
                            data.itemType((String) shopListingData.get("item")),
                            intFromObj(shopListingData.get("maxQuantity")),
                            intFromObj(shopListingData.get("quantity")),
                            intFromObj(shopListingData.get("basePrice"))
                    ));
                }
                shops.put(id, new Shop(name, shopListings));
            }
            return shops;
        }
    }

    public static class Persistor {
        public static JSONArray saveShopsToJson(Map<String, Shop> shops) {
            return new JSONArray();
            //TOOD:
            /**
            buffer.write(Integer.toString(shops.size()));
            buffer.newLine();
             **/
        }
    }
}

class ShopListing {
    ItemType item;
    int maxQuantity;
    int quantity;
    int basePrice;

    public ShopListing(ItemType item, int maxQuantity, int quantity, int basePrice) {
        this.item = item;
        this.maxQuantity = maxQuantity;
        this.quantity = quantity;
        this.basePrice = basePrice;
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