package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import static com.whiuk.philip.worldquest.Direction.EAST;
import static com.whiuk.philip.worldquest.Direction.SOUTH;
import static com.whiuk.philip.worldquest.MapConstants.MAP_SPACING;
import static com.whiuk.philip.worldquest.MapConstants.TILE_HEIGHT;
import static com.whiuk.philip.worldquest.MapConstants.TILE_WIDTH;

public class GObjects {

    static HashMap<String, GameObjectBuilder> provideBuilders() {
        HashMap<String, GameObjectBuilder> gameObjectBuilders = new HashMap<>();
        gameObjectBuilders.put("Stairs", new GObjects.StairsBuilder());
        gameObjectBuilders.put("Tree", new GObjects.TreeBuilder());
        gameObjectBuilders.put("MineralVein", new GObjects.MineralVeinBuilder());
        gameObjectBuilders.put("Furnace", new GObjects.FurnaceBuilder());
        gameObjectBuilders.put("Door", new GObjects.DoorBuilder());
        gameObjectBuilders.put("Anvil", new GObjects.AnvilBuilder());
        gameObjectBuilders.put("Fence", new GObjects.FenceBuilder());
        return gameObjectBuilders;
    }

    abstract static class GameObject {
        boolean deleted = false;

        void tick(Iterator<GameObject> objectIterator) {}

        abstract void draw(Graphics2D g, int x, int y);

        public boolean canMoveTo(Direction directionMoving) {
            return true;
        }

        public void onMoveTo(Player player) {}

        public boolean isDeleted() {
            return deleted;
        }

        public void doAction(WorldQuest game, Player player) {}

        public abstract String asString();

        public abstract String id();
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
        private Direction direction;

        public Stairs(String[] arguments) {
            this(arguments[0], Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]), Direction.valueOf(arguments[3]));
        }

        public Stairs(String map, int startX, int startY, Direction direction) {
            this.map = map;
            this.startX = startX;
            this.startY = startY;
            this.direction = direction;
        }

        void go(WorldQuest worldQuest) {
            worldQuest.switchMap(map, startX, startY);
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            if (direction.equals(Direction.DOWN)) {
                g.setColor(Color.BLACK);
                g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT), TILE_WIDTH, 3);
                g.setColor(Color.DARK_GRAY);
                g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT) + 3, TILE_WIDTH, 3);
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT) + 6, TILE_WIDTH, 4);
            } else if (direction.equals(Direction.UP)) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT), TILE_WIDTH, 3);
                g.setColor(Color.DARK_GRAY);
                g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT) + 3, TILE_WIDTH, 3);
                g.setColor(Color.BLACK);
                g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT) + 6, TILE_WIDTH, 4);
            } else {
                throw new RuntimeException("Unsupported stairs direction:" + direction);
            }
        }

        @Override
        public String asString() {
            return map+","+startX+","+startY+","+direction;
        }

        @Override
        public String id() {
            return "Stairs";
        }
    }

    static class TreeBuilder extends GameObjectBuilder {
        public GameObject build(String[] arguments) {
            return new Tree(arguments);
        }
    }

    static class Tree extends GameObject {
        Item resource;
        boolean cutDown;
        int ticksToRegrow;

        public Tree(String[] arguments) {
            String resourceData = arguments[0].replaceAll("\\|",",");
            this.resource = ItemProvider.parseItem(resourceData);
        }

        @Override
        void tick(Iterator<GameObject> objectIterator) {
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
        public boolean canMoveTo(Direction directionMoving) {
            return cutDown;
        }

        @Override
        public void doAction(WorldQuest game, Player player) {
            if (player.mainHandWeapon instanceof Hatchet && player.inventory.hasSpaceForItem(resource)) {
                cutDown = true;
                ticksToRegrow = 10;
                player.inventory.add(resource.copy());
                player.gainExperience("Woodcutting", 10);
            } else if (!(player.mainHandWeapon instanceof Hatchet)) {
                game.eventMessage("You need to wield a Hatchet to chop down trees");
            } else if (!player.inventory.hasSpaceForItem(resource)) {
                game.eventMessage("No space in your inventory");
            }
        }

        @Override
        public String asString() {
            return ItemProvider.printItem(resource).replaceAll(",","\\|");
        }

        @Override
        public String id() {
            return "Tree";
        }
    }

    static class Fire extends GameObject {

        @Override
        public boolean canMoveTo(Direction directionMoving) {
            return true;
        }

        @Override
        public String asString() {
            return "";
        }

        @Override
        public String id() {
            return "Fire";
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

    static class DoorBuilder extends GameObjectBuilder {
        public GameObject build(String[] arguments) {
            if (arguments.length != 2) {
                throw new IllegalArgumentException("Expected door position and open side: " + Arrays.toString(arguments));
            }
            Direction doorDirection = Direction.valueOf(arguments[0]);
            switch (doorDirection) {
                case SOUTH:
                    return new SouthDoor(SOUTH, Direction.valueOf(arguments[1]));
                case EAST:
                    return new EastDoor(EAST, Direction.valueOf(arguments[1]));
                default:
                    throw new IllegalArgumentException("Unexpected direction for door:" + doorDirection);
            }
        }
    }

    abstract static class Door extends GameObject {
        final Direction direction;
        Direction openDoorSide;
        boolean isOpen = false;
        private int ticksToShut = 0;

        Door(Direction direction) {
            this.direction = direction;
        }

        @Override
        public void doAction(WorldQuest game, Player player) {
            isOpen = true;
            ticksToShut = 30;
        }

        @Override
        void tick(Iterator<GameObject> objectIterator) {
            if (isOpen && ticksToShut <= 1) {
                isOpen = false;
            } else {
                ticksToShut -= 1;
            }
        }

        @Override
        public String asString() {
            if (openDoorSide != null) {
                return direction.name()+","+openDoorSide.name();
            }
            return direction.name()+","+Direction.NORTH.name();
        }

        @Override
        public String id() {
            return "Door";
        }

    }

    static class SouthDoor extends Door {

        SouthDoor(Direction direction, Direction openDoorSide) {
            super(direction);
            this.openDoorSide = openDoorSide;
        }

        @Override
        public boolean canMoveTo(Direction directionMoving) {
            return directionMoving != Direction.NORTH || isOpen;
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(new Color(100,68,8));
            if (isOpen) {
                g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + TILE_WIDTH - 4, MAP_SPACING + (y * TILE_HEIGHT),
                        4, TILE_HEIGHT);
            } else {
                g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT) + TILE_HEIGHT - 4,
                        TILE_WIDTH, 4);
            }
        }
    }

    static class EastDoor extends Door {
        private final Direction openDoorSide;

        EastDoor(Direction direction, Direction openDoorSide) {
            super(direction);
            this.openDoorSide = openDoorSide;
        }

        @Override
        public boolean canMoveTo(Direction directionMoving) {
            return directionMoving != Direction.WEST || isOpen;
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(new Color(100,68,8));
            if (isOpen) {
                if (openDoorSide == Direction.NORTH) {
                    g.fillRect(MAP_SPACING + (x * TILE_WIDTH),
                            MAP_SPACING + (y * TILE_HEIGHT),
                            TILE_WIDTH, 4);
                } else {
                    g.fillRect(MAP_SPACING + (x * TILE_WIDTH),
                            MAP_SPACING + (y * TILE_HEIGHT) + TILE_HEIGHT - 4,
                            TILE_WIDTH, 4);
                }
            } else {
                g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + TILE_WIDTH - 4, MAP_SPACING + (y * TILE_HEIGHT),
                        4, TILE_HEIGHT);
            }
        }
    }

    static class ItemDrop extends GObjects.GameObject {
        private Item item;
        private int money;
        private int ticksToRemove = 100;

        ItemDrop(Item i) {
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
            if (item == null || player.inventory.hasSpaceForItem(item)) {
                if (item != null) {
                    player.inventory.add(item);
                }
                player.addMoney(money);
                deleted = true;
            }
        }

        @Override
        public String asString() {
            return money+","+(item != null ? ItemProvider.printItem(item).replaceAll(",","\\|") : "");
        }

        @Override
        public String id() {
            return "ItemDrop";
        }

        public ItemDrop copy() {
            ItemDrop copy = new ItemDrop(money);
            if (item != null)
                copy.item = item.copy();
            return copy;
        }

        @Override
        void tick(Iterator<GameObject> objectIterator) {
            if (ticksToRemove <= 1) {
                objectIterator.remove();
            } else {
                ticksToRemove -= 1;
            }
        }
    }

    static class MineralVeinBuilder extends GameObjectBuilder {
        public GameObject build(String[] arguments) {
            return new MineralVein(arguments);
        }
    }

    static class MineralVein extends GameObject {
        final Item resource;
        final String cssDef;
        final Color veinColour;

        MineralVein(String[] arguments) {
            String resourceData = arguments[0].replaceAll("\\|",",");
            this.resource = ItemProvider.parseItem(resourceData);
            this.cssDef = arguments[1];
            this.veinColour = fromCSSDef(cssDef);
        }

        private Color fromCSSDef(String value) {
            Integer i = Integer.decode(value);
            return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(veinColour);
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + 3, MAP_SPACING + (y * TILE_HEIGHT) + 3,
                    TILE_WIDTH - 6, TILE_HEIGHT - 6);
        }

        public Item mine() {
            return resource.copy();
        }

        @Override
        public String asString() {
            return ItemProvider.printItem(resource).replaceAll(",","\\|")+","+cssDef;
        }

        @Override
        public String id() {
            return "MineralVein";
        }
    }

    static class FurnaceBuilder extends GameObjectBuilder {
        public GameObject build(String[] arguments) {
            return new Furnace();
        }
    }

    static class Furnace extends GameObject {
        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(Color.BLACK);
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + 1, MAP_SPACING + (y * TILE_HEIGHT) + 1,
                    TILE_WIDTH - 2, TILE_HEIGHT - 2);
            g.setColor(Color.ORANGE);
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + 3, MAP_SPACING + (y * TILE_HEIGHT) + 4,
                    TILE_WIDTH - 6, TILE_HEIGHT - 8);
            g.setColor(Color.RED);
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + 3, MAP_SPACING + (y * TILE_HEIGHT) + 6,
                    TILE_WIDTH - 6, TILE_HEIGHT - 8);
        }

        @Override
        public String asString() {
            return "";
        }

        @Override
        public String id() {
            return "Furnace";
        }
    }

    static class AnvilBuilder extends GameObjectBuilder {
        public GameObject build(String[] arguments) {
            return new Anvil();
        }
    }

    static class Anvil extends GameObject {
        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + 1, MAP_SPACING + (y * TILE_HEIGHT) + 1,
                    TILE_WIDTH - 3, TILE_HEIGHT - 3);
        }

        @Override
        public String asString() {
            return "";
        }

        @Override
        public String id() {
            return "Anvil";
        }
    }

    static class FenceBuilder extends GameObjectBuilder {
        public GameObject build(String[] arguments) {
            if (arguments.length != 1) {
                throw new IllegalArgumentException("Expected fence position only: " + Arrays.toString(arguments));
            }
            Direction fenceDirection = Direction.valueOf(arguments[0]);
            switch (fenceDirection) {
                case NORTH:
                    return new NorthFence();
                case SOUTH:
                    return new SouthFence();
                case EAST:
                    return new EastFence();
                case NORTHEAST:
                    return new NorthEastFence();
                case SOUTHEAST:
                    return new SouthEastFence();
                default:
                    throw new IllegalArgumentException("Unexpected direction for fence:" + fenceDirection);
            }
        }
    }

    abstract static class Fence extends GameObject {

        private final Direction direction;

        Fence(Direction direction) {
            this.direction = direction;
        }

        @Override
        public String asString() {
            return direction.name();
        }

        @Override
        public String id() {
            return "Fence";
        }

    }

    static class NorthFence extends Fence {

        NorthFence() {
            super(Direction.NORTH);
        }

        @Override
        public boolean canMoveTo(Direction directionMoving) {
            return directionMoving != SOUTH && directionMoving != Direction.NORTH;
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(new Color(100,68,8));
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT) + 2,
                    TILE_WIDTH, 2);
        }
    }

    static class SouthFence extends Fence {
        SouthFence() {
            super(Direction.SOUTH);
        }

        @Override
        public boolean canMoveTo(Direction directionMoving) {
            return directionMoving != SOUTH && directionMoving != Direction.NORTH;
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(new Color(100,68,8));
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT) + TILE_HEIGHT - 4,
                    TILE_WIDTH, 2);
        }
    }

    static class EastFence extends Fence {

        EastFence() {
            super(Direction.EAST);
        }

        @Override
        public boolean canMoveTo(Direction directionMoving) {
            return directionMoving != Direction.WEST && directionMoving != EAST;
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(new Color(100,68,8));
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + TILE_WIDTH - 3, MAP_SPACING + (y * TILE_HEIGHT),
                    3, TILE_HEIGHT);
        }
    }

    static class SouthEastFence extends Fence {

        SouthEastFence() {
            super(Direction.SOUTHEAST);
        }

        @Override
        public boolean canMoveTo(Direction directionMoving) {
            return false;
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(new Color(100,68,8));
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT) + TILE_HEIGHT - 4,
                    TILE_WIDTH, 2); //SOUTH
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + TILE_WIDTH - 3, MAP_SPACING + (y * TILE_HEIGHT),
                    3, TILE_HEIGHT); //EAST
        }
    }

    static class NorthEastFence extends Fence {

        NorthEastFence() {
            super(Direction.NORTHEAST);
        }

        @Override
        public boolean canMoveTo(Direction directionMoving) {
            return false;
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(new Color(100,68,8));
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT) + 2,
                    TILE_WIDTH, 2); // NORTH
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH) + TILE_WIDTH - 3, MAP_SPACING + (y * TILE_HEIGHT),
                    3, TILE_HEIGHT); //EAST
        }
    }
}
