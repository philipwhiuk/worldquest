package com.whiuk.philip.worldquest;

import java.awt.event.MouseEvent;
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
                new StatsTab(game),
                new SkillsTab(game),
                new EquipmentTab(game),
                new ItemsTab(game),
                new NPCsTab(game),
                new QuestsTab(game)
        };
        tabButtons = new TextButton[]{
                new TextButton(Color.GREEN, Color.BLACK, 425, 80, "Stats", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(0);
                    }
                },
                new TextButton(Color.GREEN, Color.BLACK, 475, 80, "Skills", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(1);
                    }
                },
                new TextButton(Color.GREEN, Color.BLACK, 525, 80, "Equip.", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(2);
                    }
                },
                new TextButton(Color.GREEN, Color.BLACK, 425, 105, "Items", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(3);
                    }
                },
                new TextButton(Color.GREEN, Color.BLACK, 475, 105, "NPCs", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(4);
                    }
                },
                new TextButton(Color.GREEN, Color.BLACK, 525, 105, "Quests", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(5);
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

    private void setActiveTab(int activeTab) {
        this.activeTab = tabs[activeTab];
        for(int i = 0; i < tabButtons.length; i++) {
            tabButtons[i].changeToggleState(i == activeTab);
        }
    }


}