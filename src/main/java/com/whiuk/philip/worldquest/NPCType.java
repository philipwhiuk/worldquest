package com.whiuk.philip.worldquest;

import java.awt.*;

public class NPCType {
    final String name;
    final Color color;
    public final boolean canFight;
    public int health;
    public int damage;
    public GObjects.ItemDrop[] dropTable;

    NPCType(String name, Color color, boolean canFight, int health, int damage, GObjects.ItemDrop[] dropTable) {
        this.name = name;
        this.color = color;
        this.canFight = canFight;
        this.health = health;
        this.damage = damage;
        this.dropTable = dropTable;
    }
}