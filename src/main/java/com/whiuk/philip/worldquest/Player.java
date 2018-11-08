package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player extends GameCharacter {
    HashMap<WorldQuest.Slot, WorldQuest.Armour> armour;
    WorldQuest.Weapon mainHandWeapon;
    List<WorldQuest.Item> inventory;
    int money;
    int baseDamage;
    int experience;
    int itemBeingUsed = -1;

    Player(int x, int y) {
        super(Color.YELLOW, x, y, 10, 10);
        this.baseDamage = 3;
        this.armour = new HashMap<>();
        this.inventory = new ArrayList<>();
    }

    Player(
            int maxHealth, int health, int money, int experience, int baseDamage,
            WorldQuest.Weapon mainHandWeapon, Map<WorldQuest.Slot, WorldQuest.Armour> armour,
            List<WorldQuest.Item> inventory,
            int x, int y) {
        this(x, y);
        this.maxHealth = maxHealth;
        this.health = health;
        this.money = money;
        this.experience = experience;
        this.baseDamage = baseDamage;
        this.mainHandWeapon = mainHandWeapon;
        this.armour = new HashMap<>();
        this.armour.putAll(armour);
        this.inventory = inventory;
    }

    void actionOnNpc(WorldQuest game, NPC npc) {
        if (npc.canFight()) {
            attackNpc(game, npc);
        }
    }

    void attackNpc(WorldQuest game, NPC npc) {
        super.attackNpc(npc);
        game.npcAttacked(npc);
    }

    void addExperience(int points) {
        experience += points;
    }

    @Override
    int calculateDamage() {
        return baseDamage + (mainHandWeapon != null ? mainHandWeapon.damage : 0);
    }

    void addMoney(int money) {
        this.money = money;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }
}