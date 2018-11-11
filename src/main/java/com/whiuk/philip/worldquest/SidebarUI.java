package com.whiuk.philip.worldquest;

import java.awt.event.MouseEvent;
import java.util.*;
import java.awt.*;

import static com.whiuk.philip.worldquest.WorldQuest.GameState.DEAD;

class SidebarUI extends Rectangle implements UI {
    private WorldQuest game;
    private SidebarTab[] tabs;
    private TextButton[] tabButtons;
    private SidebarTab activeTab;

    SidebarUI(WorldQuest game) {
        super(400, 0, 240, 480);
        this.game = game;
        tabs = new SidebarTab[]{
                new SkillsTab(),
                new EquipmentTab(),
                new ItemsTab(),
                new NPCsTab()
        };
        tabButtons = new TextButton[]{
                new TextButton(Color.GREEN, Color.BLACK, 425, 80, "Skills", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(0);
                    }
                },
                new TextButton(Color.GREEN, Color.BLACK, 475, 80, "Equip.", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(1);
                    }
                },
                new TextButton(Color.GREEN, Color.BLACK, 525, 80, "Items", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(2);
                    }
                },
                new TextButton(Color.GREEN, Color.BLACK, 425, 105, "NPCs", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(3);
                    }
                }
        };
        setActiveTab(0);
    }

    @Override
    public void render(Graphics2D g) {
        paintStats(g, game);
        for (TextButton button : tabButtons) {
            button.render(g);
        }
        activeTab.render(g);
    }

    private static void paintStats(Graphics2D g, WorldQuest game) {
        g.setColor(Color.WHITE);
        g.drawString("Stats", 425, 20);

        if (game.gameState == DEAD) {
            g.drawString("GAME OVER!", 450, 40);
        } else {
            g.drawString("Health: ", 450, 40);
            g.drawRect(499, 26, 101, 15);
            g.setColor(Color.RED);
            g.fillRect(500, 27, (game.player.health * 10), 14);
        }

        g.setColor(Color.WHITE);
        g.drawString("Money: " + game.player.money, 450, 60);

        g.setColor(Color.WHITE);
    }

    @Override
    public void onClick(MouseEvent e) {
        for (TextButton button : tabButtons) {
            if (button.contains(e.getPoint())) {
                button.onClick(e);
                return;
            }
        }
        if (activeTab.contains(e.getPoint())) {
            activeTab.onClick(e);
        }
    }

    private Rectangle npcButtonTalkLocation(int index) {
        int offset = 20*index;
        return new Rectangle(433,155+offset, 15, 15);
    }

    private void setActiveTab(int activeTab) {
        this.activeTab = tabs[activeTab];
        for(int i = 0; i < tabButtons.length; i++) {
            tabButtons[i].changeToggleState(i == activeTab);
        }
    }

    abstract class SidebarTab extends Rectangle implements UI {
        SidebarTab() {
            super(420, 150, 240, 330);
        }
    }

    class SkillsTab extends SidebarTab {
        @Override
        public void render(Graphics2D g) {
            int offset = y;
            Set<Map.Entry<String, Experience>> skillEntries = game.player.skills.entrySet();
            Iterator<Map.Entry<String, Experience>> skillI = skillEntries.iterator();
            for (int i = 0; i < skillEntries.size(); i++) {
                Map.Entry<String, Experience> skill = skillI.next();
                Experience experience = skill.getValue();
                String skillDescriptor = skill.getKey() + ": " + experience.level + " (" + experience.experience + "xp)";
                g.setColor(Color.WHITE);
                g.drawString(skillDescriptor, 450, offset);
                offset += 20;
            }
        }

        @Override
        public void onClick(MouseEvent e) {

        }
    }

    private class EquipmentTab extends SidebarTab {

        @Override
        public void render(Graphics2D g) {
            int offset = y;
            g.setColor(Color.WHITE);
            String mainHandWeaponName = game.player.mainHandWeapon != null ?
                    game.player.mainHandWeapon.name + " (+"+game.player.mainHandWeapon.damage+")" :
                    "";
            g.drawString("Weapon: " + mainHandWeaponName, 425, offset);
            offset += 20;

            g.drawString("Armour:", 425, offset);
            offset += 20;

            Armour chestArmour = game.player.armour.get(Slot.CHEST);
            String chestArmourName = chestArmour != null ?
                    chestArmour.name + " (+"+chestArmour.protection+")" :
                    "";
            g.drawString("Chest: " + chestArmourName, 450, offset);
        }

        @Override
        public void onClick(MouseEvent e) {

        }
    }

    private class ItemsTab extends SidebarTab {

        @Override
        public void render(Graphics2D g) {
            int offset = y + 13;
            g.setColor(Color.WHITE);
            for (int i = 0; i < game.player.inventory.size(); i++) {
                listItem(g, game.player, game.player.inventory.get(i), offset, i);
                offset += 20;
            }
        }

        private void listItem(Graphics2D g, Player player, Item item, int offset, int index) {
            if (item.canUse()) {
                ButtonPainter.paintButton(g,
                        player.itemBeingUsed != index ? Color.BLUE : Color.DARK_GRAY,
                        player.itemBeingUsed != index ? Color.DARK_GRAY : Color.BLUE,
                        433,
                        offset - 13,
                        "o"
                );
            }

            if (item.canEquip()) {
                ButtonPainter.paintButton(g,
                        Color.GREEN,
                        Color.DARK_GRAY,
                        450,
                        offset - 13,
                        "+"
                );
            }

            ButtonPainter.paintButton(g,
                    Color.RED,
                    Color.DARK_GRAY,
                    467,
                    offset - 13,
                    "-"
            );

            g.setColor(Color.WHITE);
            g.drawString(item.name, 500, offset);
        }

        @Override
        public void onClick(MouseEvent e) {
            for (int i = 0; i < game.player.inventory.size(); i++) {
                if (inventoryButtonLocation(i, 0).contains(e.getPoint())) {
                    game.useItem(i);
                    return;
                } else if (inventoryButtonLocation(i, 1).contains(e.getPoint())) {
                    game.equipItem(i);
                    return;
                } else if (inventoryButtonLocation(i, 2).contains(e.getPoint())) {
                    game.dropItem(i);
                    return;
                }
            }
        }

        private Rectangle inventoryButtonLocation(int index, int button) {
            int xOffset = button*17;
            int offset = y+(20*index);
            return new Rectangle(433+xOffset, offset, 15, 15);
        }
    }


    private class NPCsTab extends SidebarTab {
        @Override
        public void render(Graphics2D g) {
            int offset = y;
            g.drawString("NPCs", 425, offset);

            offset += 20;

            for (NPC npc : game.visibleNpcs) {
                if (npc.canTalk()) {
                    ButtonPainter.paintButton(g,
                            npc.currentConversation != null ? Color.DARK_GRAY : Color.BLUE,
                            npc.currentConversation != null ? Color.BLUE : Color.DARK_GRAY,
                            433,
                            offset-13,
                            "o"
                    );
                }

                String npcHealth = npc.canFight() ? ": " + npc.health + "/" + npc.type.health : "";
                String npcInfo = npc.type.name + npcHealth;
                g.drawString(npcInfo, 500, offset);
                offset += 20;
            }
        }

        @Override
        public void onClick(MouseEvent e) {
            Action a = calculateAction(e.getPoint());
            game.processAction(a);
        }

        private Action calculateAction(Point p) {
            if (npcButtonTalkLocation(0).contains(p) && game.visibleNpcs.size() >= 1) {
                return Action.TALK_0;
            } else if (npcButtonTalkLocation(1).contains(p) && game.visibleNpcs.size() >= 2) {
                return Action.TALK_1;
            }
            return null;
        }
    }
}