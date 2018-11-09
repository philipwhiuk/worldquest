package com.whiuk.philip.worldquest;

import java.awt.*;

public class NPCType {
    final String name;
    final Color color;
    //TODO: Move Types: Pursuit, Guarding, Zonal
    public final boolean canMove;
    //TODO: Fight Postures: Aggressive, Defensive, Neutral
    public final boolean canFight;
    public int health;
    public int damage;
    public GObjects.ItemDrop[] dropTable;
    public final boolean canTalk;
    final ConversationChoice conversationEntryPoint;
    public Shop shop;

    NPCType(String name, Color color,
            boolean canMove,
            boolean canFight, int health, int damage, GObjects.ItemDrop[] dropTable,
            boolean canTalk, ConversationChoice conversationEntryPoint, Shop shop) {
        this.name = name;
        this.color = color;
        this.canMove = canMove;
        this.canFight = canFight;
        this.health = health;
        this.damage = damage;
        this.dropTable = dropTable;
        this.canTalk = canTalk;
        this.conversationEntryPoint = conversationEntryPoint;
        this.shop = shop;
    }
}