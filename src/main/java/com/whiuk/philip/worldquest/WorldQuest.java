package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.ClickableUI;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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

import static com.whiuk.philip.worldquest.GameFileUtils.*;
import static com.whiuk.philip.worldquest.MapConstants.*;
import static com.whiuk.philip.worldquest.AppState.*;
import static com.whiuk.philip.worldquest.MessageState.*;

public class WorldQuest extends JFrame {
    static final String LICENSE_TEXT = "All rights reserved, Philip Whitehouse (2018)";
    private static final String DEFAULT_SCENARIO = "default";
    private static final String INITIAL_MAP_FILE = "map000";
    private static final String KEYMAP_FILE = "keymap";

    private static final HashMap<Character, Action> keymap = new HashMap<>();
    private static final HashMap<Integer, Action> keyPressMap = new HashMap<>();

    static {
        setupKeymap();
    }

    private List<TileSelectionListener> tileSelectionListeners = new ArrayList<>();
    private WorldQuestMenuBar menuBar;

    private static void setupKeymap() {
        if (Files.exists(new File(KEYMAP_FILE+FILE_EXTENSION).toPath())) {
            try {
                readKeymap();
            } catch (Exception e) {
                e.printStackTrace();
                setupDefaultKeymap();
            }
        } else {
            setupDefaultKeymap();
        }
    }

    private static void readKeymap() {
        try(
                InputStream mapDataStream = new FileInputStream(KEYMAP_FILE+FILE_EXTENSION);
                BufferedReader buffer = new BufferedReader(new InputStreamReader(mapDataStream))) {
            int keyTypeCount = Integer.parseInt(buffer.readLine());
            for (int i = 0; i < keyTypeCount; i++ ) {
                String keyTypeLine = buffer.readLine();
                String[] keyData = keyTypeLine.split(",");
                keymap.put(keyData[0].charAt(0), Action.valueOf(keyData[1]));
            }
            int keyPressCount = Integer.parseInt(buffer.readLine());
            for (int i = 0; i < keyPressCount; i++ ) {
                String keyPressLine = buffer.readLine();
                String[] keyPressData = keyPressLine.split(",");
                keyPressMap.put(Integer.parseInt(keyPressData[0]), Action.valueOf(keyPressData[1]));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load key map data: " + e.getMessage(), e);
        }
    }

    private static void setupDefaultKeymap() {
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

    AppState appState = LAUNCHING;

    ScenarioData scenarioData;
    private Map<String, GObjects.GameObjectBuilder> gameObjectBuilders;

    private GameUI gameUI;
    private LoadingScreen loadingScreen;
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private DeathScreen deathScreen;
    private Screen editorScreen;
    private MessageState messageState;
    private List<String> messageHistory;


    private String scenario = DEFAULT_SCENARIO;
    private String saveFolder;

    Tile[][] map;
    private String mapName;
    List<NPC> npcs;
    private List<Room> rooms;
    private String northMap;
    private String eastMap;
    private String southMap;
    private String westMap;

    //TODO: Refactor into GameInProgress class
    Player player;
    List<NPC> visibleNpcs;
    private NPC talkingTo;
    private Shop currentShop;

    //TODO: Refactor into ScenarioBeingChanged class

    public static void main(String[] args) {
        ExperienceTable.initializeExpTable();
        new WorldQuest();
    }

    private WorldQuest() {
        super("WorldQuest v0.0.1");
        setSize(640, 480);
        menuBar = new WorldQuestMenuBar(this);
        setJMenuBar(menuBar);
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
        launch();
        renderQueue.scheduleAtFixedRate(canvas, 0, 16, TimeUnit.MILLISECONDS);
        setVisible(true);
    }

    private void launch() {
        showMenu();
    }

    private void showMenu() {
        menuScreen = new MenuScreen(this);
        appState = MENU_SCREEN;
    }

    void loadSave(String saveFolder) {
        if (!GameFileUtils.savedGameExists(saveFolder)) {
            JOptionPane.showMessageDialog(WorldQuest.this,
                    "Saved Game Doesn't Exist",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                appState = LOADING;
                String scenario = Files.readAllLines(resourceInSaveFolder(saveFolder, "scenario").toPath()).get(0);
                this.saveFolder = saveFolder;
                loadScenario(scenario);
                loadGame();
                continueGame();
            } catch (Exception e) {
                crashOnError(e);
            }
        }
    }

    void newSaveGame(String scenarioName) {
        try {
            appState = LOADING;
            scenario = scenarioName;
            this.saveFolder = GameFileUtils.newSaveFolder();
            loadScenario(scenarioName);
            GameFileUtils.deleteSave(saveFolder);
            GameFileUtils.createSave(saveFolder);
            writeSaveScenarioFile();
            newGame();
            continueGame();
        } catch (Exception e) {
            crashOnError(e);
        }
    }

    public void newScenario() {
        try {
            appState = LOADING;
            //Create scenario
            createScenario();
            //Write Scenario
            String scenarioName = saveAsNewScenario();
            //Load Scenario
            loadScenario(scenarioName);
            continueEditingScenario();
            } catch (Exception e) {
            crashOnError(e);
        }
    }

    private void createScenario() {
        scenarioData = ScenarioData.Provider.loadScenarioFromBase();
        mapName = INITIAL_MAP_FILE;
        map = MapTileLoader.newMap(scenarioData.tileTypes);
        npcs = new ArrayList<>();
        rooms = new ArrayList<>();
        northMap = eastMap = westMap = southMap = "none";
    }

    private String saveAsNewScenario() {
        try {
            String name = GameFileUtils.newScenarioFolder();
            GameFileUtils.createScenario(name);
            scenario = name;
            saveScenario();
            return name;
        } catch (Exception e) {
            crashOnError(e);
            return null;
        }
    }

    void saveScenario() {
        ScenarioData.Persistor.saveScenario(scenarioData, scenario);
        saveScenarioMap(mapName);
    }

    public void editScenario(String scenarioName) {
        try {
            appState = LOADING;
            scenario = scenarioName;
            loadScenario(scenarioName);
            continueEditingScenario();
        } catch (Exception e) {
            crashOnError(e);
        }
    }

    private void continueEditingScenario() {
        loadScenarioMap(INITIAL_MAP_FILE);
        EditorSidebar sidebar = new EditorSidebar(this);
        MessageDisplay messages = new MessageDisplay();
        MapView mapView = new EditorMapView(this, this::processEditorTileClick);
        editorScreen = new EditorScreen(mapView, sidebar, messages);
        messageHistory = new ArrayList<>();
        menuBar.showEditorScreenItems(this);
        appState = EDITOR_RUNNING;
    }

    private void crashOnError(Throwable e) {
        JOptionPane.showMessageDialog(WorldQuest.this,
                e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
        System.exit(1);
    }

    private void writeSaveScenarioFile() throws IOException {
        File scenarioFile = resourceInSaveFolder(saveFolder, "scenario");
        OutputStream scenarioDataStream = new FileOutputStream(scenarioFile);
        BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(scenarioDataStream));
        buffer.write(scenario);
        buffer.flush();
        buffer.close();
        scenarioDataStream.close();
    }

    private void loadScenario(String scenarioName) {
        scenarioData = ScenarioData.Provider.loadScenarioByName(scenarioName);
        gameObjectBuilders = GObjects.provideBuilders(scenarioData.itemTypes);
    }

    private void newGame() {
        loadGameMap(INITIAL_MAP_FILE);
        this.player = PlayerProvider.createPlayer(scenarioData);
        messageHistory = new ArrayList<>();
    }

    private void loadGame() {
        File playerSaveFile = resourceInCurrentSaveFolder(PLAYER_SAVE_FILE);
        if (!playerSaveFile.exists()) {
            throw new RuntimeException("Unable to load save data: Player save data file not found: "+ playerSaveFile);
        }
        if (!playerSaveFile.canRead()) {
            throw new RuntimeException("Unable to read player save file");
        }
        try(
                InputStream mapDataStream = new FileInputStream(playerSaveFile);
                BufferedReader buffer = new BufferedReader(new InputStreamReader(mapDataStream))) {
            String mapName = buffer.readLine();
            loadGameMap(mapName);
            this.player = PlayerProvider.loadPlayer(new JSONObject(), scenarioData.itemTypes, scenarioData.quests);
            int eventHistoryItems = Integer.parseInt(buffer.readLine());
            List<String> eventHistory = new ArrayList<>();
            for (int i = 0; i < eventHistoryItems; i++) {
                eventHistory.add(buffer.readLine());
            }
            this.messageHistory = eventHistory;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load saved game: " + e.getMessage(), e);
        }
    }

    private void continueGame() {
        visibleNpcs = npcs.stream().filter(npc -> isVisible(npc.x, npc.y)).collect(Collectors.toList());
        GameSidebar sidebar = new GameSidebar(this);
        MessageDisplay messages = new MessageDisplay();
        gameScreen = new GameScreen(
                new GameMapView(this, this::processGameTileClick), sidebar, messages);
        appState = GAME_RUNNING;
    }

    void saveGame() {
        File saveFile = resourceInCurrentSaveFolder("player");
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
                OutputStream saveFileDataStream = new FileOutputStream(saveFile);
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(saveFileDataStream))) {
            buffer.write(mapName);
            buffer.newLine();
            saveGameMap(mapName);
            PlayerProvider.savePlayer(buffer, player);
            buffer.write(""+ messageHistory.size());
            buffer.newLine();
            for (String line : messageHistory) {
                buffer.write(line);
                buffer.newLine();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to save: " + e.getMessage(), e);
        }
    }

    private void saveScenarioMap(String mapResourceName) {
        saveMap(resourceInCurrentScenarioFolder(mapResourceName));
    }

    private void saveGameMap(String mapResourceName) {
        saveMap(resourceInCurrentSaveFolder(mapResourceName));
    }

    private void saveMap(File mapFile) {
        try(
                OutputStream mapDataStream = new FileOutputStream(mapFile);
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(mapDataStream))) {
            MapTileLoader.saveMapTiles(map);
            MapTileLoader.saveMapExploration(map);
            NPCLoader.saveNPCs(scenarioData.npcTypes, npcs);
            GameObjectLoader.saveGameObjects(map);
            RoomLoader.saveRooms(buffer, rooms);
            buffer.write(northMap); buffer.newLine();
            buffer.write(eastMap); buffer.newLine();
            buffer.write(southMap); buffer.newLine();
            buffer.write(westMap); buffer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Unable to save map: " + e.getMessage(), e);
        }
    }

    private boolean mapExists(String mapResourceName) {
        return resourceInCurrentScenarioFolder(mapResourceName).exists();
    }

    private void loadGameMap(String mapResourceName) {
        boolean copied;
        try {
            copied = copyCurrentScenarioMapIfNotFoundOrNewer(mapResourceName);
        } catch (IOException e) {
            throw new RuntimeException("Resource not found: " + mapResourceName, e);
        }
        try {
            loadMap(mapResourceName, resourceInCurrentSaveFolder(mapResourceName));
        } catch (Exception e) {
            try {
                if (copied) {
                    Files.delete(resourceInCurrentSaveFolder(mapResourceName).toPath());
                }
            } catch (IOException deleteError) {
                throw new RuntimeException("Unable to delete bad copy", deleteError);
            }
            throw new RuntimeException("Unable to load map data: " + e.getMessage(), e);
        }
    }

    private void loadScenarioMap(String mapResourceName) {
        loadMap(mapResourceName, resourceInCurrentScenarioFolder(mapResourceName));
    }

    private void loadMap(String mapResourceName, File mapFile) {
        if (!mapFile.exists()) {
            throw new RuntimeException(
                    "Unable to load map data: Map data file not found: " + mapFile.getAbsolutePath());
        }
        if (!mapFile.canRead()) {
            throw new RuntimeException("Unable to read map file");
        }
        try(FileReader fileReader = new FileReader(mapFile)) {
            JSONParser parser = new JSONParser();
            JSONObject mapData = (JSONObject) parser.parse(fileReader);
            Tile[][] newMap = MapTileLoader.loadMapTiles(scenarioData.tileTypes, (JSONArray) mapData.get("tiles"));
            MapTileLoader.markExploration(newMap, (JSONArray) mapData.get("exploration"));
            List<NPC> npcs = NPCLoader.loadNPCs(scenarioData.npcTypes, (JSONArray) mapData.get("npcs"));
            GameObjectLoader.loadGameObjects(gameObjectBuilders, (JSONArray) mapData.get("gameObjects"), newMap);
            this.rooms = RoomLoader.loadRooms((JSONArray) mapData.get("rooms"), newMap);
            this.northMap = (String) mapData.get("northMap");
            this.eastMap = (String) mapData.get("eastMap");
            this.southMap = (String) mapData.get("southMap");
            this.westMap = (String) mapData.get("westMap");
            this.npcs = npcs;
            this.map = newMap;
            this.mapName = mapResourceName;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load map data: " + e.getMessage(), e);
        }
    }

    private File resourceInCurrentSaveFolder(String resource) {
        return resourceInSaveFolder(this.saveFolder, resource);
    }

    private File resourceInCurrentScenarioFolder(String resource) {
        return resourceInScenarioFolder(this.scenario, resource);
    }

    private boolean copyCurrentScenarioMapIfNotFoundOrNewer(String resource) throws IOException {
        if (resourceInCurrentSaveFolder(resource).exists() && currentScenarioFileIsNewer(resource)) {
            Files.delete(resourceInCurrentSaveFolder(resource).toPath());
        }
        if(!resourceInCurrentSaveFolder(resource).exists()) {
            Files.copy(resourceInCurrentScenarioFolder(resource).toPath(), resourceInCurrentSaveFolder(resource).toPath());
            return true;
        }
        return false;
    }

    private boolean currentScenarioFileIsNewer(String resource) {
        return resourceInCurrentScenarioFolder(resource).lastModified() > resourceInCurrentSaveFolder(resource).lastModified();
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
        messageHistory.add(message);
    }

    public void attemptResourceGathering(ResourceGathering resourceGathering, Tile tile) {
        resourceGathering.gather(this, player, tile);
    }

    public void attemptStructureCreation(StructureCreation structureCreation, Tile tile, int item) {
        structureCreation.create(this, player, tile, item);
    }

    public boolean inShop() {
        return appState == GAME_SHOP;
    }

    public boolean isGameRunning() {
        return appState == GAME_SHOP || appState == GAME_RUNNING;
    }

    public void registerTileSelectionListener(TileSelectionListener listener) {
        tileSelectionListeners.add(listener);
    }

    public void showItemManager() {
        JFrame itemManager = new JFrame();
        itemManager.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Object[][] itemData = scenarioData.itemTypes.entrySet().stream().map(e -> new Object[]{
                e.getKey(),
                e.getClass().getSimpleName(),
                e.getValue().name
        }).collect(Collectors.toList()).toArray(new Object[][]{});
        String[] columns = new String[]{"Key", "Type", "Data"};
        JTable itemTable = new JTable(itemData, columns);
        itemManager.getContentPane().add(itemTable);
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

    class GameUI extends ClickableUI {

        @Override
        public void render(Graphics2D g) {
            selectScreen().render(g);
        }

        private Screen selectScreen() {
            if (appState == LAUNCHING || appState == LOADING) {
                return loadingScreen;
            } else if (appState == MENU_SCREEN) {
                return menuScreen;
            } else if (appState == EDITOR_RUNNING) {
                return editorScreen;
            } else if (appState != GAME_DEAD) {
                return gameScreen;
            } else {
                return deathScreen;
            }
        }

        @Override
        public void onClick(MouseEvent e) {
            selectScreen().onClick(e);
        }
    }

    class MessageDisplay extends ClickableUI {

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
                int last = messageHistory.size()-1;
                for (int i = 0; i < 5 && last-i > 0; i++) {
                    g.setColor(Color.WHITE);
                    g.setFont(g.getFont().deriveFont(Font.BOLD,11));
                    g.drawString(messageHistory.get(last-i), 25, CONVERSATION_Y+100-(i*20));
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
        if (appState == GAME_RUNNING) {
            if (messageState == PLAYER_TALKING || messageState == NPC_TALKING || messageState == CONVERSATION_OPTION) {
                processActionWhileTalking(a, e);
            } else {
                processActionWhileRunning(a);
            }
        } else  if (appState == GAME_SHOP) {
            processActionWhileShopping(a);
        } else if (appState == GAME_DEAD) {
            processActionWhileGameOver(a);
        }
    }

    private void processEditorTileClick(Tile tile) {
        tileSelectionListeners.forEach(l -> l.tileSelected(tile));
    }

    private void processGameTileClick(Tile tile) {
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
        int initialPlayerFood = player.food;
        boolean shouldTick = processor.getAsBoolean();
        if (shouldTick) {
            tick(initialPlayerHealth, initialPlayerFood);
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
        } else if (subject == player) {
            switchGameMap(northMap, subject.x, MAX_Y);
        }
    }

    private void south(GameCharacter subject) {
        if (inBounds(subject.x, subject.y + 1)) {
            directionAction(subject, Direction.SOUTH, subject.x, subject.y + 1);
        } else if (subject == player) {
            switchGameMap(southMap, subject.x, 0);
        }
    }

    private void east(GameCharacter subject) {
        if (inBounds(subject.x + 1, subject.y)) {
            directionAction(subject, Direction.EAST, subject.x + 1, subject.y);
        } else if (subject == player) {
            switchGameMap(eastMap, 0, subject.y);
        }
    }

    private void west(GameCharacter subject) {
        if (inBounds(subject.x - 1, subject.y)) {
            directionAction(subject,Direction.WEST, subject.x - 1, subject.y);
        } else if (subject == player) {
            switchGameMap(westMap, MAX_X, 0);
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

    public void actionItem(int index) {
        Item i = player.inventory.get(index);
        switch(i.getType().getPrimaryAction()) {
            case USE:
                useItem(index);
                break;
            case EQUIP:
                equipItem(i);
                break;
            case EAT:
                eatItem(i);
                break;
        }
    }

    private void equipItem(Item i) {
        if (i instanceof Weapon) {
            Weapon<? extends WeaponType> w = (Weapon<? extends WeaponType>) i;
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
            if (player.armour.get(a.getType().slot) != null && ! player.inventory.hasSpaceForItem(player.armour.get(a.getType().slot))) {
                eventMessage("No space to store the item you're currently wielding");
            } else {
                Armour removed = player.armour.put(a.getType().slot, a);
                if (removed != null) {
                    player.inventory.add(removed);
                }
                player.inventory.remove(a);
            }
        }
    }

    private void eatItem(Item i) {
        if (i instanceof Consumable) {
            Consumable f = (Consumable) i;
            player.inventory.remove(f);
            player.applyStatChanges(f.getType().statChanges);
        } else if (i instanceof Armour) {
            Armour a = (Armour) i;
            if (player.armour.get(a.getType().slot) != null && ! player.inventory.hasSpaceForItem(player.armour.get(a.getType().slot))) {
                eventMessage("No space to store the item you're currently wielding");
            } else {
                Armour removed = player.armour.put(a.getType().slot, a);
                if (removed != null) {
                    player.inventory.add(removed);
                }
                player.inventory.remove(a);
            }
        }
    }

    void useItem(int index) {
        if (player.itemBeingUsed == index) {
            player.itemBeingUsed = -1;
        } else if (player.itemBeingUsed == -1) {
            player.itemBeingUsed = index;
        } else {
            int firstItem = player.itemBeingUsed;
            player.itemBeingUsed = -1;
            boolean actionPerformed = useItems(firstItem, index);
            if (!actionPerformed) {
                eventMessage("Nothing happens");
            }
        }
    }

    private boolean useItems(int firstItemIndex, int secondItemIndex) {
        String item1type = player.inventory.get(firstItemIndex).getType().name;
        String item2type = player.inventory.get(secondItemIndex).getType().name;
        ItemActivity activity;
        if (scenarioData.itemUses.get(item1type) != null && scenarioData.itemUses.get(item1type).get(item2type) != null) {
            activity = scenarioData.itemUses.get(item1type).get(item2type);
            activity.perform(this, map[player.x][player.y], player, firstItemIndex, secondItemIndex);
            return true;
        } else if (scenarioData.itemUses.get(item2type) != null && scenarioData.itemUses.get(item2type).get(item1type) != null) {
            activity = scenarioData.itemUses.get(item2type).get(item1type);
            activity.perform(this, map[player.x][player.y], player, secondItemIndex, firstItemIndex);
        }
        return false;
    }

    private boolean useItemOnTile(int itemIndex, Tile tile) {
        String itemTypeName = player.inventory.get(itemIndex).getType().name;
        String tileTypeName = tile.type.name;
        if (scenarioData.tileItemUses.get(tileTypeName) != null
                && scenarioData.tileItemUses.get(tileTypeName).get(itemTypeName) != null) {
            ItemActivity activity = scenarioData.tileItemUses.get(tileTypeName).get(itemTypeName);
            if (activity != null) {
                activity.perform(this, tile, player, itemIndex, -1);
                return true;
            }
        }
        for (GObjects.GameObject object : tile.objects) {
            Map<String, ItemActivity> objectActivities = scenarioData.objectItemUses.get(object.id());
            if (objectActivities != null && objectActivities.get(itemTypeName) != null) {
                objectActivities.get(itemTypeName).perform(this, tile, player, itemIndex, -1);
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
        if (talkingTo.currentConversation.playerText != null && talkingTo.currentConversation.playerText.length() != 0) {
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
                .map(choice -> scenarioData.conversationChoices.get(choice))
                .filter(choice -> choice.canSee.test(new QuestState(this)))
                .collect(Collectors.toList());
        talkingTo.currentConversation = options.get(i-1);
        displayConversation();
    }

    private void tick(int initialPlayerHealth, int initialPlayerFood) {
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
        } else {
            if (player.health == initialPlayerHealth && player.health < player.maxHealth) {
                player.health += 1;
            }
            if (player.food == initialPlayerFood) {
                if (player.food > 0) {
                     if (player.foodTick <= 10) {
                         player.foodTick += 1;
                     } else {
                         player.foodTick = 0;
                         player.food -= 1;
                     }
                }
            } else {
                player.foodTick = 0;
            }
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
            messageHistory.add("Killed by a "+npc.type.name);
        }
    }

    private boolean nextToPlayer(NPC npc) {
        return
                (player.x == npc.x && (player.y-1 == npc.y || player.y+1 == npc.y)) ||
                        (player.y == npc.y && (player.x-1 == npc.x || player.x+1 == npc.x));
    }

    private void gameOver() {
        appState = GAME_DEAD;
        deathScreen = new DeathScreen(messageHistory);
    }

    void switchGameMap(String map, int startX, int startY) {
        if (mapExists(map)) {
            saveGameMap(mapName);
            loadGameMap(map);
            player.x = startX;
            player.y = startY;
        } else {
            eventMessage("You've reached the edge of the world!");
        }
    }

    void npcAttacked(NPC npc) {
        if (npc.isDead()) {
            map[npc.x][npc.y].objects.add(npc.dropItem());
            player.quests.forEach((name,quest) -> quest.npcDeath(npc.type.name));
            messageHistory.add("Killed a "+npc.type.name);
        }
    }

    void spawn(GObjects.GameObject object, int x, int y) {
        map[x][y].objects.add(object);
    }

    public void startQuest(String questName) {
        Quest quest = scenarioData.quests.get(questName);
        if (quest == null) {
            throw new RuntimeException("Unable to start quest "+questName+" - quest not found");
        } else if (player.quests.containsKey(questName)) {
            throw new RuntimeException("Unable to start quest "+questName+" - already started quest");
        } else {
            player.quests.put(questName, quest.start());
        }
    }

    void showShop(Shop shop) {
        appState = GAME_SHOP;
        currentShop = shop;
        gameScreen.showWindow(new ShopWindow(this, shop));
    }

    void showCrafting(CraftingOptions craftingOptions) {
        gameScreen.showWindow(new CraftingWindow(this, craftingOptions));
    }

    void closeShop() {
        appState = GAME_RUNNING;
        currentShop = null;
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
        //noinspection RedundantIfStatement
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
        if (player.x < x && mapRoom.getMinX() == x) {
            return true;
        }
        if (player.x > x && mapRoom.getMaxX() == x+1) {
            return true;
        }
        if (player.y < y && mapRoom.getMinY() == y) {
            return true;
        }
        //noinspection RedundantIfStatement
        if (player.y > y && mapRoom.getMaxY() == y+1) {
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


    public void buyItem(Shop shop, int i) {
        ShopListing s = shop.items.get(i);
        if (s.quantity >= 1 && s.getPrice() <= player.money) {
            s.quantity -= 1;
            if (player.inventory.hasSpaceForItemOfType(s.item)) {
                player.money -= s.getPrice();
                //TODO: Shop quality
                player.inventory.add(s.item.create());
            }
        }
    }

    public void sellItem(int index) {
        Item item = player.inventory.remove(index);
        Optional<ShopListing> s = this.currentShop.items.stream().filter(l -> l.item.name.equals(item.getType().name)).findFirst();
        if (s.isPresent()) {
            s.get().quantity += 1;
            player.money += s.get().getPrice();
        } else {
            currentShop.items.add(new ShopListing(item.getType(), 1, 1, 1));
            player.money += 1;
        }
    }

    public void craftRecipe(CraftingOptions craftingOptions, int i) {
        Recipe recipe = craftingOptions.recipes.get(i);
        if (recipe.canBeDone(player)) {
            recipe.perform(player);
        }
    }
}
