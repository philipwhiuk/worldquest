package com.whiuk.philip.worldquest;

public abstract class ItemAction {
    abstract void perform(WorldQuest game, Tile tile, Player player, int firstItemIndex, int secondItemIndex);
}
