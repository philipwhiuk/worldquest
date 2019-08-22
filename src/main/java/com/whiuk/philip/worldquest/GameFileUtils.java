package com.whiuk.philip.worldquest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GameFileUtils {
    static final String FILE_EXTENSION = ".dat";
    static final String PLAYER_SAVE_FILE = "player";
    static final String SCENARIOS_FOLDER = "scenario";
    static final String SAVES_FOLDER = "saves";

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
        return new File(SAVES_FOLDER+File.separator+saveFolder+File.separator+resource+FILE_EXTENSION);
    }

    static File resourceInScenarioFolder(String scenario, String resource) {
        return new File(SCENARIOS_FOLDER+File.separator+scenario+File.separator+resource+FILE_EXTENSION);
    }

    static List<String> scenarioList() {
        try {
            return Files
                    .list(new File(SCENARIOS_FOLDER).toPath())
                    .filter(p -> p.toFile().isDirectory())
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Unable to find scenarios");
            return new ArrayList<>();
        }
    }

    static List<String> saveList() {
        try {
            return Files
                    .list(new File(SAVES_FOLDER).toPath())
                    .filter(p -> p.toFile().isDirectory())
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Unable to find saved games");
            return new ArrayList<>();
        }
    }
}
