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
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import static com.whiuk.philip.worldquest.MapConstants.*;
import static com.whiuk.philip.worldquest.SidebarViewState.*;
import static com.whiuk.philip.worldquest.WorldQuest.GameState.*;

//TODO: Move TODOs to an issue tracker
public class WorldQuest extends JFrame {

    private static final HashMap<Character, Action> keymap = new HashMap<>();
    private static final HashMap<Integer, Action> keyPressMap = new HashMap<>();

    public void changeTileType(Tile tile, TileType type) {
        Tile newTile = new Tile(type, tile.x, tile.y);
        newTile.objects.addAll(tile.objects);
        map[tile.x][tile.y] = newTile;
    }

    enum GameState {
        LAUNCHING, LOADING,
        RUNNING,
        PLAYER_TALKING, NPC_TALKING,
        //TODO: Implement use of conversation options
        @SuppressWarnings("unused") OPTION_SELECTION, SHOP,
        DEAD
    }

    static {
        //TODO: Key config loaded from file + pre-sets
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
        keyPressMap.put(KeyEvent.VK_ENTER, Action.TALK_CONTINUE);
    }

    //TODO: Move this to 'Scenario'
    private Map<String, TileType> tileTypes = new HashMap<>();
    private Map<String, NPCType> npcTypes = new HashMap<>();
    private Map<String, ItemAction> itemUses = new HashMap<>();
    private Map<String, ItemAction> tileItemUses = new HashMap<>();
    private Map<String, GObjects.GameObjectBuilder> gameObjectBuilders = new HashMap<>();

    //TODO: This is the actual gamestate
    private Tile[][] map;
    private String mapName;
    private List<NPC> npcs;
    private List<NPC> visibleNpcs;
    private Player player;
    private List<String> eventHistory;
    GameState gameState = LAUNCHING;
    private SidebarViewState sidebarViewState = SKILLS;
    private NPC talkingTo;
    private Shop currentShop;

    public static void main(String[] args) {
        ExperienceTable.initializeExpTable();
        new WorldQuest();
    }

    private WorldQuest() {
        super("WorldQuest v0.0.1");
        setSize(640, 480);
        WorldQuestKeyListener keyListener = new WorldQuestKeyListener(this);
        WorldQuestMouseListener mouseListener = new WorldQuestMouseListener(this);
        WorldQuestCanvas canvas = new WorldQuestCanvas(keyListener, mouseListener);
        ScheduledExecutorService renderQueue = Executors.newSingleThreadScheduledExecutor();
        getContentPane().add(canvas);
        pack();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WorldQuestWindowAdapter(this));
        renderQueue.scheduleAtFixedRate(canvas, 0, 16, TimeUnit.MILLISECONDS);
        setVisible(true);
        new Thread(() -> {
            try {
                gameState = LOADING;
                loadScenario();
                if (new File("saves/save.dat").exists()) {
                    loadGame();
                } else {
                    newGame();
                }
                continueGame();
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
        loadNpcTypes();
        loadItemUses();
        gameObjectBuilders = GObjects.provideBuilders();
    }

    private void newGame() {
        //TODO: Use map-relative-grid-reference-0,0,0
        loadMap("map");
        this.player = PlayerProvider.createPlayer();
        eventHistory = new ArrayList<>();
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
            throw new RuntimeException("Unable to load saved game: " + e.getMessage(), e);
        }
    }

    private void continueGame() {
        visibleNpcs = npcs.stream().filter(npc -> isVisible(npc.x, npc.y)).collect(Collectors.toList());
        gameState = RUNNING;
    }

    void saveGame() {
        //TODO: Treat map data as stuff to save.
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
        tileTypes.put("Dirt", GameData.Dirt);
    }

    private void loadNpcTypes() {
        npcTypes.put("Goblin", GameData.Goblin);
        npcTypes.put("Shopkeeper", GameData.ShopKeeper);
    }

    private void loadItemUses() {
        //TODO: At some point this will come from a file too, but right now there's not much to load.
        itemUses.put("Steel & flint,Oak logs", GameData.Firemaking);
        tileItemUses.put("Grass,Shovel", GameData.Dig);
    }

    private void loadMap(String mapResourceName) {
        //TODO: Generate new map if map doesn't exist (first load)
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

        WorldQuestCanvas(WorldQuestKeyListener keyListener, WorldQuestMouseListener mouseListener) {
            Dimension size = new Dimension (PANEL_WIDTH, PANEL_HEIGHT);
            setPreferredSize(size);
            addKeyListener(keyListener);
            addMouseListener(mouseListener);
            setFocusable(true);
            requestFocus();
        }

        @Override
        public void paintComponent(Graphics gBase) {
            try {
                Graphics2D g = (Graphics2D) gBase;
                g.setColor(Color.black);
                g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
                if (gameState == LAUNCHING || gameState == LOADING) {
                    paintLoading(g);
                } else {
                    //System.exit(0);
                    if (gameState == RUNNING || gameState == PLAYER_TALKING || gameState == NPC_TALKING) {
                        MapViewPainter.paintMapView(g, WorldQuest.this, map, visibleNpcs, player);
                    }
                    SidebarPainter.paintSidebar(g, sidebarViewState, WorldQuest.this, player, visibleNpcs);
                    if (gameState == PLAYER_TALKING || gameState == NPC_TALKING) {
                        ConversationPainter.paintConversation(g, gameState, talkingTo);
                    } else if (gameState == SHOP) {
                        ShopPainter.paintShop(g, currentShop);
                    } else if (gameState == DEAD) {
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

        @Override
        public void run() {
            repaint();
        }
    }

    private Rectangle sidebarLocation() {
        return new Rectangle(420, 0, 260, 480);
    }

    private Rectangle inventoryButtonLocation(int index, int button) {
        int xOffset = button*17;
        int offset = 20*index;
        return new Rectangle(433+xOffset,155+offset, 15, 15);
    }

    private Rectangle npcButtonTalkLocation(int index) {
        int offset = 20*index;
        return new Rectangle(433,155+offset, 15, 15);
    }

    private Rectangle shopCloseLocation() {
        return new Rectangle(19+BORDER_WIDTH-35,SHOP_Y+10, 15, 15);
    }

    Action getKeyMappedAction(Character c) {
        return keymap.get(c);
    }

    Action getKeypressMappedAction(Integer code) {
        return keyPressMap.get(code);
    }

    Action checkForActionInterception(Point point) {
        if (sidebarLocation().contains(point)) {
            return checkSidebarInterceptions(point);
        } else if (gameState == SHOP) {
            if (shopCloseLocation().contains(point)) {
                return Action.CLOSE_SHOP;
            }
        }
        return null;
    }

    Tile checkForTileInterception(Point p) {
        double x = p.getX();
        double y = p.getY();
        if (x > MAP_SPACING && x < MAP_SPACING+MAP_WIDTH & y > MAP_SPACING & y < MAP_SPACING+MAP_HEIGHT) {
            int tileX = (int) Math.floor((x - MAP_SPACING)/TILE_WIDTH);
            int tileY = (int) Math.floor((y - MAP_SPACING)/TILE_HEIGHT);
            return map[tileX][tileY];
        }
        return null;
    }

    private Action checkSidebarInterceptions(Point p) {
        if (sidebarTabButton(0, 0).contains(p)) {
            return Action.SWITCH_TO_SKILLS;
        } else if (sidebarTabButton(1, 0).contains(p)) {
            return Action.SWITCH_TO_EQUIPMENT;
        } else if (sidebarTabButton(2, 0).contains(p)) {
            return Action.SWITCH_TO_ITEMS;
        } else if (sidebarTabButton(0, 1).contains(p)) {
            return Action.SWITCH_TO_NPCS;
        }

        switch (sidebarViewState) {
            case SKILLS:
                return null;
            case ITEMS:
                if (inventoryButtonLocation(0,0).contains(p) && player.inventory.size() >= 1) {
                    return Action.USE_0;
                } else if (inventoryButtonLocation(1,0).contains(p) && player.inventory.size() >= 2) {
                    return Action.USE_1;
                } else if (inventoryButtonLocation(2,0).contains(p) && player.inventory.size() >= 3) {
                    return Action.USE_2;
                } else if (inventoryButtonLocation(0,1).contains(p) && player.inventory.size() >= 1) {
                    return Action.EQUIP_0;
                } else if (inventoryButtonLocation(1,1).contains(p) && player.inventory.size() >= 2) {
                    return Action.EQUIP_1;
                } else if (inventoryButtonLocation(2,1).contains(p) && player.inventory.size() >= 3) {
                    return Action.EQUIP_2;
                } else if (inventoryButtonLocation(0,2).contains(p) && player.inventory.size() >= 1) {
                    return Action.DROP_0;
                } else if (inventoryButtonLocation(1,2).contains(p) && player.inventory.size() >= 2) {
                    return Action.DROP_1;
                } else if (inventoryButtonLocation(2,2).contains(p) && player.inventory.size() >= 3) {
                    return Action.DROP_2;
                }
                return null;
            case NPCS:
                if (npcButtonTalkLocation(0).contains(p) && visibleNpcs.size() >= 1) {
                    return Action.TALK_0;
                } else if (npcButtonTalkLocation(1).contains(p) && visibleNpcs.size() >= 2) {
                    return Action.TALK_1;
                }
                return null;
        }
        return null;
    }

    private Rectangle sidebarTabButton(int x, int y) {
        return new Rectangle(425+(x*50), 75+(y*30), 45, 30);
    }

    void processAction(Action a) {
        if (a.isSidebarViewAction()) {
            processSidebarViewAction(a);
        }
        if (gameState == RUNNING) {
            processActionWhileRunning(a);
        } else if (gameState == PLAYER_TALKING || gameState == NPC_TALKING) {
            processActionWhileTalking(a);
        } else if (gameState == SHOP) {
            processActionWhileShopping(a);
        } else if (gameState == DEAD) {
            processActionWhileGameOver(a);
        }
    }

    void processTileClick(Tile tile) {
        processPlayerInputWhileRunning(() -> {
            if (player.itemBeingUsed != -1) {
                int item = player.itemBeingUsed;
                player.itemBeingUsed = -1;
                return useItemOnTile(item, tile);
            }
            return false;
        });
    }

    private void processSidebarViewAction(Action a) {
        switch (a) {
            case SWITCH_TO_EQUIPMENT:
                sidebarViewState = EQUIPMENT;
                break;
            case SWITCH_TO_ITEMS:
                sidebarViewState = ITEMS;
                break;
            case SWITCH_TO_NPCS:
                sidebarViewState = NPCS;
                break;
            case SWITCH_TO_SKILLS:
                sidebarViewState = SKILLS;
        }
    }

    private void processActionWhileRunning(Action action) {
        processPlayerInputWhileRunning(() -> {
            switch (action) {
                case NORTH:
                    north(player);
                    return true;
                case EAST:
                    east(player);
                    return true;
                case SOUTH:
                    south(player);
                    return true;
                case WEST:
                    west(player);
                    return true;
                case STAIRS:
                    for (GObjects.GameObject object : map[player.x][player.y].objects) {
                        if (object instanceof GObjects.Stairs) {
                            ((GObjects.Stairs) object).go(this);
                        }
                    }
                    break;
                case USE_0:
                    return useItem(0);
                case USE_1:
                    return useItem(1);
                case USE_2:
                    return useItem(2);
                case EQUIP_0:
                    equipItem(0);
                    return true;
                case EQUIP_1:
                    equipItem(1);
                    return true;
                case EQUIP_2:
                    equipItem(2);
                    return true;
                case DROP_0:
                    dropItem(0);
                    return true;
                case DROP_1:
                    dropItem(1);
                    return true;
                case DROP_2:
                    dropItem(2);
                    return true;
                case TALK_0:
                    talkTo(0);
                    return true;
                case TALK_1:
                    talkTo(1);
                    return true;
                case TALK_2:
                    talkTo(2);
                    return true;
            }
            return false;
        });
    }

    private void processPlayerInputWhileRunning(BooleanSupplier processor) {
        int initialPlayerHealth = player.health;
        boolean shouldTick = processor.getAsBoolean();
        if (shouldTick) {
            tick(initialPlayerHealth);
        }
    }

    private void processActionWhileTalking(Action action) {
        switch(action) {
            case TALK_CONTINUE:
                progressConversation();
                break;
        }
    }

    private void processActionWhileShopping(Action action) {
        switch(action) {
            case CLOSE_SHOP:
                closeShop();
                break;
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

    private void north(GameCharacter subject) {
        if (inBounds(subject.x, subject.y - 1)) {
            directionAction(subject, subject.x, subject.y - 1);
        }
    }

    private void south(GameCharacter subject) {
        if (inBounds(subject.x, subject.y + 1)) {
            directionAction(subject, subject.x, subject.y + 1);
        }
    }

    private void east(GameCharacter subject) {
        if (inBounds(subject.x + 1, subject.y)) {
            directionAction(subject, subject.x + 1, subject.y);
        }
    }

    private void west(GameCharacter subject) {
        if (inBounds(subject.x - 1, subject.y)) {
            directionAction(subject,subject.x - 1, subject.y);
        }
    }

    private boolean inBounds(int x, int y) {
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
            player.itemBeingUsed = index;
            return false;
        } else {
            int firstItem = player.itemBeingUsed;
            player.itemBeingUsed = -1;
            return useItems(firstItem, index);
        }
    }

    private boolean useItems(int firstItemIndex, int secondItemIndex) {
        ItemAction action = itemUses.get(
                player.inventory.get(firstItemIndex).name+","+player.inventory.get(secondItemIndex).name);
        if (action != null) {
            action.perform(this, map[player.x][player.y], player, firstItemIndex, secondItemIndex);
            return true;
        } else {
            action = itemUses.get(
                    player.inventory.get(secondItemIndex).name+","+player.inventory.get(firstItemIndex).name);
            if (action != null) {
                action.perform(this, map[player.x][player.y], player, secondItemIndex, firstItemIndex);
                return true;
            }
        }
        return false;
    }

    private boolean useItemOnTile(int firstItemIndex, Tile tile) {
        String key = tile.type.name+","+player.inventory.get(firstItemIndex).name;
        ItemAction action = tileItemUses.get(key);
        if (action != null) {
            action.perform(this, tile, player, firstItemIndex, -1);
            return true;
        }
        return false;
    }

    private void dropItem(int index) {
        map[player.x][player.y].objects.add(new GObjects.ItemDrop(player.inventory.remove(index)));
    }

    private void talkTo(int index) {
        startConversation(visibleNpcs.get(index));
    }

    private void startConversation(NPC npc) {
        npc.startConversation();
        talkingTo = npc;
        displayConversation();
    }

    private void displayConversation() {
        if (talkingTo.currentConversation.playerText != null) {
            gameState = PLAYER_TALKING;
        } else {
            gameState = NPC_TALKING;
        }
    }

    private void progressConversation() {
        if (gameState == PLAYER_TALKING) {
            gameState = NPC_TALKING;
        } else {
            talkingTo.currentConversation.npcAction.doAction(this, talkingTo);
        }
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
        visibleNpcs = npcs.stream().filter(npc -> isVisible(npc.x, npc.y)).collect(Collectors.toList());
    }

    private void tickNPC(NPC npc) {
        if (nextToPlayer(npc) && npc.canFight() && (npc.isAggressive() || npc.hasBeenAttacked())) {
            attackPlayer(npc);
        } else if(npc.canMove() && RandomSource.getRandom().nextBoolean()) {
            move(npc);
        }
    }

    private void attackPlayer(NPC npc) {
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
        switch(RandomSource.getRandom().nextInt(4)) {
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
        gameState = DEAD;
    }

    void switchMap(String map, int startX, int startY) {
        //TODO: Save current map
        loadMap(map);
        player.x = startX;
        player.y = startY;
    }

    void npcAttacked(NPC npc) {
        if (npc.isDead()) {
            map[npc.x][npc.y].objects.add(npc.dropItem());
            eventHistory.add("Killed a "+npc.type.name);
        }

    }

    void spawn(GObjects.GameObject object, int x, int y) {
        map[x][y].objects.add(object);
    }

    void showOptions(Map<String, ConversationChoice> conversationOptions) {
        //TODO: Show conversation options
    }

    void showShop(Shop shop) {
        currentShop = shop;
        gameState = SHOP;
    }

    private void closeShop() {
        currentShop = null;
        gameState = RUNNING;
    }

    boolean isVisible(int x, int y) {
        // TODO: Walls should block field of view
        boolean isVisible = x >= player.x - 5 && x <= player.x + 5 && y >= player.y - 5 && y <= player.y + 5;
        return isVisible;
    }

}