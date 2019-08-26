package com.whiuk.philip.worldquest;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResourceGatheringProvider {

    @SuppressWarnings("unused")
    static //TODO: Load Resource Gathering Methods from buffer
    Map<String, ResourceGathering> loadResourceGatheringFromBuffer(BufferedReader buffer) {
        Map<String, ResourceGathering> resourceGathering = new HashMap<>();
        resourceGathering.put("Mine", new ResourceGathering(
                "Rock",
                "Rock",
                "RockShards",
                Arrays.asList("CopperVein", "TinVein"),
                "Mining",
                15));
        resourceGathering.put("DigGrass", new ResourceGathering(
                "Grass",
                "Dirt",
                "PileOfDirt",
                Collections.emptyList(),
                "Digging",
                10));
        resourceGathering.put("DigDirt", new ResourceGathering(
                "Grass",
                "Rock",
                "PileOfDirt",
                Collections.emptyList(),
                "Digging",
                10));
        resourceGathering.put("Fish", new ResourceGathering(
                "Water",
                "Water",
                "RawCatfish",
                Collections.emptyList(),
                "Fishing",
                10));
        return resourceGathering;
    }
}
