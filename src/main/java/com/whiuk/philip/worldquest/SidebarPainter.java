package com.whiuk.philip.worldquest;

import java.awt.Graphics2D;
import java.awt.Color;
import java.util.List;

import static com.whiuk.philip.worldquest.WorldQuest.GameState.DEAD;

public class SidebarPainter {
    static void paintSidebar(Graphics2D g, WorldQuest game, Player player, List<NPC> visibleNpcs) {
        g.setColor(Color.WHITE);
        paintStats(g, game, player);
        int y = paintEquipment(g, player);
        y = paintInventory(g, y, player);
        paintNPCs(g, y, game, visibleNpcs);
    }

    private static void paintStats(Graphics2D g, WorldQuest game, Player player) {
        g.drawString("Stats", 425, 20);
        g.drawString("Experience: " + player.experience, 450, 40);
        g.drawString("Money: " + player.money, 450, 60);
        g.drawString("Health: ", 450, 80);
        g.drawRect(499, 66, 101, 15);
        g.setColor(Color.RED);
        g.fillRect(500, 67, (player.health*10), 14);
        g.setColor(Color.WHITE);
        if (game.gameState == DEAD) {
            g.drawString("GAME OVER!", 425, 100);
        }
    }

    private static int paintEquipment(Graphics2D g, Player player) {
        String mainHandWeaponName = player.mainHandWeapon != null ? player.mainHandWeapon.name : "";
        g.drawString("Weapon: " + mainHandWeaponName, 425, 140);

        int y = 160;
        g.drawString("Armour:", 425, y);
        y += 20;

        String chestArmourName = player.armour.get(Slot.CHEST) != null ? player.armour.get(Slot.CHEST).name : "";
        g.drawString("Chest: " + chestArmourName, 450, y);
        y += 20;
        return y;
    }

    private static int paintInventory(Graphics2D g, int y, Player player) {
        g.drawString("Inventory:", 425, y);
        y += 20;

        for (int i = 0; i < player.inventory.size(); i++) {
            listItem(g, player, player.inventory.get(i), y, i);
            y += 20;
        }

        y += 20;
        return y;
    }

    private static void listItem(Graphics2D g, Player player, Item item, int y, int index) {
        if (item.canUse()) {
            ButtonPainter.paintButton(g,
                    player.itemBeingUsed != index ? Color.BLUE : Color.DARK_GRAY,
                    player.itemBeingUsed != index ? Color.DARK_GRAY : Color.BLUE,
                    433,
                    y-13,
                    "o"
            );
        }

        if (item.canEquip()) {
            ButtonPainter.paintButton(g,
                    Color.GREEN,
                    Color.DARK_GRAY,
                    450,
                    y-13,
                    "+"
            );
        }

        ButtonPainter.paintButton(g,
                Color.RED,
                Color.DARK_GRAY,
                467,
                y-13,
                "-"
        );

        g.setColor(Color.WHITE);
        g.drawString(item.name, 500, y);
    }

    static int paintNPCs(Graphics2D g, int y, WorldQuest game, List<NPC> visibleNpcs) {
        g.drawString("NPCs", 425, y);

        y += 20;

        for (NPC npc : visibleNpcs) {
            if (npc.canTalk()) {
                ButtonPainter.paintButton(g,
                        npc.currentConversation != null ? Color.DARK_GRAY : Color.BLUE,
                        npc.currentConversation != null ? Color.BLUE : Color.DARK_GRAY,
                        433,
                        y-13,
                        "o"
                );
            }

            //TODO: canFight vs canDie
            String npcHealth = npc.canFight() ? ": " + npc.health + "/" + npc.type.health : "";
            //TODO: typeName vs NPC unique name
            String npcInfo = npc.type.name + npcHealth;
            g.drawString(npcInfo, 500, y);
            y+=20;
        }

        return y;
    }
}
