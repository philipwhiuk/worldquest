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
        keymap.put('>', Action.STAIRS);
        keymap.put('n', Action.EXIT);
        keymap.put('N', Action.EXIT);
        keymap.put('y', Action.START_NEW_GAME);
        keymap.put('Y', Action.START_NEW_GAME);
        keymap.put('1', Action.CONVERSATION_OPTION);
        keymap.put('2', Action.CONVERSATION_OPTION);
        keymap.put('3', Action.CONVERSATION_OPTION);
        keyPressMap.put(KeyEvent.VK_ENTER, Action.TALK_CONTINUE);
    }

    private Map<Integer, TileType> tileTypes = new HashMap<>();
    private Map<String, NPCType> npcTypes = new HashMap<>();
    private Map<String, ItemAction> itemUses = new HashMap<>();
    private Map<String, ItemAction> tileItemUses = new HashMap<>();
    private Map<String, ItemAction> objectItemUses = new HashMap<>();
    private Map<String, GObjects.GameObjectBuilder> gameObjectBuilders = new HashMap<>();
    private Map<String, Quest> questList = new HashMap<>();

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
    private List<Room> rooms;

    public static void main(String[] args) {
        ExperienceTable.initializeExpTable();
        new WorldQuest();
    }

    private WorldQuest() {
        super("WorldQuest v0.0.1");
        setSize(640, 480);
        setJMenuBar(buildMenuBar());
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

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("WorldQuest");
        JMenuItem licenseItem = new JMenuItem("License");
        licenseItem.addActionListener(e -> JOptionPane.showMessageDialog(
                WorldQuest.this,
                "All rights reserved, Philip Whitehouce (2018)"));
        menu.add(licenseItem);
        menuBar.add(menu);
        return menuBar;
    }

    private void loadScenario() {
        loadTileTypes();
        loadNpcTypes();
        loadItemUses();
        loadQuestList();
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
            saveMap(mapName);
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

    private void saveMap(String mapResourceName) {
        File mapFile = resourceInSaveFolder(mapResourceName);
        try(
                OutputStream mapDataStream = new FileOutputStream(mapFile);
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(mapDataStream))) {
            MapTileLoader.saveMapTiles(map, buffer);
            NPCLoader.saveNPCs(npcTypes, npcs, buffer);
            GameObjectLoader.saveGameObjects(buffer, map);
            RoomLoader.saveRooms(buffer, rooms);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save map: " + e.getMessage(), e);
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
        objectItemUses.putAll(GameData.objectItemUses);
    }

    private void loadQuestList() {
        questList.putAll(GameData.questList);
    }

    private void loadMap(String mapResourceName) {
        boolean copied;
        try {
            copied = copyScenarioMapIfNotFoundOrNewer(mapResourceName);
        } catch (IOException e) {
            throw new RuntimeException("Resource not found: " + mapResourceName, e);
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
            this.rooms = RoomLoader.loadRooms(buffer, newMap);
            this.npcs = npcs;
            this.map = newMap;
            this.mapName = mapResourceName;
            if (buffer.readLine() != null) {
                throw new RuntimeException("Map has unread data");
            }
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

    private boolean copyScenarioMapIfNotFoundOrNewer(String resource) throws IOException {
        if (resourceInSaveFolder(resource).exists() && scenarioFileIsNewer(resource)) {
            Files.delete(resourceInSaveFolder(resource).toPath());
        }
        if(!resourceInSaveFolder(resource).exists()) {
            Files.copy(resourceInScenarioFolder(resource).toPath(), resourceInSaveFolder(resource).toPath());
            return true;
        }
        return false;
    }

    private boolean scenarioFileIsNewer(String resource) {
        return resourceInScenarioFolder(resource).lastModified() > resourceInSaveFolder(resource).lastModified();
    }

    public void reportHit(GameCharacter attacker, GameCharacter defender, int damage) {
        if (attacker instanceof Player) {
            eventMessage("You attack the " + ((NPC) defender).type.name + ", doing " + damage + " damage");
        } else if (defender instanceof Player) {
            eventMessage("The "+((NPC) attacker).type.name+ " attacks you, doing " + damage + " damage");
        } else {
            eventMessage("The "+((NPC) attacker).type.name+ " attacks the " + ((NPC) defender).type.name +", doing " + damage + " damage");
        }
    }

    public void eventMessage(String message) {
        eventHistory.add(message);
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
                ConversationPainter.paintConversationOptions(
                        g,
                        WorldQuest.this,
                        (ConversationChoiceSelection) talkingTo.currentConversation.npcAction);
            } else {
                int last = eventHistory.size()-1;
                for (int i = 0; i < 5 && last-i > 0; i++) {
                    g.setColor(Color.WHITE);
                    g.setFont(g.getFont().deriveFont(Font.BOLD,11));
                    g.drawString(eventHistory.get(last-i), 25, CONVERSATION_Y+80-(i*20));
                }
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
            directionAction(subject, Direction.NORTH, subject.x, subject.y - 1);
        }
    }

    private void south(GameCharacter subject) {
        if (inBounds(subject.x, subject.y + 1)) {
            directionAction(subject, Direction.SOUTH, subject.x, subject.y + 1);
        }
    }

    private void east(GameCharacter subject) {
        if (inBounds(subject.x + 1, subject.y)) {
            directionAction(subject, Direction.EAST, subject.x + 1, subject.y);
        }
    }

    private void west(GameCharacter subject) {
        if (inBounds(subject.x - 1, subject.y)) {
            directionAction(subject,Direction.WEST, subject.x - 1, subject.y);
        }
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x <= MAX_X && y >= 0 && y <= MAX_Y;
    }

    private void directionAction(GameCharacter subject, Direction directionMoving, int x, int y) {
        for (NPC npc : npcs) {
            if (npc.x == x && npc.y == y && npc != subject) {
                //Player asks to move to another subject - handle interaction (e.g. fight)
                subject.actionOnNpc(this, npc);
                return;
            }
        }
        if (player.x == x && player.y == y && player != subject) {
            //Subject can't move to player's location (attacks already dealt with)
            return;
        }
        if (map[x][y].canMoveTo(directionMoving)) {
            if (player == subject) {
                if (map[player.x][player.y].room != map[x][y].room) {
                    if (map[x][y].room == null) {
                        eventMessage("Leaving the "+map[player.x][player.y].room.name);
                    } else if (map[player.x][player.y].room == null) {
                        eventMessage("Entering the "+map[x][y].room.name);
                    } else {
                        eventMessage("Moving to the "+map[x][y].room.name);
                    }
                }
                map[x][y].onMoveTo(player);
            }
            subject.x = x;
            subject.y = y;
        } else if (map[x][y].objects.size() > 0) {
            map[x][y].objects.get(0).doAction(this, player);
        }
    }

    void equipItem(int index) {
        Item i = player.inventory.get(index);
        if (i instanceof Weapon) {
            Weapon w = (Weapon) i;
            if (player.mainHandWeapon != null && ! player.inventory.hasSpaceForItem(player.mainHandWeapon)) {
                eventMessage("No space to store the item you're currently wielding");
            } else {
                if (player.mainHandWeapon != null) {
                    player.inventory.add(player.mainHandWeapon);
                    player.mainHandWeapon = null;
                }
                player.inventory.remove(w);
                player.mainHandWeapon = w;
            }
        } else if (i instanceof Armour) {
            Armour a = (Armour) i;
            if (player.armour.get(a.slot) != null && ! player.inventory.hasSpaceForItem(player.armour.get(a.slot))) {
                eventMessage("No space to store the item you're currently wielding");
            } else {
                Armour removed = player.armour.put(a.slot, a);
                if (removed != null) {
                    player.inventory.add(removed);
                }
                player.inventory.remove(a);
            }
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

    private boolean useItemOnTile(int itemIndex, Tile tile) {
        String itemName = player.inventory.get(itemIndex).name;
        String key = tile.type.name+","+player.inventory.get(itemIndex).name;
        ItemAction action = tileItemUses.get(key);
        if (action != null) {
            action.perform(this, tile, player, itemIndex, -1);
            return true;
        }
        for (GObjects.GameObject object : tile.objects) {
            key = object.getClass().getSimpleName()+","+itemName;
            System.out.println("Key: "+key);
            action = objectItemUses.get(key);
            if (action != null) {
                System.out.println("Performing action");
                action.perform(this, tile, player, itemIndex, -1);
                return true;
            }
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
        npc.startConversation(this);
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
        ConversationChoiceSelection selection = ((ConversationChoiceSelection) talkingTo.currentConversation.npcAction);
        List<ConversationChoice> options = selection
                .conversationOptions.stream()
                .filter(choice -> choice.canSee.test(new QuestState(this)))
                .collect(Collectors.toList());
        talkingTo.currentConversation = options.get(i-1);
        displayConversation();
    }

    private void tick(int initialPlayerHealth) {
        for (NPC npc: npcs) {
            tickNPC(npc);
        }
        npcs.removeIf(GameCharacter::isDead);
        for (Tile[] tiles: map) {
            for(Tile tile: tiles) {
                Iterator<GObjects.GameObject> objectIterator = tile.objects.iterator();
                while (objectIterator.hasNext()) {
                    objectIterator.next().tick(objectIterator);
                }
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
        } else if(npc.canMove()) {
            Direction direction = npc.movementStrategy.nextDirection(npc);
            if (direction != null) {
                switch (direction) {
                    case NORTH:
                        north(npc);
                        break;
                    case SOUTH:
                        south(npc);
                        break;
                    case EAST:
                        east(npc);
                        break;
                    case WEST:
                        west(npc);
                        break;
                }
            }
        }
    }

    private void attackPlayer(NPC npc) {
        npc.attack(this, player);
        if (player.isDead()) {
            eventHistory.add("Killed by a "+npc.type.name);
        }
    }

    private boolean nextToPlayer(NPC npc) {
        return
                (player.x == npc.x && (player.y-1 == npc.y || player.y+1 == npc.y)) ||
                        (player.y == npc.y && (player.x-1 == npc.x || player.x+1 == npc.x));
    }

    private void gameOver() {
        gameState = DEAD;
        deathScreen = new DeathScreen();
    }

    void switchMap(String map, int startX, int startY) {
        saveMap(mapName);
        loadMap(map);
        player.x = startX;
        player.y = startY;
    }

    void npcAttacked(NPC npc) {
        if (npc.isDead()) {
            map[npc.x][npc.y].objects.add(npc.dropItem());
            player.quests.forEach((name,quest) -> quest.npcDeath(npc.type.name));
            eventHistory.add("Killed a "+npc.type.name);
        }
    }

    void spawn(GObjects.GameObject object, int x, int y) {
        map[x][y].objects.add(object);
    }

    public void startQuest(String questName) {
        Quest quest = questList.get(questName);
        if (quest == null) {
            throw new RuntimeException("Unable to start quest "+questName+" - quest not found");
        } else if (player.quests.containsKey(questName)) {
            throw new RuntimeException("Unable to start quest "+questName+" - already started quest");
        } else {
            player.quests.put(questName, quest.start());
        }
    }

    void showShop(Shop shop) {
        gameScreen.showWindow(new ShopWindow(this, shop));
    }

    private void closeShop() {
        gameState = RUNNING;
    }

    boolean isVisible(int x, int y) {
        int xDiff = Math.abs(x - player.x);
        int yDiff = Math.abs(y - player.y);

        if (xDiff + yDiff > 8 || xDiff > 6 || yDiff > 6) {
            return false;
        }
        if (bothNotInRoom(x, y)) {
            return true;
        }
        if (inSameRoom(x, y)) {
            return true;
        }
        if (map[player.x][player.y].room == null && visibleRoomEdge(x,y)) {
                return true;
        }
        return false;
    }

    private boolean bothNotInRoom(int x, int y) {
        return map[player.x][player.y].room == null && map[x][y].room == null;
    }

    private boolean inSameRoom(int x, int y) {
        return map[player.x][player.y].room == map[x][y].room;
    }

    private boolean visibleRoomEdge(int x, int y) {
        Room mapRoom = map[x][y].room;
        if (player.x < x && ((int) mapRoom.getMinX()) == x) {
            return true;
        }
        if (player.x > x && ((int) mapRoom.getMaxX()) == x+1) {
            return true;
        }
        if (player.y < y && ((int) mapRoom.getMinY()) == y) {
            return true;
        }
        if (player.y > y && ((int) mapRoom.getMaxY()) == y+1) {
            return true;
        }
        return false;
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
            if (player.inventory.hasSpaceForItem(s.item)) {
                player.money -= s.getPrice();
                player.inventory.add(s.item.copy());
            }
        }
    }
}
