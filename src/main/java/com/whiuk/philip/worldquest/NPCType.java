package com.whiuk.philip.worldquest;

import java.awt.*;

public class NPCType {
    final String name;
    final Color color;
    public final boolean canMove;
    public final boolean canFight;
    public final boolean isAggressive;
    public int health;
    public int damage;
    public GObjects.ItemDrop[] dropTable;
    public final boolean canTalk;
    final Conversation conversation;
    public Shop shop;

    NPCType(String name, Color color,
            boolean canMove,
            boolean canFight, boolean isAggressive,
            int health, int damage,
            GObjects.ItemDrop[] dropTable,
            boolean canTalk, Conversation conversation, Shop shop) {
        this.name = name;
        this.color = color;
        this.canMove = canMove;
        this.canFight = canFight;
        this.isAggressive = isAggressive;
        this.health = health;
        this.damage = damage;
        this.dropTable = dropTable;
        this.canTalk = canTalk;
        this.conversation = conversation;
        this.shop = shop;
    }
}