package com.whiuk.philip.worldquest;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExperienceTest {

    @Before
    public void before() {
        ExperienceTable.initializeExpTable();
    }

    @Test
    public void constructor_with_0_exp_set_level_to_0() {
        Experience test = new Experience(0);
        assertEquals(test.level, 0);
    }

    @Test
    public void constructor_with_level_1_exp_sets_level_to_1() {
        Experience test = new Experience(ExperienceTable.experienceTable[1]);
        assertEquals(test.level, 1);
    }

    @Test
    public void NoExperience_provides_Experience_with_level_and_xp_0() {
        Experience test = Experience.NoExperience();
        assertEquals(test.level, 0);
        assertEquals(test.experience,0);
    }
}
