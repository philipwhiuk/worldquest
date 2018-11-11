package com.whiuk.philip.worldquest;

import java.util.Random;

public class RandomSource {
    private static Random random = new Random();

    public static Random getRandom() {
        return random;
    }
}
