package com.whiuk.philip.worldquest;

import java.awt.Graphics2D;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.whiuk.philip.worldquest.SidebarViewState.*;
import static com.whiuk.philip.worldquest.WorldQuest.GameState.DEAD;

public class SidebarPainter {
    static void paintSidebar(Graphics2D g, SidebarViewState sidebarViewState, WorldQuest game, Player player, List<NPC> visibleNpcs) {
        g.setColor(Color.WHITE);
        paintStats(g, game, player);
        paintButtons(g, sidebarViewState);
        switch (sidebarViewState) {
            case SKILLS: paintSkills(g, player, 150); break;
            case EQUIPMENT: paintEquipment(g, player, 150); break;
            case ITEMS: paintInventory(g, player, 150); break;
            case NPCS: paintNPCs(g, 150, game, visibleNpcs); break;
        }
    }

    private static void paintButtons(Graphics2D g, SidebarViewState sidebarViewState) {
        ButtonPainter.paintTextButton(
                g, Color.GREEN, Color.BLACK, 425, 80, "Skills",
                sidebarViewState == SKILLS);
        ButtonPainter.paintTextButton(
                g, Color.GREEN, Color.BLACK, 475, 80, "Equip.",
                sidebarViewState == EQUIPMENT);
        ButtonPainter.paintTextButton(
                g, Color.GREEN, Color.BLACK, 525, 80, "Items",
                sidebarViewState == ITEMS);
        ButtonPainter.paintTextButton(
                g, Color.GREEN, Color.BLACK, 425, 105, "NPCs",
                sidebarViewState == NPCS);
    }

    private static void paintStats(Graphics2D g, WorldQuest game, Player player) {
        g.drawString("Stats", 425, 20);

        if (game.gameState == DEAD) {
            g.drawString("GAME OVER!", 450, 40);
        } else {
            g.drawString("Health: ", 450, 40);
        }
        g.drawRect(499, 26, 101, 15);
        g.setColor(Color.RED);
        g.fillRect(500, 27, (player.health*10), 14);

        g.setColor(Color.WHITE);
        g.drawString("Money: " + player.money, 450, 60);

        g.setColor(Color.WHITE);
    }

    private static void paintSkills(Graphics2D g, Player player, int y) {
        Set<Map.Entry<String,Experience>> skillEntries = player.skills.entrySet();
        Iterator<Map.Entry<String,Experience>> skillI = skillEntries.iterator();
        for (int i = 0; i < skillEntries.size(); i++) {
            Map.Entry<String, Experience> skill = skillI.next();
            Experience experience = skill.getValue();
            String skillDescriptor = skill.getKey()+ ": "+ experience.level + " ("+experience.experience+"xp)";
            g.setColor(Color.WHITE);
            g.drawString(skillDescriptor, 450, y);
            y += 20;
        }
    }

    private static void paintEquipment(Graphics2D g, Player player, int y) {
        g.setColor(Color.WHITE);
        String mainHandWeaponName = player.mainHandWeapon != null ?
                player.mainHandWeapon.name + " (+"+player.mainHandWeapon.damage+")" :
                "";
        g.drawString("Weapon: " + mainHandWeaponName, 425, y);
        y += 20;

        g.drawString("Armour:", 425, y);
        y += 20;

        Armour chestArmour = player.armour.get(Slot.CHEST);
        String chestArmourName = chestArmour != null ?
                chestArmour.name + " (+"+chestArmour.protection+")" :
                "";
        g.drawString("Chest: " + chestArmourName, 450, y);
    }

    private static void paintInventory(Graphics2D g, Player player, int y) {
        g.setColor(Color.WHITE);
        g.drawString("Inventory:", 425, y);
        y += 20;

        for (int i = 0; i < player.inventory.size(); i++) {
            listItem(g, player, player.inventory.get(i), y, i);
            y += 20;
        }
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

    private static void paintNPCs(Graphics2D g, int y, WorldQuest game, List<NPC> visibleNpcs) {
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

            String npcHealth = npc.canFight() ? ": " + npc.health + "/" + npc.type.health : "";
            String npcInfo = npc.type.name + npcHealth;
            g.drawString(npcInfo, 500, y);
            y+=20;
        }

    }
}
