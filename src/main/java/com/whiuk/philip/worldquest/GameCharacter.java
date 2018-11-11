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

    void attackNpc(NPC npc) {
        attack(npc);
    }

    void attack(GameCharacter c) {
        if (RandomSource.getRandom().nextBoolean()) {
            int damage = this.calculateDamage();
            c.takeHit(damage);
            attackSuccessful(damage);
        }
    }

    void attackSuccessful(int damageCaused) {
    }

    abstract int calculateDamage();

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

    Experience(int experience) {
        this.level = ExperienceTable.getLevel(experience);
        this.experience = experience;
    }
}