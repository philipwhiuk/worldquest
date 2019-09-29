package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.Sidebar;
import com.whiuk.philip.worldquest.ui.Tab;
import com.whiuk.philip.worldquest.ui.TabView;

import java.awt.event.MouseEvent;
import java.awt.*;

import static com.whiuk.philip.worldquest.AppState.GAME_DEAD;

class GameSidebar extends Sidebar {
    private WorldQuest game;
    private final TabView tabView;

    GameSidebar(WorldQuest game) {
        super();
        this.game = game;
        tabView = new TabView(new Tab[]{
                new Tab("Stats", 420, 170, 240, 330,
                        new StatsView(game)),
                new Tab("Skills", 420, 170, 240, 330,
                        new SkillsView(game)),
                new Tab("Equip.", 420, 170, 240, 330,
                        new EquipmentView(game)),
                new Tab("Items", 420, 170, 240, 330,
                        new ItemsView(game)),
                new Tab("NPCs", 420, 170, 240, 330,
                        new NPCsView(game)),
                new Tab("Quests", 420, 170, 240, 330,
                        new QuestsView(game))
        }, 425, 100, 525);
        tabView.setActiveTab(0);
    }

    @Override
    public void render(Graphics2D g) {
        paintStats(g);
        tabView.render(g);
    }

    private void paintStats(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.drawString("Stats", 425, 20);

        if (game.appState == GAME_DEAD) {
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
        tabView.onClick(e);
    }
}