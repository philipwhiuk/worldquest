package com.whiuk.philip.worldquest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.whiuk.philip.worldquest.MapConstants.*;

public class WorldQuest extends JFrame implements KeyListener, MouseListener {

    private static final HashMap<Character, Action> keymap = new HashMap<>();

    public void npcAttacked(NPC npc) {
        if (npc.isDead()) {
            player.addExperience(npc.experience);
            map[npc.x][npc.y].objects.add(npc.dropItem());
            eventHistory.add("Killed a "+npc.type.name);
        }

    }

    public void spawn(GObjects.GameObject object, int x, int y) {
        map[x][y].objects.add(object);
    }

    enum Action {
        NORTH, EAST, SOUTH, WEST, STAIRS,
        USE_0, EQUIP_0, DROP_0,
        USE_1, EQUIP_1, DROP_1,
        USE_2, EQUIP_2, DROP_2, EXIT, NEW_GAME
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


    private Rectangle INVENTORY_USE_0 = new Rectangle(
            433,227, 15, 15);
    private Rectangle INVENTORY_EQUIP_0 = new Rectangle(
            450,227, 15, 15);
    private Rectangle INVENTORY_DROP_0 = new Rectangle(
            467,227, 15, 15);
    private Rectangle INVENTORY_USE_1 = new Rectangle(
            433,247, 15, 15);
    private Rectangle INVENTORY_EQUIP_1 = new Rectangle(
            450,247, 15, 15);
    private Rectangle INVENTORY_DROP_1 = new Rectangle(
            467,247, 15, 15);
    private Rectangle INVENTORY_USE_2 = new Rectangle(
            433,267, 15, 15);
    private Rectangle INVENTORY_EQUIP_2 = new Rectangle(
            450,267, 15, 15);
    private Rectangle INVENTORY_DROP_2 = new Rectangle(
            467,267, 15, 15);


    static class Item {
        static Item parseItem(String itemData) {
            String[] data = itemData.split(",");
            return new Item(data[0], Boolean.parseBoolean(data[1]));
        }

        final String name;
        final boolean useable;

        Item(String name, boolean useable) {
            this.name = name;
            this.useable = useable;
        }

        public Item copy() {
            return new Item(this.name, this.useable);
        }

        public boolean canUse() {
            return useable;
        }

        public boolean canEquip() {
            return false;
        }

        public String print() {
            return this.name+","+useable;
        }
    }

    enum Slot { CHEST }

    static class Armour extends Item {
        private final Slot slot;

        static Armour parseItem(String itemData) {
            String[] itemDataFields = itemData.split(",");
            return new Armour(itemDataFields[0], Boolean.parseBoolean(itemDataFields[1]), Slot.valueOf(itemDataFields[2]));
        }

        Armour(String name, boolean useable, Slot slot) {
            super(name, useable);
            this.slot = slot;
        }

        @Override
        public Armour copy() {
            return new Armour(name, useable, slot);
        }

        @Override
        public boolean canEquip() {
            return true;
        }

        @Override
        public String print() {
            return super.print()+","+slot;
        }
    }

    static class Weapon extends Item {
        public final int damage;

        static Weapon parseItem(String itemData) {
            String[] itemDataFields = itemData.split(",");
            return new Weapon(itemDataFields[0], Boolean.parseBoolean(itemDataFields[1]), Integer.parseInt(itemDataFields[2]));
        }

        Weapon(String name, boolean useable, int damage) {
            super(name, useable);
            this.damage = damage;
        }

        @Override
        public Weapon copy() {
            return new Weapon(this.name, this.useable, damage);
        }

        @Override
        public boolean canEquip() {
            return true;
        }

        @Override
        public String print() {
            return super.print()+","+damage;
        }
    }

    static class Hatchet extends Weapon {

        static Hatchet parseItem(String itemData) {
            String[] itemDataFields = itemData.split(",");
            return new Hatchet(itemDataFields[0], Boolean.parseBoolean(itemDataFields[1]), Integer.parseInt(itemDataFields[2]));
        }

        Hatchet(String name, boolean useable, int damage) {
            super(name, useable, damage);
        }

        @Override
        public Hatchet copy() {
            return new Hatchet(this.name, this.useable, damage);
        }

        @Override
        public String print() {
            return super.print();
        }
    }

    private Random random = new Random();

    private Map<String, TileType> tileTypes = new HashMap<>();
    private Map<String, NPCType> npcTypes = new HashMap<>();
    private Map<String, ItemAction> itemUses = new HashMap<>();
    private Map<String, GObjects.GameObjectBuilder> gameObjectBuilders = new HashMap<>();
    private Tile[][] map;
    private String mapName;
    private List<NPC> npcs;
    private Player player;
    private List<String> eventHistory;
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
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                String[] options = {"Save", "Quit", "Cancel"};
                int option = JOptionPane.showOptionDialog(
                        WorldQuest.this,
                        "Save before quitting?",
                        "Save?",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[0]);
                switch (option) {
                    case 0:
                        saveGame();
                        System.exit(0);
                    case 1:
                        System.exit(0);
                    case 2:
                }
            }
        });
        renderQueue.scheduleAtFixedRate(canvas, 0, 16, TimeUnit.MILLISECONDS);
        setVisible(true);
        addKeyListener(this);
        addMouseListener(this);
        new Thread(() -> {
            try {
                loadScenario();
                if (new File("saves/save.dat").exists()) {
                    loadGame();
                } else {
                    newGame();
                }
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
        loadItemUses();
        gameObjectBuilders = GObjects.provideBuilders();
    }

    private void newGame() {
        loadMap("map");
        this.player = PlayerProvider.createPlayer();
        eventHistory = new ArrayList<>();
        gameStarted = true;
        gameRunning = true;
    }

    private void loadGame() {
        //TODO: Multiple save files
        String savePathname = "saves"+File.separator+"save.dat";
        File saveFile = new File(savePathname);
        if (!saveFile.exists()) {
            throw new RuntimeException("Unable to load save data: Save data file not found");
        }
        if (!saveFile.canRead()) {
            throw new RuntimeException("Unable to read save file");
        }
        try(
                InputStream mapDataStream = new FileInputStream(saveFile);
                BufferedReader buffer = new BufferedReader(new InputStreamReader(mapDataStream))) {
            String mapName = buffer.readLine();
            loadMap(mapName);
            this.player = PlayerProvider.loadPlayer(buffer);
            int eventHistoryItems = Integer.parseInt(buffer.readLine());
            List<String> eventHistory = new ArrayList<>();
            for (int i = 0; i < eventHistoryItems; i++) {
                eventHistory.add(buffer.readLine());
            }
            this.eventHistory = eventHistory;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load map.dat data: " + e.getMessage(), e);
        }
        gameStarted = true;
        gameRunning = true;
    }

    private void saveGame() {
        String savePathname = "saves"+File.separator+"save.dat";
        File saveFile = new File(savePathname);
        try {
            Files.createDirectories(Paths.get(saveFile.getParent()));
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to create save file: " + e.getMessage(), e);
        }
        if (saveFile.exists() && !saveFile.canWrite()) {
            throw new RuntimeException("Unable to update save file");
        }
        try(
                OutputStream mapDataStream = new FileOutputStream(saveFile);
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(mapDataStream))) {
            buffer.write(mapName);
            buffer.newLine();
            PlayerProvider.savePlayer(buffer, player);
            buffer.write(""+eventHistory.size());
            buffer.newLine();
            for (String line : eventHistory) {
                buffer.write(line);
                buffer.newLine();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to save: " + e.getMessage(), e);
        }
    }

    private void loadTileTypes() {
        //TODO: At some point this will come from a file too, but right now there's not much to load.
        tileTypes.put("Grass", GameData.Grass);
        tileTypes.put("Wall", GameData.Wall);
        tileTypes.put("Floor", GameData.Floor);
        tileTypes.put("Door", GameData.Door);
        npcTypes.put("Goblin", GameData.Goblin);
    }

    private void loadItemUses() {
        //TODO: At some point this will come from a file too, but right now there's not much to load.
        itemUses.put("Steel & flint,Oak logs", GameData.Firemaking);
    }

    private void loadMap(String mapResourceName) {
        //TODO: Map format not very efficient but easy to read
        String mapPathname = "maps"+File.separator+mapResourceName+".dat";
        File mapFile = new File(mapPathname);
        if (!mapFile.exists()) {
            throw new RuntimeException(
                    "Unable to load map data: Map data file not found: " + mapFile.getAbsolutePath());
        }
        if (!mapFile.canRead()) {
            throw new RuntimeException("Unable to read map file");
        }
        try(
                InputStream mapDataStream = new FileInputStream(mapFile);
                BufferedReader buffer = new BufferedReader(new InputStreamReader(mapDataStream))) {
            Tile[][] newMap = MapTileLoader.loadMapTiles(tileTypes, buffer);
            List<NPC> npcs = NPCLoader.loadNPCs(npcTypes, buffer);
            GameObjectLoader.loadGameObjects(gameObjectBuilders, buffer, newMap);
            this.npcs = npcs;
            this.map = newMap;
            this.mapName = mapResourceName;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load map data: " + e.getMessage(), e);
        }
    }

    private class WorldQuestCanvas extends JPanel implements Runnable {

        WorldQuestCanvas() {
            Dimension size = new Dimension (PANEL_WIDTH, PANEL_HEIGHT);
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
                                for (GObjects.GameObject object : map[x][y].objects) {
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
                listItem(g, player.inventory.get(i), y, i);
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

        private void listItem(Graphics2D g, Item item, int y, int index) {
            if (item.canUse()) {
                g.setColor(player.itemBeingUsed == index ? Color.BLUE : Color.DARK_GRAY);
                g.fillRoundRect(433, y - 13, 15, 14, 2, 2);
                g.setColor(player.itemBeingUsed == index ? Color.DARK_GRAY : Color.BLUE);
                g.drawRoundRect(433, y - 13, 15, 14, 2, 2);
                g.drawString("o", 437, y - 3);
            }

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
        if (INVENTORY_USE_0.contains(e.getPoint()) && player.inventory.size() >= 1) {
            action = Action.USE_0;
        } else if (INVENTORY_USE_1.contains(e.getPoint()) && player.inventory.size() >= 2) {
            action = Action.USE_1;
        } else if (INVENTORY_USE_2.contains(e.getPoint()) && player.inventory.size() >= 3) {
            action = Action.USE_2;
        } else if (INVENTORY_EQUIP_0.contains(e.getPoint()) && player.inventory.size() >= 1) {
            action = Action.EQUIP_0;
        } else if (INVENTORY_EQUIP_1.contains(e.getPoint()) && player.inventory.size() >= 2) {
            action = Action.EQUIP_1;
        } else if (INVENTORY_EQUIP_2.contains(e.getPoint()) && player.inventory.size() >= 3) {
            action = Action.EQUIP_2;
        } else if (INVENTORY_DROP_0.contains(e.getPoint()) && player.inventory.size() >= 1) {
            action = Action.DROP_0;
        } else if (INVENTORY_DROP_1.contains(e.getPoint()) && player.inventory.size() >= 2) {
            action = Action.DROP_1;
        } else if (INVENTORY_DROP_2.contains(e.getPoint()) && player.inventory.size() >= 3) {
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
                north(player);
                shouldTick = true;
                break;
            case EAST:
                east(player);
                shouldTick = true;
                break;
            case SOUTH:
                south(player);
                shouldTick = true;
                break;
            case WEST:
                west(player);
                shouldTick = true;
                break;
            case STAIRS:
                for (GObjects.GameObject object : map[player.x][player.y].objects) {
                    if (object instanceof GObjects.Stairs) {
                        ((GObjects.Stairs) object).go(this);
                    }
                }
                break;
            case USE_0:
                shouldTick = useItem(0);
                break;
            case USE_1:
                shouldTick = useItem(1);
                break;
            case USE_2:
                shouldTick = useItem(2);
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

    void north(GameCharacter subject) {
        if (inBounds(subject.x, subject.y - 1)) {
            directionAction(subject, subject.x, subject.y - 1);
        }
    }

    void south(GameCharacter subject) {
        if (inBounds(subject.x, subject.y + 1)) {
            directionAction(subject, subject.x, subject.y + 1);
        }
    }

    void east(GameCharacter subject) {
        if (inBounds(subject.x + 1, subject.y)) {
            directionAction(subject, subject.x + 1, subject.y);
        }
    }

    void west(GameCharacter subject) {
        if (inBounds(subject.x - 1, subject.y)) {
            directionAction(subject,subject.x - 1, subject.y);
        }
    }

    boolean inBounds(int x, int y) {
        return x >= 0 && x <= MAX_X && y >= 0 && y <= MAX_Y;
    }

    private void directionAction(GameCharacter subject, int x, int y) {
        for (NPC npc : npcs) {
            if (npc.x == x && npc.y == y && npc != subject) {
                subject.actionOnNpc(this, npc);
                return;
            }
        }
        if (player.x == x && player.y == y && player != subject) {
            return;
        }
        if (map[x][y].canMoveTo()) {
            if (player == subject) {
                map[x][y].onMoveTo(player);
            }
            subject.x = x;
            subject.y = y;
        } else if (map[x][y].objects.size() > 0) {
            map[x][y].objects.get(0).doAction(player);
        }
    }

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

    private boolean useItem(int index) {
        if (player.itemBeingUsed == index) {
            player.itemBeingUsed = -1;
            return false;
        } else if (player.itemBeingUsed == -1) {
            System.out.println("Using item: " + player.inventory.get(index).name);
            player.itemBeingUsed = index;
            return false;
        } else {
            int firstItem = player.itemBeingUsed;
            player.itemBeingUsed = -1;
            return useItems(firstItem, index);
        }
    }

    private boolean useItems(int firstItemIndex, int secondItemIndex) {
        System.out.println("Using items: " + player.inventory.get(firstItemIndex).name + " with " + player.inventory.get(secondItemIndex).name);
        ItemAction action = itemUses.get(player.inventory.get(firstItemIndex).name+","+player.inventory.get(secondItemIndex).name);
        if (action != null) {
            action.perform(this, player, firstItemIndex, secondItemIndex);
            return true;
        } else {
            action = itemUses.get(player.inventory.get(secondItemIndex).name+","+player.inventory.get(firstItemIndex).name);
            if (action != null) {
                action.perform(this, player, secondItemIndex, firstItemIndex);
                return true;
            }
        }
        return false;
    }

    private void dropItem(int index) {
        map[player.x][player.y].objects.add(new GObjects.ItemDrop(player.inventory.remove(index)));
    }

    private void tick(int initialPlayerHealth) {
        for (NPC npc: npcs) {
            tickNPC(npc);
        }
        npcs.removeIf(GameCharacter::isDead);
        for (Tile[] tiles: map) {
            for(Tile tile: tiles) {
                tile.objects.forEach(GObjects.GameObject::tick);
            }
        }
        if (player.isDead()) {
            gameOver();
        } else if (player.health == initialPlayerHealth && player.health < player.maxHealth) {
            player.health += 1;
        }
    }

    void tickNPC(NPC npc) {
        if (nextToPlayer(npc)) {
            attackPlayer(npc);
        } else if(random.nextBoolean()) {
            move(npc);
        }
    }

    void attackPlayer(NPC npc) {
        npc.attack(player);
        if (player.isDead()) {
            eventHistory.add("Killed by a "+npc.type.name);
        }
    }

    private boolean nextToPlayer(NPC npc) {
        return
                (player.x == npc.x && (player.y-1 == npc.y || player.y+1 == npc.y)) ||
                        (player.y == npc.y && (player.x-1 == npc.x || player.x+1 == npc.x));
    }

    private void move(NPC npc) {
        switch(random.nextInt(4)) {
            case 0:
                north(npc);
                break;
            case 1:
                east(npc);
                break;
            case 2:
                west(npc);
                break;
            case 3:
                south(npc);
                break;
        }
    }

    private void gameOver() {
        gameRunning = false;
    }

    public void switchMap(String map, int startX, int startY) {
        loadMap(map);
        player.x = startX;
        player.y = startY;
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
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
}