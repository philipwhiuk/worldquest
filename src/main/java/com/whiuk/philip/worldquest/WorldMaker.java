package com.whiuk.philip.worldquest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.whiuk.philip.worldquest.GameData.*;
import static com.whiuk.philip.worldquest.MapConstants.*;

public class WorldMaker extends JFrame implements KeyListener, MouseListener {
    private HashMap<String, GObjects.GameObjectBuilder> gameObjectBuilders;

    public static void main(String[] args) {
        new WorldMaker();
    }

    private boolean mapLoaded;
    private Map<String, NPCType> npcTypes = new HashMap<>();
    private Map<String, TileType> tileTypes = new HashMap<>();
    private Tile[][] map;
    private List<NPC> npcs;

    public WorldMaker() {
        ScheduledExecutorService renderQueue = Executors.newSingleThreadScheduledExecutor();
        WorldMakerCanvas canvas = new WorldMakerCanvas();
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
                loadMap("map.dat");
                mapLoaded = true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
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
        gameObjectBuilders = GObjects.provideBuilders();
    }

    private void loadTileTypes() {
        //TODO: At some point this will come from a file too, but right now there's not much to load.
        tileTypes.put("Grass", Grass);
        tileTypes.put("Wall", Wall);
        tileTypes.put("Floor", Floor);
        tileTypes.put("Door", Door);
        npcTypes.put("Goblin", Goblin);
    }
    
    private void loadMap(String mapResourceName) {
        //TODO: Map format not very efficient but easy to read
        InputStream mapDataStream = getClass().getResourceAsStream(mapResourceName);
        if (mapDataStream == null) {
            throw new RuntimeException("Unable to load map.dat data: Map data file not found");
        }
        try(BufferedReader buffer = new BufferedReader(new InputStreamReader(mapDataStream))) {
            Tile[][] newMap = processMap(buffer);
            List<NPC> npcs = processNPCs(buffer);
            processGameObjects(buffer, newMap);
            this.npcs = npcs;
            this.map = newMap;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load map.dat data: " + e.getMessage(), e);
        }
    }



    private Tile[][] processMap(BufferedReader buffer) throws IOException {
        Tile[][] newMap = new Tile[MAP_WIDTH][MAP_HEIGHT];
        int mapLines = Integer.parseInt(buffer.readLine());
        if (mapLines != MAP_HEIGHT) {
            throw new RuntimeException("Invalid map.dat size");
        }
        for (int y = 0; y < MAP_HEIGHT; y++) {
            String mapLine = buffer.readLine();
            if (mapLine != null) {
                processMapLine(newMap, y, mapLine);
            }
        }
        return newMap;
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

    private class WorldMakerCanvas extends JPanel implements Runnable {

        WorldMakerCanvas() {
            Dimension size = new Dimension(PANEL_WIDTH, PANEL_HEIGHT);
            setPreferredSize(size);
        }

        @Override
        public void paintComponent(Graphics gBase) {
            try {
                Graphics2D g = (Graphics2D) gBase;
                g.setColor(Color.black);
                g.fillRect(0, 0, 640, 480);
                if (!mapLoaded) {
                    paintLoading(g);
                } else {
                    paintMap(g);
                    paintNPCs(g);
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
                            g.setColor(map[x][y].getColor(true));
                            g.fillRect(MAP_SPACING + (x * TILE_WIDTH), MAP_SPACING + (y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT);
                            for (GObjects.GameObject object : map[x][y].objects) {
                                object.draw(g, x, y);
                            }
                        }
                    }
                }
            }
        }

        private void paintNPCs(Graphics2D g) {
            npcs.forEach(npc -> {
                paintCharacter(g, npc);
            });
        }

        private void paintCharacter(Graphics2D g, GameCharacter c) {
            if (c != null) {
                g.setColor(Color.BLACK);
                g.drawOval(MAP_SPACING + (c.x * TILE_WIDTH), MAP_SPACING + (c.y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT);
                g.setColor(c.color);
                g.fillOval(MAP_SPACING + (c.x * TILE_WIDTH), MAP_SPACING + (c.y * TILE_HEIGHT), TILE_WIDTH, TILE_HEIGHT);
            }
        }

        @Override
        public void run() {
            repaint();
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

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
