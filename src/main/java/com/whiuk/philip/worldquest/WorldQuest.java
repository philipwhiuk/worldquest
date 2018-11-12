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
import static com.whiuk.philip.worldquest.WorldQuest.GameState.*;
import static com.whiuk.philip.worldquest.WorldQuest.MessageState.*;

public class WorldQuest extends JFrame {

    private static final HashMap<Character, Action> keymap = new HashMap<>();
    private static final HashMap<Integer, Action> keyPressMap = new HashMap<>();

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
        keymap.put('y', Action.START_NEW_GAME);
        keymap.put('Y', Action.START_NEW_GAME);
        keymap.put('1', Action.CONVERSATION_OPTION);
        keymap.put('2', Action.CONVERSATION_OPTION);
        keymap.put('3', Action.CONVERSATION_OPTION);
        keyPressMap.put(KeyEvent.VK_ENTER, Action.TALK_CONTINUE);
    }

    private Map<String, TileType> tileTypes = new HashMap<>();
    private Map<String, NPCType> npcTypes = new HashMap<>();
    private Map<String, ItemAction> itemUses = new HashMap<>();
    private Map<String, ItemAction> tileItemUses = new HashMap<>();
    private Map<String, GObjects.GameObjectBuilder> gameObjectBuilders = new HashMap<>();

    private GameUI gameUI;
    private LoadingScreen loadingScreen;
    private GameScreen gameScreen;
    private SidebarUI sidebar;
    private MessageDisplay messages;
    private MessageState messageState;
    private Tile[][] map;
    private String mapName;
    private List<NPC> npcs;
    List<NPC> visibleNpcs;
    Player player;
    private List<String> eventHistory;
    GameState gameState = LAUNCHING;
    private NPC talkingTo;
    private DeathScreen deathScreen;

    public static void main(String[] args) {
        ExperienceTable.initializeExpTable();
        new WorldQuest();
    }

    private WorldQuest() {
        super("WorldQuest v0.0.1");
        setSize(640, 480);
        WorldQuestKeyListener keyListener = new WorldQuestKeyListener(this);
        gameUI = new GameUI();
        WorldQuestMouseListener mouseListener = new WorldQuestMouseListener(this, gameUI);
        WorldQuestCanvas canvas = new WorldQuestCanvas(keyListener, mouseListener);
        ScheduledExecutorService renderQueue = Executors.newSingleThreadScheduledExecutor();
        getContentPane().add(canvas);
        pack();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WorldQuestWindowAdapter(this));
        loadingScreen = new LoadingScreen();

        renderQueue.scheduleAtFixedRate(canvas, 0, 16, TimeUnit.MILLISECONDS);
        setVisible(true);
        new Thread(() -> {
            try {
                gameState = LOADING;
                loadScenario();
                if (resourceInSaveFolder("player").exists()) {
                    loadGame();
                } else {
                    Files.createDirectories(new File("saves/save").toPath());
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
        loadMap("map");
        this.player = PlayerProvider.createPlayer();
        eventHistory = new ArrayList<>();
    }

    private void loadGame() {
        File saveFile = resourceInSaveFolder("player");
        if (!saveFile.exists()) {
            throw new RuntimeException("Unable to load save data: Save data file not found: "+ saveFile);
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
        gameUI = new GameUI();
        gameScreen = new GameScreen(new MapView());
        sidebar = new SidebarUI(this);
        messages = new MessageDisplay();
        gameState = RUNNING;
    }

    void saveGame() {
        File saveFile = resourceInSaveFolder("player");
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
        tileTypes.putAll(GameData.tileTypes);
    }

    private void loadNpcTypes() {
        npcTypes.putAll(GameData.npcTypes);
    }

    private void loadItemUses() {
        itemUses.putAll(GameData.itemUses);
        tileItemUses.putAll(GameData.tileItemUses);
    }

    private void loadMap(String mapResourceName) {
        boolean copied;
        try {
            copied = copyScenarioMapIfNotFound(mapResourceName);
        } catch (IOException e) {
            throw new RuntimeException("Resource not found: " + mapResourceName);
        }
        File mapFile = resourceInSaveFolder(mapResourceName);
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
            try {
                if (copied) {
                    Files.delete(resourceInSaveFolder(mapResourceName).toPath());
                }
            } catch (IOException deleteError) {
                throw new RuntimeException("Unable to delete bad copy", deleteError);
            }
            throw new RuntimeException("Unable to load map data: " + e.getMessage(), e);
        }
    }

    private File resourceInSaveFolder(String resource) {
        return new File("saves"+File.separator+"save"+File.separator+resource+".dat");
    }

    private File resourceInScenarioFolder(String resource) {
        return new File("scenario"+File.separator+"default"+File.separator+resource+".dat");
    }

    private boolean copyScenarioMapIfNotFound(String resource) throws IOException {
        if(!resourceInSaveFolder(resource).exists()) {
            Files.copy(resourceInScenarioFolder(resource).toPath(), resourceInSaveFolder(resource).toPath());
            return true;
        }
        return false;
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
                gameUI.render(g);
            } catch (Exception e) {
                new Exception("Render failed", e).printStackTrace();
                System.exit(1);
            }
        }

        @Override
        public void run() {
            repaint();
        }
    }

    class GameUI implements UI {

        @Override
        public void render(Graphics2D g) {
            if (gameState == LAUNCHING || gameState == LOADING) {
                loadingScreen.render(g);
            } else if (gameState != DEAD) {
                gameScreen.render(g);
                sidebar.render(g);
                messages.render(g);
            } else {
                deathScreen.render(g);
            }
        }

        @Override
        public void onClick(MouseEvent e) {
            if (gameState == LAUNCHING || gameState == LOADING) {
                loadingScreen.onClick(e);
            } else {
                if (gameScreen.contains(e.getPoint())) {
                    gameScreen.onClick(e);
                } else if (sidebar.contains(e.getPoint())) {
                    sidebar.onClick(e);
                } else if (messages.contains(e.getPoint())) {
                    messages.onClick(e);
                }
            }
        }
    }

    class LoadingScreen implements UI {

        @Override
        public void render(Graphics2D g) {
            g.setColor(Color.WHITE);
            g.drawRect(9,9, BORDER_WIDTH, BORDER_HEIGHT);
            g.drawString("Loading game", 200, 200);
        }

        @Override
        public void onClick(MouseEvent e) {}
    }

    class MapView implements UI {

        @Override
        public void render(Graphics2D g) {
            MapViewPainter.paintMapView(g, WorldQuest.this, map, visibleNpcs, player);
        }

        @Override
        public void onClick(MouseEvent e) {
            Tile t = checkForTileInterception(e.getPoint());
            if (t != null) {
                processTileClick(t);
            }
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
    }

    class DeathScreen implements Screen {

        private void paintEventHistory(Graphics2D g) {
            g.setColor(Color.BLACK);
            g.fillRect(40, 40, 560, 400);
            g.setColor(Color.WHITE);
            g.drawRect(40, 40, 560, 400);
            g.drawString("Events", 60, 60);
            for (int i = 0; i < eventHistory.size() && i < 15; i++) {
                g.drawString(eventHistory.get(i), 80, 80+(i*20));
            }
            g.drawString("New Game? Y/N", 60, 400);
        }

        @Override
        public void render(Graphics2D g) {
            paintEventHistory(g);
        }

        @Override
        public void onClick(MouseEvent e) {

        }
    }

    class MessageDisplay extends Rectangle implements UI {

        @Override
        public void render(Graphics2D g) {
            if (messageState == PLAYER_TALKING || messageState == NPC_TALKING) {
                ConversationPainter.paintConversation(g, messageState, talkingTo);
            } else if (messageState == CONVERSATION_OPTION) {
                ConversationPainter.paintConversationOptions(g, (ConversationChoiceSelection) talkingTo.currentConversation.npcAction);
            }
        }

        @Override
        public void onClick(MouseEvent e) {

        }
    }

    Action getKeyMappedAction(Character c) {
        return keymap.get(c);
    }

    Action getKeypressMappedAction(Integer code) {
        return keyPressMap.get(code);
    }

    void processAction(Action a, KeyEvent e) {
        if (gameState == RUNNING) {
            if (messageState == PLAYER_TALKING || messageState == NPC_TALKING || messageState == CONVERSATION_OPTION) {
                processActionWhileTalking(a, e);
            } else {
                processActionWhileRunning(a);
            }
        } else  if (gameState == SHOP) {
            processActionWhileShopping(a);
        } else if (gameState == DEAD) {
            processActionWhileGameOver(a);
        }
    }

    private void processTileClick(Tile tile) {
        processPlayerInputWhileRunning(() -> {
            if (player.itemBeingUsed != -1) {
                int item = player.itemBeingUsed;
                player.itemBeingUsed = -1;
                return useItemOnTile(item, tile);
            }
            return false;
        });
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

    private void processActionWhileTalking(Action action, KeyEvent e) {
        switch(action) {
            case TALK_CONTINUE:
                progressConversation();
                break;
            case CONVERSATION_OPTION:
                pickConversationOption(Integer.parseInt(""+e.getKeyChar()));
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
            case START_NEW_GAME:
                newGame();
                continueGame();
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

    void equipItem(int index) {
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

    boolean useItem(int index) {
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

    void dropItem(int index) {
        map[player.x][player.y].objects.add(new GObjects.ItemDrop(player.inventory.remove(index)));
    }

    void talkTo(int index) {
        startConversation(visibleNpcs.get(index));
    }

    private void startConversation(NPC npc) {
        npc.startConversation();
        talkingTo = npc;
        displayConversation();
    }

    private void displayConversation() {
        if (talkingTo.currentConversation.playerText != null) {
            messageState = PLAYER_TALKING;
        } else {
            messageState = NPC_TALKING;
        }
    }

    private void progressConversation() {
        if (messageState == PLAYER_TALKING) {
            messageState = NPC_TALKING;
        } else {
            if (talkingTo.currentConversation.npcAction != null) {
                talkingTo.currentConversation.npcAction.doAction(this, talkingTo);
            } else {
                endConversation(talkingTo);
            }
        }
    }

    void setMessageState(MessageState newState) {
        messageState = newState;
    }

    private void pickConversationOption(int i) {
        talkingTo.currentConversation = ((ConversationChoiceSelection) talkingTo.currentConversation.npcAction)
                .conversationOptions
                .get(i-1);
        displayConversation();
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
        deathScreen = new DeathScreen();
    }

    void switchMap(String map, int startX, int startY) {
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

    public void startQuest(String questName) {
    }

    void showShop(Shop shop) {
        gameScreen.showWindow(new ShopWindow(this, shop));
    }

    private void closeShop() {
        gameState = RUNNING;
    }

    boolean isVisible(int x, int y) {
        boolean isVisible = x >= player.x - 5 && x <= player.x + 5 && y >= player.y - 5 && y <= player.y + 5;
        return isVisible;
    }

    public void changeTileType(Tile tile, TileType type) {
        Tile newTile = new Tile(type, tile.x, tile.y);
        newTile.objects.addAll(tile.objects);
        map[tile.x][tile.y] = newTile;
    }

    public void endConversation(NPC npc) {
        npc.currentConversation = null;
        messageState = CHATBOX;
    }

    enum GameState {
        LAUNCHING, LOADING,
        RUNNING,
        SHOP,
        DEAD
    }

    enum MessageState {
        CHATBOX,
        CONVERSATION_OPTION,
        PLAYER_TALKING, NPC_TALKING
    }

    public void buyItem(Shop shop, int i) {
        ShopListing s = shop.items.get(i);
        if (s.quantity >= 1 && s.getPrice() <= player.money) {
            s.quantity -= 1;
            player.money -= s.getPrice();
            player.inventory.add(s.item.copy());
        }
    }
}
