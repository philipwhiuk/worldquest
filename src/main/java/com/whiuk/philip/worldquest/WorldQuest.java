package com.whiuk.philip.worldquest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WorldQuest extends JFrame implements KeyListener, MouseListener {

    private static final int MAP_WIDTH = 40;
    private static final int MAP_HEIGHT = 40;
    private static final int MAX_X = MAP_WIDTH - 1;
    private static final int MAX_Y = MAP_HEIGHT - 1;
    private static final int TILE_WIDTH = 10;
    private static final int TILE_HEIGHT = 10;
    private static final int MAP_SPACING = 10;
    private static final int BORDER_WIDTH = (MAP_WIDTH * TILE_WIDTH) + 1;
    private static final int BORDER_HEIGHT = (MAP_HEIGHT * TILE_HEIGHT) + 1;
    private static final int DIFFICULTY = 1;
    private static final HashMap<Character, Action> keymap = new HashMap<>();

    enum Action {
        NORTH, EAST, SOUTH, WEST, STAIRS, EQUIP_0, DROP_0,
        EQUIP_1, DROP_1, EQUIP_2, DROP_2, EXIT, NEW_GAME
    }

    static {
        keymap.put('w', Action.NORTH);
        keymap.put('W', Action.NORTH);
        keymap.put('a', Action.WEST);
        keymap.put('A', Action.WEST);
        keymap.put('s', Action.SOUTH);
        keymap.put('S', Action.SOUTH);
        keymap.put('d', Action.EAST);
        keymap.put('D', Action.EAST);
        keymap.put('<', Action.STAIRS);
        keymap.put('n', Action.EXIT);
        keymap.put('N', Action.EXIT);
        keymap.put('y', Action.NEW_GAME);
        keymap.put('Y', Action.NEW_GAME);
    }

    private Rectangle INVENTORY_EQUIP_0 = new Rectangle(
            450,227, 15, 15);
    private Rectangle INVENTORY_DROP_0 = new Rectangle(
            467,227, 15, 15);
    private Rectangle INVENTORY_EQUIP_1 = new Rectangle(
            450,247, 15, 15);
    private Rectangle INVENTORY_DROP_1 = new Rectangle(
            467,247, 15, 15);
    private Rectangle INVENTORY_EQUIP_2 = new Rectangle(
            450,267, 15, 15);
    private Rectangle INVENTORY_DROP_2 = new Rectangle(
            467,267, 15, 15);

    private void equipItem(int index) {
        Item i = player.inventory.get(index);
        if (i instanceof Weapon) {
            Weapon w = (Weapon) i;
            if (player.mainHandWeapon != null) {
                player.inventory.add(player.mainHandWeapon);
                player.mainHandWeapon = null;
            }
            player.inventory.remove(w);
            player.mainHandWeapon = w;
        } else if (i instanceof Armour) {
            Armour a = (Armour) i;
            Armour removed = player.armour.put(a.slot, a);
            if (removed != null) {
                player.inventory.add(removed);
            }
            player.inventory.remove(a);
        }
    }

    private void dropItem(int index) {
        map[player.x][player.y].objects.add(new ItemDrop(player.inventory.remove(index)));
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private class Tile {
        private final TileType type;
        private final List<GameObject> objects;
        private boolean hasSeen = false;

        private Tile(TileType type) {
            this.type = type;
            objects = new ArrayList<>();
        }

        private boolean canMoveTo() {
            if (type.canMoveTo()) {
                boolean canMove = true;
                for (GameObject obj : objects) {
                    if (!obj.canMoveTo()) {
                        canMove = false;
                    }
                }
                return canMove;
            }
            return false;
        }

        public Color getColor(boolean isVisible) {
            if (isVisible) {
                hasSeen = true;
                return type.color;
            } else if (hasSeen) {
                return type.fowColor;
            } else {
                return Color.BLACK;
            }
        }

        public void onMoveTo() {
            objects.forEach(GameObject::onMoveTo);
            objects.removeIf(GameObject::isDeleted);
        }
    }

    private class TileType {
        private final Color color;
        private final Color fowColor;
        private final boolean canMoveTo;

        private TileType(Color color, Color fowColor, boolean canMoveTo) {
            this.color = color;
            this.fowColor = fowColor;
            this.canMoveTo = canMoveTo;
        }

        public boolean canMoveTo() {
            return canMoveTo;
        }
    }

    private class Player extends GameCharacter {
        private HashMap<Slot, Armour> armour;
        private Weapon mainHandWeapon;
        private List<Item> inventory;
        private int money;
        private int baseDamage;
        private int experience;

        private Player() {
            this.color = Color.YELLOW;
            this.maxHealth = 10;
            this.health = 10;
            this.baseDamage = 3;
            this.armour = new HashMap<>();
            this.inventory = new ArrayList<>();
        }

        void actionOnNpc(NPC npc) {
            if (npc.canFight()) {
                attackNpc(npc);
            }
        }

        @Override
        void attackNpc(NPC npc) {
            super.attackNpc(npc);
            if (npc.isDead()) {
                addExperience(npc.experience);
                map[npc.x][npc.y].objects.add(npc.dropItem());
                eventHistory.add("Killed a "+npc.type.name);
            }
        }

        @Override
        int calculateDamage() {
            return baseDamage + (mainHandWeapon != null ? mainHandWeapon.damage : 0);
        }
    }

    private class NPCType {
        private final String name;
        private final Color color;
        public final boolean canFight;
        public int health;
        public int damage;
        public ItemDrop[] dropTable;

        private NPCType(String name, Color color, boolean canFight, int health, int damage, ItemDrop[] dropTable) {
            this.name = name;
            this.color = color;
            this.canFight = canFight;
            this.health = health;
            this.damage = damage;
            this.dropTable = dropTable;
        }
    }

    private class NPC extends GameCharacter {
        private final NPCType type;
        private final int experience = 10;

        private NPC(NPCType type, int x, int y) {
            this.type = type;
            this.color = type.color;
            this.x = x;
            this.y = y;
            this.health = type.health;
        }

        private boolean canFight() {
            return type.canFight;
        }

        private void tick() {
            if (nextToPlayer()) {
                attackPlayer();
            } else if(random.nextBoolean()) {
               move();
            }
        }

        void attackPlayer() {
            attack(player);
            if (player.isDead()) {
                eventHistory.add("Killed by a "+this.type.name);
            }
        }

        private boolean nextToPlayer() {
            return
                (player.x == x && (player.y-1 == y || player.y+1 == y)) ||
                (player.y == y && (player.x-1 == x || player.x+1 == x));
        }

        private void move() {
            switch(random.nextInt(4)) {
                case 0:
                    north();
                    break;
                case 1:
                    east();
                    break;
                case 2:
                    west();
                    break;
                case 3:
                    south();
                    break;
            }
        }

        @Override
        void actionOnNpc(NPC npc) {
            //TODO: NPC->NPC interaction
        }

        @Override
        int calculateDamage() {
            return type.damage;
        }

        public ItemDrop dropItem() {
            return type.dropTable[random.nextInt(type.dropTable.length)].copy();
        }
    }

    abstract class GameCharacter {
        Color color;
        int x;
        int y;
        int maxHealth;
        int health;

        void north() {
            if (inBounds(x, y - 1)) {
                directionAction(x, y - 1);
            }
        }

        void south() {
            if (inBounds(x, y + 1)) {
                directionAction(x, y + 1);
            }
        }

        void east() {
            if (inBounds(x + 1, y)) {
                directionAction(x + 1, y);
            }
        }

        void west() {
            if (inBounds(x - 1, y)) {
                directionAction(x - 1, y);
            }
        }

        private void directionAction(int x, int y) {
            for (NPC npc : npcs) {
                if (npc.x == x && npc.y == y && npc != this) {
                    actionOnNpc(npc);
                    return;
                }
            }
            if (player.x == x && player.y == y && player != this) {
                return;
            }
            if (map[x][y].canMoveTo()) {
                if (player == this) {
                    map[x][y].onMoveTo();
                }
                this.x = x;
                this.y = y;
            } else if (map[x][y].objects.size() > 0) {
                map[x][y].objects.get(0).doAction();
            }
        }

        abstract void actionOnNpc(NPC npc);

        void attackNpc(NPC npc) {
            attack(npc);
        }

        void attack(GameCharacter c) {
            if (random.nextBoolean()) {
                c.takeHit(this.calculateDamage());
            }
        }

        abstract int calculateDamage();

        void takeHit(int damage) {
            if (health < damage) {
                health = 0;
            } else {
                health -= damage;
            }
        }

        boolean isDead() {
            return health <= 0;
        }
    }

    abstract class GameObject {
        boolean deleted = false;

        void tick() {}

        abstract void draw(Graphics2D g, int x, int y);

        public boolean canMoveTo() {
             return true;
        }

        public void onMoveTo() {}

        public boolean isDeleted() {
            return deleted;
        }

        public void doAction() {}
    }

    abstract class GameObjectBuilder {
        public abstract GameObject build(String[] arguments);
    }

    class StairsBuilder extends GameObjectBuilder {
        public GameObject build(String[] arguments) {
            return new Stairs(arguments);
        }
    }

    class Stairs extends GameObject {
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

        void go() {
            loadMap(map);
            player.x = startX;
            player.y = startY;
        }

        @Override
        void draw(Graphics2D g, int x, int y) {
            g.setColor(Color.blue);
            g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT / 2);
        }
    }

    class ItemDrop extends GameObject {
        private Item item;
        private int money;

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
        public void onMoveTo() {
            if (item != null) {
                player.inventory.add(item);
            }
            player.money += money;
            deleted = true;
        }

        public ItemDrop copy() {
            ItemDrop copy = new ItemDrop(money);
            if (item != null)
                copy.item = item.copy();
            return copy;
        }
    }

    class Item {
        final String name;
        Item(String name) {
            this.name = name;
        }

        public Item copy() {
            return new Item(this.name);
        }

        public boolean canEquip() {
            return false;
        }
    }

    enum Slot { CHEST }

    class Armour extends Item {
        private final Slot slot;

        Armour(String name, Slot slot) {
            super(name);
            this.slot = slot;
        }

        @Override
        public Armour copy() {
            return new Armour(this.name, slot);
        }

        @Override
        public boolean canEquip() {
            return true;
        }
    }

    class Weapon extends Item {
        public final int damage;

        Weapon(String name, int damage) {
            super(name);
            this.damage = damage;
        }

        @Override
        public Weapon copy() {
            return new Weapon(this.name, damage);
        }

        @Override
        public boolean canEquip() {
            return true;
        }
    }

    class Hatchet extends Weapon {

        Hatchet(String name, int damage) {
            super(name, damage);
        }

        @Override
        public Weapon copy() {
            return new Hatchet(this.name, damage);
        }
    }

    class TreeBuilder extends GameObjectBuilder {
        public GameObject build(String[] arguments) {
            return new Tree();
        }
    }

    class Tree extends GameObject {
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
        public void doAction() {
            if (player.mainHandWeapon instanceof Hatchet) {
                cutDown = true;
                ticksToRegrow = 10;
                player.inventory.add(new Item("Oak logs"));
            }
        }
    }

    private TileType Grass = new TileType(
            new Color(0,100,0),
            new Color(0,40,0),
            true);
    private TileType Door = new TileType(
            new Color(55,27,0),
            new Color(20,15,0),
            true);
    private TileType Floor = new TileType(
            new Color(100,68,8),
            new Color(50,34,4),
            true);
    private TileType Wall = new TileType(
            Color.GRAY,
            Color.DARK_GRAY,
            false);
    private NPCType Goblin = new NPCType(
            "Goblin",
            Color.RED,
            true,
            5,
            2*DIFFICULTY,
            new ItemDrop[]{
                new ItemDrop(new Weapon("Bronze dagger",2)),
                new ItemDrop(new Weapon("Bronze sword",4)),
                new ItemDrop(new Hatchet("Bronze hatchet",3)),
                new ItemDrop(new Armour("Leather tunic", Slot.CHEST)),
                new ItemDrop(new Item("Oak logs")),
                new ItemDrop(5),
                new ItemDrop(10),
            });

    private Random random = new Random();

    private Map<String, TileType> tileTypes = new HashMap<>();
    private Map<String, NPCType> npcTypes = new HashMap<>();
    private Map<String, GameObjectBuilder> gameObjectBuilders = new HashMap<>();
    private Tile[][] map;
    private List<NPC> npcs;
    private Player player;
    private ArrayList<String> eventHistory;
    private boolean gameStarted = false;
    private boolean gameRunning = false;

    public static void main(String[] args) {
        new WorldQuest();
    }

    private WorldQuest() {
        super("WorldQuest v0.0.1");
        setSize(640, 480);
        WorldQuestCanvas canvas = new WorldQuestCanvas();
        ScheduledExecutorService renderQueue = Executors.newSingleThreadScheduledExecutor();
        add(canvas);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        renderQueue.scheduleAtFixedRate(canvas, 0, 16, TimeUnit.MILLISECONDS);
        setVisible(true);
        addKeyListener(this);
        addMouseListener(this);
        new Thread(() -> {
            try {
                loadScenario();
                newGame();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(WorldQuest.this,
                        e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        }).start();
    }

    private void loadScenario() {
        loadTileTypes();
        loadBuilders();
    }

    private void newGame() {
        loadMap("map");
        createPlayer();
        eventHistory = new ArrayList<>();
        gameStarted = true;
        gameRunning = true;
    }

    private void loadTileTypes() {
        //TODO: At some point this will come from a file too, but right now there's not much to load.
        tileTypes.put("Grass", Grass);
        tileTypes.put("Wall", Wall);
        tileTypes.put("Floor", Floor);
        tileTypes.put("Door", Door);
        npcTypes.put("Goblin", Goblin);
    }

    private void loadBuilders() {
        gameObjectBuilders.put("Stairs", new StairsBuilder());
        gameObjectBuilders.put("Tree", new TreeBuilder());
    }

    private void loadMap(String mapResourceName) {
        //TODO: Map format not very efficient but easy to read
        InputStream mapDataStream = getClass().getResourceAsStream(mapResourceName);
        if (mapDataStream == null) {
            throw new RuntimeException("Unable to load map data: Map data file not found");
        }
        try(BufferedReader buffer = new BufferedReader(new InputStreamReader(mapDataStream))) {
            Tile[][] newMap = processMap(buffer);
            List<NPC> npcs = processNPCs(buffer);
            processGameObjects(buffer, newMap);
            this.npcs = npcs;
            this.map = newMap;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load map data: " + e.getMessage(), e);
        }
    }

    private Tile[][] processMap(BufferedReader buffer) throws IOException {
        Tile[][] newMap = new Tile[MAP_WIDTH][MAP_HEIGHT];
        int mapLines = Integer.parseInt(buffer.readLine());
        if (mapLines != MAP_HEIGHT) {
            throw new RuntimeException("Invalid map size");
        }
        for (int y = 0; y < MAP_HEIGHT; y++) {
            String mapLine = buffer.readLine();
            if (mapLine != null) {
                processMapLine(newMap, y, mapLine);
            }
        }
        return newMap;
    }

    private List<NPC> processNPCs(BufferedReader buffer) throws IOException {
        int npcCount = Integer.parseInt(buffer.readLine());
        List<NPC> npcs = new ArrayList<>(npcCount);
        for (int i = 0; i < npcCount; i++) {
            String[] npcData = buffer.readLine().split(",");
            NPCType npcType = npcTypes.get(npcData[0]);
            npcs.add(new NPC(npcType, Integer.parseInt(npcData[1]), Integer.parseInt(npcData[2])));
        }
        return npcs;
    }

    private void processGameObjects(BufferedReader buffer, Tile[][] newMap) throws IOException {
        int gameObjectsCount = Integer.parseInt(buffer.readLine());
        for (int i = 0; i < gameObjectsCount; i++) {
            String[] gameObjectData = buffer.readLine().split(",");

            int x = Integer.parseInt(gameObjectData[0]);
            int y = Integer.parseInt(gameObjectData[1]);
            String[] args = (gameObjectData.length > 3) ? gameObjectData[3].split(":") : new String[]{};
            newMap[x][y].objects.add(gameObjectBuilders.get(gameObjectData[2]).build(args));
        }
    }

    private void processMapLine(Tile[][] map, int y, String mapLine) {
        String[] tileData = mapLine.split(",");
        for (int x = 0; x < tileData.length; x++) {
            //Currently the per tile data is just the tileType.
            String tileTypeName = tileData[x];
            TileType tileType = tileTypes.get(tileTypeName);
            if (tileType == null) {
                throw new RuntimeException("Invalid tile type: " + tileTypeName);
            }
            map[x][y] = new Tile(tileType);
        }
    }

    private void createPlayer() {
        Player player = new Player();
        player.x = 10;
        player.y = 10;
        this.player = player;
    }

    private class WorldQuestCanvas extends JPanel implements Runnable {

        WorldQuestCanvas() {
            Dimension size = new Dimension (640, 480);
            setPreferredSize(size);
            addKeyListener(WorldQuest.this);
            setFocusable(true);
            requestFocus();
        }

        @Override
        public void paintComponent(Graphics gBase) {
            try {
                Graphics2D g = (Graphics2D) gBase;
                g.setColor(Color.black);
                g.fillRect(0, 0, 640, 480);
                if (!gameStarted) {
                    paintLoading(g);
                } else {
                    paintMap(g);
                    paintNPCs(g);
                    paintPlayer(g);
                    paintStats(g);
                    if (!gameRunning) {
                        paintEventHistory(g);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void paintLoading(Graphics2D g) {
            g.setColor(Color.WHITE);
            g.drawRect(9,9, BORDER_WIDTH, BORDER_HEIGHT);
            g.drawString("Loading game", 200, 200);
        }

        private void paintMap(Graphics2D g) {
            g.setColor(Color.WHITE);
            g.drawRect(9,9, BORDER_WIDTH, BORDER_HEIGHT);
            if (map != null) {
                for (int x = 0; x < map.length; x++) {
                    for (int y = 0; y < map[x].length; y++) {
                        if (map[x][y] != null) {
                            g.setColor(map[x][y].getColor(isVisible(x, y)));
                            g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT);
                            if (isVisible(x, y)) {
                                for (GameObject object : map[x][y].objects) {
                                    object.draw(g, x, y);
                                }
                            }
                        }
                    }
                }
            }
        }

        private boolean isVisible(int x, int y) {
            // TODO: Walls should block field of view
            return x >= player.x - 5 && x <= player.x + 5 && y >= player.y - 5 && y <= player.y + 5;
        }

        private void paintEventHistory(Graphics2D g) {
            g.setColor(Color.BLACK);
            g.fillRect(40, 40, 560, 400);
            g.setColor(Color.WHITE);
            g.drawRect(40, 40, 560, 400);
            g.drawString("Events", 60, 60);
            for (int i = 0; i < eventHistory.size(); i++) {
                String event = eventHistory.get(i);
                g.drawString(event, 80, 80+(i*20));
            }
            g.drawString("New Game? Y/N", 60, 360);
        }

        private void paintNPCs(Graphics2D g) {
            npcs.forEach(npc -> {
                if (isVisible(npc.x, npc.y)) {
                    paintCharacter(g, npc);
                }
            });
        }

        private void paintPlayer(Graphics2D g) {
            paintCharacter(g, player);
        }

        private void paintCharacter(Graphics2D g, GameCharacter c) {
            if (c != null) {
                g.setColor(Color.BLACK);
                g.drawOval(MAP_SPACING + (c.x * TILE_WIDTH), MAP_SPACING + (c.y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT);
                g.setColor(c.color);
                g.fillOval(MAP_SPACING + (c.x * TILE_WIDTH), MAP_SPACING + (c.y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT);
            }
        }

        private void paintStats(Graphics2D g) {
            g.setColor(Color.WHITE);
            g.drawString("Stats", 425, 20);
            g.drawString("Experience: " + player.experience, 450, 40);
            g.drawString("Money: " + player.money, 450, 60);
            g.drawString("Health: ", 450, 80);
            g.drawRect(499, 66, 101, 15);
            g.setColor(Color.RED);
            g.fillRect(500, 67, (player.health*10), 14);
            g.setColor(Color.WHITE);
            if (gameStarted && !gameRunning) {
                g.drawString("GAME OVER!", 425, 100);
            }
            String mainHandWeaponName = player.mainHandWeapon != null ? player.mainHandWeapon.name : "";
            g.drawString("Weapon: " + mainHandWeaponName, 425, 140);

            int y = 160;
            g.drawString("Armour:", 425, y);
            y += 20;

            String chestArmourName = player.armour.get(Slot.CHEST) != null ? player.armour.get(Slot.CHEST).name : "";
            g.drawString("Chest: " + chestArmourName, 450, y);
            y += 20;

            g.drawString("Inventory:", 425, y);
            y += 20;

            for (int i = 0; i < player.inventory.size(); i++) {
                listItem(g, player.inventory.get(i), y);
                y += 20;
            }

            y += 20;

            g.drawString("NPCs", 425, y);

            y += 20;

            for (NPC npc : npcs) {
                if (isVisible(npc.x, npc.y)) {
                    g.drawString(npc.type.name + ": " + npc.health + "/" + npc.type.health, 500, y);
                    y+=20;
                }
            }
        }

        private void listItem(Graphics2D g, Item item, int y) {
            if (item.canEquip()) {
                g.setColor(Color.DARK_GRAY);
                g.fillRoundRect(450, y - 13, 15, 14, 2, 2);
                g.setColor(Color.GREEN);
                g.drawRoundRect(450, y - 13, 15, 14, 2, 2);
                g.drawString("+", 453, y - 3);
            }
            g.setColor(Color.DARK_GRAY);
            g.fillRoundRect(467, y - 13, 15, 14, 2, 2);
            g.setColor(Color.RED);
            g.drawRoundRect(467, y - 13, 15, 14, 2, 2);
            g.drawString("-", 470, y - 3);

            g.setColor(Color.WHITE);
            g.drawString(item.name, 500, y);
        }

        @Override
        public void run() {
            repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        Action a = keymap.get(e.getKeyChar());
        if (a != null) {
            processAction(a);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Action action = null;
        if (INVENTORY_EQUIP_0.contains(e.getPoint()) && player.inventory.size() >= 1) {
            action = Action.EQUIP_0;
        }
        if (INVENTORY_EQUIP_1.contains(e.getPoint()) && player.inventory.size() >= 2) {
            action = Action.EQUIP_1;
        }
        if (INVENTORY_EQUIP_2.contains(e.getPoint()) && player.inventory.size() >= 3) {
            action = Action.EQUIP_2;
        }
        if (INVENTORY_DROP_0.contains(e.getPoint()) && player.inventory.size() >= 1) {
            action = Action.DROP_0;
        }
        if (INVENTORY_DROP_1.contains(e.getPoint()) && player.inventory.size() >= 2) {
            action = Action.DROP_1;
        }
        if (INVENTORY_DROP_2.contains(e.getPoint()) && player.inventory.size() >= 3) {
            action = Action.DROP_2;
        }
        if (action != null) {
            processAction(action);
        }
    }

    private void processAction(Action a) {
        if (gameStarted && gameRunning) {
            processActionWhileRunning(a);
        } else if (gameStarted) {
            processActionWhileGameOver(a);
        }
    }

    private void processActionWhileRunning(Action action) {
        int initialPlayerHealth = player.health;
        boolean shouldTick = false;

        switch(action) {
            case NORTH:
                player.north();
                shouldTick = true;
                break;
            case EAST:
                player.east();
                shouldTick = true;
                break;
            case SOUTH:
                player.south();
                shouldTick = true;
                break;
            case WEST:
                player.west();
                shouldTick = true;
                break;
            case STAIRS:
                for (GameObject object : map[player.x][player.y].objects) {
                    if (object instanceof Stairs) {
                        ((Stairs) object).go();
                    }
                }
                break;
            case EQUIP_0:
                equipItem(0);
                break;
            case EQUIP_1:
                equipItem(1);
                break;
            case EQUIP_2:
                equipItem(2);
                break;
            case DROP_0:
                dropItem(0);
                break;
            case DROP_1:
                dropItem(1);
                break;
            case DROP_2:
                dropItem(2);
        }
        if (shouldTick) {
            tick(initialPlayerHealth);
        }
    }

    private void processActionWhileGameOver(Action action) {
        switch (action) {
            case NEW_GAME:
                newGame();
                break;
            case EXIT:
                System.exit(0);
        }
    }

    private void tick(int initialPlayerHealth) {
        for (NPC npc: npcs) {
            npc.tick();
        }
        npcs.removeIf(GameCharacter::isDead);
        for (Tile[] tiles: map) {
            for(Tile tile: tiles) {
                tile.objects.forEach(GameObject::tick);
            }
        }
        if (player.isDead()) {
            gameOver();
        } else if (player.health == initialPlayerHealth && player.health < player.maxHealth) {
            player.health += 1;
        }
    }

    private void gameOver() {
        gameRunning = false;
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x <= MAX_X && y >= 0 && y <= MAX_Y;
    }

    private void addExperience(int points) {
        player.experience += points;
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}