package com.whiuk.philip.worldquest;

import java.util.Random;

//TODO: I'm not even sure if this is a good idea
public class RandomSource {
    private static Random random = new Random();

    public static Random getRandom() {
        return random;
    }
}
