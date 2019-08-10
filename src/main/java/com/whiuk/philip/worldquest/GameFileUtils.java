package com.whiuk.philip.worldquest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class GameFileUtils {
    static final String FILE_EXTENSION = ".dat";
    static final String PLAYER_SAVE_FILE = "player";

    static void deleteDirectory(Path pathToBeDeleted) throws IOException {
        Files.walk(pathToBeDeleted)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    static boolean savedGameExists(String saveFolder) {
        return resourceInSaveFolder(saveFolder, PLAYER_SAVE_FILE).exists();
    }

    static File resourceInSaveFolder(String saveFolder, String resource) {
        return new File("saves"+File.separator+saveFolder+File.separator+resource+FILE_EXTENSION);
    }

    static File resourceInScenarioFolder(String scenario, String resource) {
        return new File("scenario"+File.separator+scenario+File.separator+resource+FILE_EXTENSION);
    }
}
