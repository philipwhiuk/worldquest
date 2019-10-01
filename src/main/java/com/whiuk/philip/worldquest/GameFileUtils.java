package com.whiuk.philip.worldquest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GameFileUtils {
    static final String FILE_EXTENSION = ".json";
    static final String PLAYER_SAVE_FILE = "player";
    static final String SCENARIOS_FOLDER = "scenario";
    static final String BASE_FOLDER = "base";
    static final String SAVES_FOLDER = "saves";
    static final String SAVE_PREFIX = "save";
    static final String SCENARIO_PREFIX = "scenario";

    static void deleteDirectory(Path pathToBeDeleted) throws IOException {
        if (pathToBeDeleted.toFile().exists()) {
            Files.walk(pathToBeDeleted)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
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

    static File resourceInBaseFolder(String resource) {
        return new File(BASE_FOLDER+File.separator+resource+FILE_EXTENSION);
    }

    static List<String> scenarioList() {
        try {
            return Files
                    .list(new File(SCENARIOS_FOLDER).toPath())
                    .filter(p -> p.toFile().isDirectory())
                    .map(p -> p.getFileName().toString())
                    .sorted()
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
                    .filter(s -> s.matches(SAVE_PREFIX+"[0-9]*"))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Unable to find saved games");
            return new ArrayList<>();
        }
    }

    public static String newSaveFolder() {
        return newFolder(saveList(), SAVE_PREFIX);
    }

    public static String newScenarioFolder() {
        return newFolder(scenarioList(), SCENARIO_PREFIX);
    }

    private static String newFolder(List<String> usedList, String prefix) {
        Set<Integer> usedIntegers = usedFolderIntegers(usedList, prefix);
        int i = 0;
        while(i < Integer.MAX_VALUE) {
            if (!usedIntegers.contains(i)) {
                if (i == 0) {
                    return prefix;
                } else {
                    return prefix+i;
                }
            } else {
                i++;
            }
        }
        throw new UnsupportedOperationException();
    }

    private static Set<Integer> usedFolderIntegers(List<String> usedList, String prefix) {
        return usedList
                .stream()
                .filter(s -> s.startsWith(prefix))
                .map(s -> s.substring(prefix.length()))
                .map(sI -> {
                    if (sI.equals("")) {
                        return 0;
                    } else {
                        return Integer.parseInt(sI);
                    }
                })
                .sorted()
                .collect(Collectors.toSet());
    }

    public static void deleteSave(String saveFolder) throws IOException {
        deleteDirectory(new File(SAVES_FOLDER+File.separator+saveFolder).toPath());
    }

    public static void createSave(String saveFolder) throws IOException {
        Files.createDirectories(new File(SAVES_FOLDER+File.separator+saveFolder).toPath());
    }

    public static void createScenario(String scenarioFolder) throws IOException {
        Files.createDirectories(new File(SCENARIOS_FOLDER+File.separator+scenarioFolder).toPath());
    }
}
