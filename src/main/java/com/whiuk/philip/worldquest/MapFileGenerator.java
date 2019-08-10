package com.whiuk.philip.worldquest;

public class MapFileGenerator {

    public void generate() {
        /**
         *
         * Work out what of the 9 surrounding map tiles already exist.
         *
         * NE|N|NW
         *  E|*|W
         * SE|S|SW
         *
         * Terrain features shouldn't end abruptly on a tile boundary
         *
         * Match map tile borders in terms of movability.
         *
         *  Simple case - edge:
         * 1) Walkable edge
         * Provide a walkable tile
         * 2) Non-walkable edge
         * Provide a non-walkable tile.
         *  Complex case - corner:
         * 1) Walkable & non-walkable?
         * Impossible to solve. Shouldn't exist in practice
         * 2) Walkable & ?
         * Provide a walkable tile.
         * 3) Non-walkable & ?
         * Provide a non-walkable tile.
         * 4) ? & ?
         * Provide an arbitrary tile.
         *
         * Match map tile contents in terms of functionality
         *
         * If N and S is the same city, be a city square.
         * If E and W is the same city, be a city square.
         * If they are different cities, be an edge square
         * If one is a non-city, be a city-edge square.
         * Ditto other features.
         *
         * Examine surrounding map
         *
         */
    }

}
