package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Player extends GameCharacter {
    Map<String,Experience> stats;
    Map<String,Experience> skills;
    Weapon<? extends WeaponType> mainHandWeapon;
    HashMap<Slot, Armour> armour;
    Inventory inventory;
    SortedMap<String, QuestProgress> quests;
    int money;
    int itemBeingUsed = -1;
    int maxFood;
    int food;
    int foodTick;

    Player(int x, int y) {
        super(Color.YELLOW, x, y, 10, 10);
        this.food = 100;
        this.maxFood = 100;
        this.armour = new HashMap<>();
        this.inventory = new Inventory();
        this.stats = new HashMap<>();
        this.skills = new HashMap<>();
        this.quests = new TreeMap<>();
    }

    Player(
            int maxHealth, int health,
            int maxFood, int food,
            int money,
            Map<String,Experience> stats,
            Map<String,Experience> skills,
            Weapon<WeaponType> mainHandWeapon, Map<Slot, Armour> armour,
            List<Item> items, Map<String, QuestProgress> quests,
            int x, int y) {
        this(x, y);
        this.maxHealth = maxHealth;
        this.health = health;
        this.maxFood = maxFood;
        this.food = food;
        this.money = money;
        this.mainHandWeapon = mainHandWeapon;
        this.armour = new HashMap<>();
        this.armour.putAll(armour);
        this.inventory = new Inventory();
        this.inventory.addAll(items);
        this.skills = new HashMap<>();
        this.skills.putAll(skills);
        this.stats = new HashMap<>();
        this.stats.putAll(stats);
        this.quests = new TreeMap<>();
        this.quests.putAll(quests);
    }

    void actionOnNpc(WorldQuest game, NPC npc) {
        if (npc.canFight()) {
            attackNpc(game, npc);
        } else {
            game.eventMessage("You can't fight this NPC!");
        }
    }

    void attackNpc(WorldQuest game, NPC npc) {
        super.attackNpc(game, npc);
        game.npcAttacked(npc);
    }

    @Override
    void attackSuccessful(int damageCaused){
        gainStatExperience("Strength", (int) (damageCaused*1.5));
        gainExperience(mainHandWeapon == null ? "Unarmed" : mainHandWeapon.getType().weaponSkill, damageCaused*4);
    }

    void gainStatExperience(String stat, int newExp) {
        Experience experience;
        if (!stats.containsKey(stat)) {
            experience = Experience.NoExperience();
            stats.put(stat, experience);
        } else {
            experience = stats.get(stat);
        }
        while (ExperienceTable.toLevel(experience.level, experience.experience) <= newExp) {
            experience.level++;
        }
        experience.experience += newExp;
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
    boolean isHit() {
        double chanceToHit = 100;
        if (armour.get(Slot.OFF_HAND) != null) {
            //TODO: Off-hand skill
            chanceToHit -= 10;
        }
        boolean isHit = RandomSource.getRandom().nextInt(100) < chanceToHit;
        if (!isHit) {
            gainExperience("Defence", 10);
        }
        return isHit;
    }


    @Override
    void takeHit(int damage) {
        Slot location = calculateSlot();
        if (armour.get(location) != null) {
            damage -= armour.get(Slot.CHEST).getType().protection;
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

    private Slot calculateSlot() {
        int locationRoll = RandomSource.getRandom().nextInt(100);
        if (locationRoll < 44) {
            return Slot.CHEST;
        }
        locationRoll -= 44;
        if (locationRoll < 22) {
            return Slot.LEGS;
        }
        locationRoll -= 22;
        if (locationRoll < 16) {
            return Slot.HEAD;
        }
        locationRoll -= 16;
        if (locationRoll < 12) {
            return Slot.ARMS;
        }
        locationRoll -= 12;
        if (locationRoll < 3) {
            return Slot.HANDS;
        }
        return Slot.FEET;
    }

    @Override
    int calculateDamage() {
        int strBonus = stats.getOrDefault("Strength", Experience.NoExperience()).level;
        int baseDamage = strBonus + 2 + (mainHandWeapon != null ? mainHandWeapon.getType().damage : 0);
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

    QuestStatus getQuestState(String questName) {
        if (quests.containsKey(questName)) {
            return quests.get(questName).isFinished() ? QuestStatus.FINISHED : QuestStatus.STARTED;
        }
        return QuestStatus.NOT_STARTED;
    }

    public QuestProgress getQuest(String name) {
        return quests.get(name);
    }

    public void applyStatChanges(List<StatChange> statChanges) {
        for (StatChange s : statChanges) {
            switch(s.stat) {
                case "Health":
                    health += s.change;
                    if (health > maxHealth)
                        health = maxHealth;
                    break;
                case "Food":
                    food += s.change;
                    if (food > maxFood)
                        food = maxFood;
                    break;
            }
        }
    }
}