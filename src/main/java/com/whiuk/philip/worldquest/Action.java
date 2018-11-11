package com.whiuk.philip.worldquest;

enum Action {
    NORTH(false), EAST(false), SOUTH(false), WEST(false), STAIRS(false),
    //TODO: This indexing is very silly and broken
    USE_0(false), EQUIP_0(false), DROP_0(false),
    USE_1(false), EQUIP_1(false), DROP_1(false),
    USE_2(false), EQUIP_2(false), DROP_2(false),
    TALK_0(false), TALK_1(false), TALK_2(false),
    TALK_CONTINUE(false), CLOSE_SHOP(false),
    SWITCH_TO_SKILLS(true), SWITCH_TO_ITEMS(true), SWITCH_TO_EQUIPMENT(true), SWITCH_TO_NPCS(true),
    EXIT(false), NEW_GAME(false);

    private boolean isSidebarViewAction;

    Action(boolean isSidebarViewAction) {
        this.isSidebarViewAction = isSidebarViewAction;
    }

    public boolean isSidebarViewAction() {
        return isSidebarViewAction;
    }
}
