package com.whiuk.philip.worldquest;

import java.util.ArrayList;
import java.util.List;

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