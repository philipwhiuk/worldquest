package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.ToggleButton;
import com.whiuk.philip.worldquest.ui.UI;

import java.awt.event.MouseEvent;
import java.awt.*;

import static com.whiuk.philip.worldquest.WorldQuest.GameState.DEAD;

class SidebarUI extends Rectangle implements UI {
    private WorldQuest game;
    private SidebarTab[] tabs;
    private ToggleButton[] tabButtons;
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
        tabButtons = new ToggleButton[]{
                new ToggleButton(Color.GREEN, Color.BLACK, 425, 100, "Stats", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(0);
                    }
                },
                new ToggleButton(Color.GREEN, Color.BLACK, 475, 100, "Skills", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(1);
                    }
                },
                new ToggleButton(Color.GREEN, Color.BLACK, 525, 100, "Equip.", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(2);
                    }
                },
                new ToggleButton(Color.GREEN, Color.BLACK, 425, 125, "Items", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(3);
                    }
                },
                new ToggleButton(Color.GREEN, Color.BLACK, 475, 125, "NPCs", false) {
                    @Override
                    public void onClick(MouseEvent e) {
                        setActiveTab(4);
                    }
                },
                new ToggleButton(Color.GREEN, Color.BLACK, 525, 125, "Quests", false) {
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
        for (ToggleButton button : tabButtons) {
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
        g.drawString("Food: ", 450, 60);
        g.drawRect(499, 46, 101, 15);
        g.setColor(Color.ORANGE);
        g.fillRect(500, 47, (game.player.food), 14);

        g.setColor(Color.WHITE);
        g.drawString("Money: " + game.player.money, 450, 80);

        g.setColor(Color.WHITE);
    }

    @Override
    public void onClick(MouseEvent e) {
        for (ToggleButton button : tabButtons) {
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