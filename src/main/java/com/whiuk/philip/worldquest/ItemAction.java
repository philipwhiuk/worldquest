package com.whiuk.philip.worldquest;

abstract class ItemAction {
    abstract void perform(WorldQuest game, Tile tile, Player player, int firstItemIndex, int secondItemIndex);
}
