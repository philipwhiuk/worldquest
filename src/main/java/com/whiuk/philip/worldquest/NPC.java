package com.whiuk.philip.worldquest;

public class NPC extends GameCharacter {
    final NPCType type;
    final int experience = 10;

    NPC(NPCType type, int x, int y) {
        this.type = type;
        this.color = type.color;
        this.x = x;
        this.y = y;
        this.health = type.health;
    }

    boolean canFight() {
        return type.canFight;
    }

    @Override
    void actionOnNpc(WorldQuest game, NPC npc) {
        //TODO: NPC->NPC interaction
    }

    @Override
    int calculateDamage() {
        return type.damage;
    }

    public GObjects.ItemDrop dropItem() {
        return type.dropTable[RandomSource.getRandom().nextInt(type.dropTable.length)].copy();
    }
}