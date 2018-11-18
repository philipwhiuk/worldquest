package com.whiuk.philip.worldquest;

import org.junit.Test;

import java.io.*;

public class PlayerLoadSaveTest {

    @Test
    public void can_load_and_save_player() throws IOException {
        Player player = new Player(0, 0);
        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
        PlayerProvider.savePlayer(bufferedWriter, player);
        bufferedWriter.flush();
        String data = stringWriter.toString();
        BufferedReader bufferedReader = new BufferedReader(new StringReader(data));
        PlayerProvider.loadPlayer(bufferedReader);
    }
}
