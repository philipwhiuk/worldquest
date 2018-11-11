package com.whiuk.philip.worldquest;

import java.util.Arrays;

public class ExperienceTable {
    static int[] experienceTable;

    static void initializeExpTable() {
        experienceTable = new int[100];
        float experience = 0;
        for (int i = 0; i < 100; i++) {
            float level = i;
            experience = (experience*(15f/14f))+50;
            experienceTable[i] = (int) experience-50;
        }
        System.out.println(Arrays.toString(experienceTable));
    }

    static int getLevel(int experience) {
        for (int level = 0; level < 100; level++) {
            if (experienceTable[level] > experience) {
                return level >= 1 ? level-1 : 0;
            }
        }
        return 99;
    }

    static int toLevel(int level, int experience) {
        return experienceTable[level] - experience;
    }
}
