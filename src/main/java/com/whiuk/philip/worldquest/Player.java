package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player extends GameCharacter {
    Map<String,Experience> skills;
    Weapon mainHandWeapon;
    HashMap<Slot, Armour> armour;
    List<Item> inventory;
    int money;
    int baseDamage;
    int itemBeingUsed = -1;

    Player(int x, int y) {
        super(Color.YELLOW, x, y, 10, 10);
        this.baseDamage = 3;
        this.armour = new HashMap<>();
        this.inventory = new ArrayList<>();
        this.skills = new HashMap<>();
    }

    Player(
            int maxHealth, int health, int money, int baseDamage,
            Map<String,Experience> skills, Weapon mainHandWeapon, Map<Slot, Armour> armour,
            List<Item> inventory,
            int x, int y) {
        this(x, y);
        this.mainHandWeapon = mainHandWeapon;
        this.maxHealth = maxHealth;
        this.health = health;
        this.money = money;
        this.baseDamage = baseDamage;
        this.armour = new HashMap<>();
        this.armour.putAll(armour);
        this.inventory = inventory;
        this.skills = new HashMap<>();
        this.skills.putAll(skills);
    }

    void actionOnNpc(WorldQuest game, NPC npc) {
        if (npc.canFight()) {
            attackNpc(game, npc);
        }
    }

    private void attackNpc(WorldQuest game, NPC npc) {
        super.attackNpc(npc);
        game.npcAttacked(npc);
    }

    @Override
    void attackSuccessful(int damageCaused){
        gainExperience(mainHandWeapon == null ? "Unarmed" : mainHandWeapon.type, damageCaused*4);
    }

    void gainExperience(String skill, int newExp) {
        Experience experience;
        if (!skills.containsKey(skill)) {
            experience = Experience.NoExperience();
            skills.put(skill, experience);
        } else {
            experience = skills.get(skill);
        }
        while (ExperienceTable.toLevel(experience.level, experience.experience) <= newExp) {
            experience.level++;
        }
        experience.experience += newExp;
    }


    @Override
    void takeHit(int damage) {
        if (armour.get(Slot.CHEST) != null) {
            damage -= armour.get(Slot.CHEST).protection;
        }
        if (damage < 0) {
            damage = 0;
        }
        super.takeHit(damage);
        if (health < damage) {
            health = 0;
        } else {
            health -= damage;
        }
    }

    @Override
    int calculateDamage() {
        int strBonus = skills.getOrDefault("Strength", Experience.NoExperience()).level;
        int baseDamage = strBonus + 2 + (mainHandWeapon != null ? mainHandWeapon.damage : 0);
        boolean criticalHit = RandomSource.getRandom().nextInt(11) == 10;
        if (criticalHit) {
            return baseDamage * 3;
        }
        return baseDamage;
    }

    void addMoney(int money) {
        this.money += money;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }
}