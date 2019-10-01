package com.whiuk.philip.worldquest;

import org.json.simple.JSONObject;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PlayerProviderTest {

    private String savePlayerToString(Player player) throws IOException {
        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
        PlayerProvider.savePlayer(bufferedWriter, player);
        bufferedWriter.flush();
        return stringWriter.toString();
    }

    private Player loadPlayerFromString(String data) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(data));
        return PlayerProvider.loadPlayer(new JSONObject(), new HashMap<>(), new HashMap<>());
    }

    @Test
    public void can_load_and_save_player() throws IOException {
        Player player = new Player(0, 0);

        String data = savePlayerToString(player);
        Player loadedPlayer = loadPlayerFromString(data);

        assertNotNull(loadedPlayer);
    }

    @Test
    public void stores_and_retrieves_stats() throws IOException {
        Player player = new Player(0, 0);
        player.stats.put("Strength", new Experience(1));

        String data = savePlayerToString(player);
        Player loadedPlayer = loadPlayerFromString(data);

        assertEquals(1, loadedPlayer.stats.size());
    }

    @Test
    public void stores_and_retrieves_skills() throws IOException {
        Player player = new Player(0, 0);
        player.skills.put("Woodcutting", new Experience(1));

        String data = savePlayerToString(player);
        Player loadedPlayer = loadPlayerFromString(data);

        assertEquals(1, loadedPlayer.skills.size());
    }

    @Test
    public void stores_and_retrieves_armour() throws IOException {
        Player player = new Player(0, 0);
        player.armour.put(Slot.CHEST, new Armour(new ArmourType("Chestplate",
                Collections.emptyList(), Slot.CHEST, 1)));

        String data = savePlayerToString(player);
        Player loadedPlayer = loadPlayerFromString(data);

        assertEquals(1, loadedPlayer.armour.size());
    }

    @Test
    public void stores_and_retrieves_inventory_items() throws IOException {
        Player player = new Player(0, 0);
        player.inventory.add(new Armour(new ArmourType("Chestplate",
                Collections.emptyList(), Slot.CHEST, 1)));

        String data = savePlayerToString(player);
        Player loadedPlayer = loadPlayerFromString(data);

        assertEquals(1, loadedPlayer.inventory.size());
    }

    @Test
    public void stores_and_retrieves_quest_status() throws IOException {
        Player player = new Player(0, 0);
        Quest questA = new Quest("A", Collections.singletonList(
                new QuestStep(new HashMap<>())));
        player.quests.put("A", new QuestProgress(questA, QuestStatus.STARTED, 0, new QuestStepProgress(new HashMap<>())));

        String data = savePlayerToString(player);
        Player loadedPlayer = loadPlayerFromString(data);

        assertEquals(1, loadedPlayer.quests.size());
    }
}
