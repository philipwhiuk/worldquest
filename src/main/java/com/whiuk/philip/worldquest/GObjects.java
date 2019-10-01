package com.whiuk.philip.worldquest;

import org.json.simple.JSONObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.whiuk.philip.worldquest.Direction.EAST;
import static com.whiuk.philip.worldquest.Direction.SOUTH;
import static com.whiuk.philip.worldquest.JsonUtils.intFromObj;
import static com.whiuk.philip.worldquest.MapConstants.MAP_SPACING;
import static com.whiuk.philip.worldquest.MapConstants.TILE_HEIGHT;
import static com.whiuk.philip.worldquest.MapConstants.TILE_WIDTH;

public class GObjects {

    static HashMap<String, GameObjectBuilder> provideBuilders(Map<String, ItemType> itemTypes) {
        HashMap<String, GameObjectBuilder> gameObjectBuilders = new HashMap<>();
        gameObjectBuilders.put("Stairs", new GObjects.StairsBuilder());
        gameObjectBuilders.put("Tree", new GObjects.TreeBuilder());
        gameObjectBuilders.put("ResourceProvider", new GObjects.ResourceProviderBuilder());
        gameObjectBuilders.put("Furnace", new GObjects.FurnaceBuilder());
        gameObjectBuilders.put("Door", new GObjects.DoorBuilder());
        gameObjectBuilders.put("Anvil", new GObjects.AnvilBuilder());
        gameObjectBuilders.put("Fence", new GObjects.FenceBuilder());
        gameObjectBuilders.put("ItemDrop", new GObjects.ItemDropBuilder(itemTypes));
        gameObjectBuilders.put("Fire", new GObjects.FireBuilder());
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

        public abstract String id();
    }

    abstract static class GameObjectBuilder {
        public abstract GameObject build(JSONObject data);
    }

    static class StairsBuilder extends GameObjectBuilder {
        public GameObject build(JSONObject data) {
            return new Stairs((String) data.get("map"),
                    intFromObj(data.get("startX")),
                    intFromObj(data.get("startY")),
                    Direction.valueOf((String) data.get("direction")));
        }
    }

    static class Stairs extends GameObject {
        private String map;
        private int startX;
        private int startY;
        private Direction direction;

        public Stairs(String map, int startX, int startY, Direction direction) {
            this.map = map;
            this.startX = startX;
            this.startY = startY;
            this.direction = direction;
        }

        void go(WorldQuest worldQuest) {
            worldQuest.switchGameMap(map, startX, startY);
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
        public String id() {
            return "Stairs";
        }
    }

    static class TreeBuilder extends GameObjectBuilder {
        public GameObject build(JSONObject data) {
            return new Tree((String) data.get("resource"));
        }
    }

    static class Tree extends GameObject {
        String resource;
        boolean cutDown;
        int ticksToRegrow;

        public Tree(String resource) {
            this.resource = resource;
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
            //TODO: Quality
            Item item = game.scenarioData.itemType(resource).create();
            if (player.mainHandWeapon instanceof Hatchet && player.inventory.hasSpaceForItem(item)) {
                cutDown = true;
                ticksToRegrow = 10;
                player.inventory.add(item);
                player.gainExperience("Woodcutting", 10);
            } else if (!(player.mainHandWeapon instanceof Hatchet)) {
                game.eventMessage("You need to wield a Hatchet to chop down trees");
            } else if (!player.inventory.hasSpaceForItem(item)) {
                game.eventMessage("No space in your inventory");
            }
        }

        @Override
        public String id() {
            return "Tree";
        }
    }

    static class FireBuilder extends GameObjectBuilder {
        public GameObject build(JSONObject data) {
            return new Fire();
        }
    }

    static class Fire extends GameObject {

        @Override
        public boolean canMoveTo(Direction directionMoving) {
            return true;
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
        public GameObject build(JSONObject data) {
            Direction doorDirection = Direction.valueOf((String) data.get("direction"));
            switch (doorDirection) {
                case SOUTH:
                    return new SouthDoor(SOUTH, Direction.valueOf((String) data.get("openSide")));
                case EAST:
                    return new EastDoor(EAST, Direction.valueOf((String) data.get("openSide")));
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

    static class ItemDropBuilder extends GameObjectBuilder {
        private Map<String, ItemType> itemTypesStore;

        ItemDropBuilder(Map<String, ItemType> itemTypesStore) {
            this.itemTypesStore = itemTypesStore;
        }

        public GameObject build(JSONObject data) {
            if (data.containsKey("item")) {
                //noinspection RedundantCast
                return new ItemDrop(itemTypesStore.get((String) data.get("item")).create());
            } else {
                //TODO: Don't create
                return new ItemDrop(intFromObj(data.get("money")));
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

        private ItemDrop(Item i, int money) {
            this.item = i;
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
        public String id() {
            return "ItemDrop";
        }

        @Override
        void tick(Iterator<GameObject> objectIterator) {
            if (ticksToRemove <= 1) {
                objectIterator.remove();
            } else {
                ticksToRemove -= 1;
            }
        }

        public ItemDrop spawn() {
            return new ItemDrop(this.item, this.money);
        }
    }

    static class ResourceProviderBuilder extends GameObjectBuilder {
        public GameObject build(JSONObject data) {
            return new ResourceProvider((String) data.get("name"), (String) data.get("resource"), (String) data.get("cssDef"));
        }
    }

    static class ResourceProvider extends GameObject {
        final String name;
        final String resource;
        final String cssDef;
        final Color veinColour;

        ResourceProvider(String name, String resource, String cssDef) {
            this.name = name;
            this.resource = resource;
            this.cssDef = cssDef;
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

        public Item extract(WorldQuest game) {
            //TODO: Quality
            return game.scenarioData.itemType(resource).create();
        }

        @Override
        public String id() {
            return "ResourceProvider";
        }
    }

    static class FurnaceBuilder extends GameObjectBuilder {
        public GameObject build(JSONObject data) {
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
        public String id() {
            return "Furnace";
        }
    }

    static class AnvilBuilder extends GameObjectBuilder {
        public GameObject build(JSONObject data) {
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
        public String id() {
            return "Anvil";
        }
    }

    static class FenceBuilder extends GameObjectBuilder {
        public GameObject build(JSONObject data) {
            Direction fenceDirection = Direction.valueOf((String) data.get("side"));
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
