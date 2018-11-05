package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Player extends GameCharacter {
    HashMap<WorldQuest.Slot, WorldQuest.Armour> armour;
    WorldQuest.Weapon mainHandWeapon;
    List<WorldQuest.Item> inventory;
    int money;
    private int baseDamage;
    int experience;

    Player() {
        this.color = Color.YELLOW;
        this.maxHealth = 10;
        this.health = 10;
        this.baseDamage = 3;
        this.armour = new HashMap<>();
        this.inventory = new ArrayList<>();
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
}