package com.whiuk.philip.worldquest;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExperienceTableTest {

    @Before
    public void before() {
        ExperienceTable.initializeExpTable();
    }

    @Test
    public void level0_is_0xp() {
        assertEquals(0, ExperienceTable.experienceTable[0]);
    }

    @Test
    public void level1_is_53xp() {
        assertEquals(53, ExperienceTable.experienceTable[1]);
    }

    @Test
    public void level99_is_693Kxp() {
        assertEquals(693345, ExperienceTable.experienceTable[99]);
    }

    @Test
    public void level100_does_not_exist() {
        assertEquals(100, ExperienceTable.experienceTable.length);
    }

    @Test
    public void getLevel_with_0xp_returns_0() {
        int level = ExperienceTable.getLevel(0);
        assertEquals(0, level);
    }

    @Test
    public void toLevel_with_0xp_returns_levelXp() {
        int toNextLevel = ExperienceTable.toLevel(1, 0);
        assertEquals(ExperienceTable.experienceTable[1], toNextLevel);
    }

    @Test
    public void constructor_with_level_1_exp_sets_level_to_1() {
        Experience test = new Experience(ExperienceTable.experienceTable[1]);
        assertEquals(1, test.level);
    }
}
