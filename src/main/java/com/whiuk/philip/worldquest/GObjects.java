package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.HashMap;

import static com.whiuk.philip.worldquest.MapConstants.MAP_SPACING;
import static com.whiuk.philip.worldquest.MapConstants.TILE_HEIGHT;
import static com.whiuk.philip.worldquest.MapConstants.TILE_WIDTH;

public class GObjects {

    static HashMap<String, GameObjectBuilder> provideBuilders() {
        HashMap<String, GameObjectBuilder> gameObjectBuilders = new HashMap<>();
        gameObjectBuilders.put("Stairs", new GObjects.StairsBuilder());
        gameObjectBuilders.put("Tree", new GObjects.TreeBuilder());
        return gameObjectBuilders;
    }

    abstract static class GameObject {
        boolean deleted = false;

        void tick() {}

        abstract void draw(Graphics2D g, int x, int y);

        public boolean canMoveTo() {
            return true;
        }

        public void onMoveTo(Player player) {}

        public boolean isDeleted() {
            return deleted;
        }

        public void doAction(Player player) {}
    }

    abstract static class GameObjectBuilder {
        public abstract GameObject build(String[] arguments);
    }

    static class StairsBuilder extends GameObjectBuilder {
        public GameObject build(String[] arguments) {
            return new Stairs(arguments);
        }
    }

    static class Stairs extends GameObject {
        private String map;
        private int startX;
        private int startY;

        public Stairs(String[] arguments) {
            this(arguments[0], Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
        }

        public Stairs(String map, int startX, int startY) {
            this.map = map;
            this.startX = startX;
            this.startY = startY;
        }

        void go(WorldQuest worldQuest) {
            worldQuest.switchMap(map, startX, startY);
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(Color.blue);
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT / 2);
        }
    }

    static class TreeBuilder extends GameObjectBuilder {
        public GameObject build(String[] arguments) {
            return new Tree();
        }
    }

    static class Tree extends GameObject {
        boolean cutDown;
        int ticksToRegrow;

        public Tree() {
        }

        @Override
        void tick() {
            if (cutDown && ticksToRegrow <= 1) {
                cutDown = false;
            } else {
                ticksToRegrow -= 1;
            }
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            if (!cutDown) {
                g.setColor(new Color(0, 40, 0));
                g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + 1, MAP_SPACING + (y * TILE_HEIGHT) + 1,
                        TILE_WIDTH - 2, TILE_HEIGHT - 2);
            } else {
                g.setColor(new Color(50,34,4));
                g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + 2, MAP_SPACING + (y * TILE_HEIGHT) + 2,
                        TILE_WIDTH - 4, TILE_HEIGHT - 4);
            }
        }

        @Override
        public boolean canMoveTo() {
            return cutDown;
        }

        @Override
        public void doAction(Player player) {
            if (player.mainHandWeapon instanceof WorldQuest.Hatchet) {
                cutDown = true;
                ticksToRegrow = 10;
                player.inventory.add(new WorldQuest.Item("Oak logs", true));
            }
        }
    }

    static class Fire extends GameObject {

        @Override
        public boolean canMoveTo() {
            return true;
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(Color.ORANGE);
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + 2, MAP_SPACING + (y * TILE_HEIGHT) + 2,
                    TILE_WIDTH - 4, TILE_HEIGHT - 4);
            g.setColor(Color.RED);
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + 3, MAP_SPACING + (y * TILE_HEIGHT) + 3,
                    TILE_WIDTH - 6, TILE_HEIGHT - 6);
        }
    }

    static class ItemDrop extends GObjects.GameObject {
        private WorldQuest.Item item;
        private int money;

        ItemDrop(WorldQuest.Item i) {
            this.item = i;
        }

        ItemDrop(int money) {
            this.money = money;
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(new Color(255,173,0));
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + 3, MAP_SPACING + (y * TILE_HEIGHT) + 3,
                    TILE_WIDTH - 6, TILE_HEIGHT - 6);
        }

        @Override
        public void onMoveTo(Player player) {
            if (item != null) {
                player.inventory.add(item);
            }
            player.addMoney(money);
            deleted = true;
        }

        public ItemDrop copy() {
            ItemDrop copy = new ItemDrop(money);
            if (item != null)
                copy.item = item.copy();
            return copy;
        }
    }
}
