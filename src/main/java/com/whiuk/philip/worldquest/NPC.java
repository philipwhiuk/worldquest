package com.whiuk.philip.worldquest;

public class NPC extends GameCharacter {
    final NPCType type;
    final int experience = 10;

    NPC(NPCType type, int x, int y) {
        super(type.color, x, y, type.health, type.health);
        this.type = type;
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