package com.whiuk.philip.worldquest;

enum Action {
    NORTH(false), EAST(false), SOUTH(false), WEST(false), STAIRS(false),
    TALK_CONTINUE(false), CLOSE_SHOP(false),
    EXIT(false), START_NEW_GAME(false),
    CONVERSATION_OPTION(false);

    private boolean isSidebarViewAction;

    Action(boolean isSidebarViewAction) {
        this.isSidebarViewAction = isSidebarViewAction;
    }

    public boolean isSidebarViewAction() {
        return isSidebarViewAction;
    }
}
