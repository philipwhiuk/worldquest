package com.whiuk.philip.worldquest;

public

class Experience {
    int level;
    int experience;

    static Experience NoExperience() {
        return new Experience(0);
    }

    Experience(int experience) {
        this.level = ExperienceTable.getLevel(experience);
        this.experience = experience;
    }
}
