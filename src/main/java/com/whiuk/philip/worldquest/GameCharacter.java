package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

abstract class GameCharacter {
    Color color;
    int x;
    int y;
    int maxHealth;
    int health;

    GameCharacter(Color color, int x, int y, int maxHealth, int health) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.maxHealth = maxHealth;
        this.health = health;
    }

    abstract void actionOnNpc(WorldQuest game, NPC npc);

    void attackNpc(WorldQuest game, NPC npc) {
        attack(game, npc);
    }

    void attack(WorldQuest game, GameCharacter target) {
        if (target.isHit()) {
            int damage = this.calculateDamage();
            game.reportHit(this, target, damage);
            target.takeHit(damage);
            attackSuccessful(damage);
        }
    }

    void attackSuccessful(int damageCaused) {
    }

    abstract int calculateDamage();

    abstract boolean isHit();

    void takeHit(int damage) {
        if (health < damage) {
            health = 0;
        } else {
            health -= damage;
        }
    }

    boolean isDead() {
        return health <= 0;
    }
}

class Experience {
    int level;
    int experience;

    static Experience NoExperience() {
        return new Experience(0);
    }

    Experience(int experience) {
        this.level = ExperienceTable.getLevel(experience);
        this.experience = experience;
    }
}